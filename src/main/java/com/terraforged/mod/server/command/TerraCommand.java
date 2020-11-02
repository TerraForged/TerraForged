/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.profiler.Profiler;
import com.terraforged.mod.chunk.settings.SettingsHelper;
import com.terraforged.mod.data.DataGen;
import com.terraforged.mod.server.command.arg.TerrainArgType;
import com.terraforged.mod.server.command.search.BiomeSearchTask;
import com.terraforged.mod.server.command.search.BothSearchTask;
import com.terraforged.mod.server.command.search.Search;
import com.terraforged.mod.server.command.search.TerrainSearchTask;
import com.terraforged.world.WorldGenerator;
import com.terraforged.world.terrain.Terrain;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.command.arguments.SuggestionProviders;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TerraCommand {
    private static final TextFormatting TITLE_FORMAT = TextFormatting.ITALIC;
    private static final TextFormatting SECONDARY_FORMAT = TextFormatting.YELLOW;
    private static final TextFormatting PREFIX_FORMAT = TextFormatting.GOLD;
    private static final Map<UUID, Integer> SEARCH_IDS = Collections.synchronizedMap(new HashMap<>());
    private static final BiFunction<UUID, Integer, Integer> INCREMENTER = (k, v) -> v == null ? 0 : v + 1;

    public static void init() {
        ArgumentTypes.register("terraforged:terrain", TerrainArgType.class, new ArgumentSerializer<>(TerrainArgType::new));
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        Log.info("Registering /terra command");
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(command());
    }

    private static LiteralArgumentBuilder<CommandSource> command() {
        return Commands.literal("terra")
                .requires(source -> source.hasPermissionLevel(2))
                .then(Commands.literal("stats")
                        .executes(TerraCommand::stats))
                .then(Commands.literal("query")
                        .executes(TerraCommand::query))
                .then(Commands.literal("data")
                        .then(Commands.literal("dump")
                                .executes(TerraCommand::dump)))
                .then(Commands.literal("defaults")
                        .then(Commands.literal("set")
                                .executes(TerraCommand::setDefaults)))
                .then(Commands.literal("debug")
                        .executes(TerraCommand::debugBiome))
                .then(Commands.literal("locate")
                        .then(Commands.literal("biome")
                                .then(Commands.argument("biome", ResourceLocationArgument.resourceLocation())
                                        .suggests(SuggestionProviders.field_239574_d_)
                                        .executes(TerraCommand::findBiome)))
                        .then(Commands.literal("terrain")
                                .then(Commands.argument("terrain", TerrainArgType.terrain())
                                        .executes(TerraCommand::findTerrain)))
                        .then(Commands.literal("both")
                                .then(Commands.argument("biome", ResourceLocationArgument.resourceLocation())
                                        .suggests(SuggestionProviders.field_239574_d_)
                                        .then(Commands.argument("terrain", TerrainArgType.terrain())
                                                .executes(TerraCommand::findTerrainAndBiome)))));
    }

    private static int query(CommandContext<CommandSource> context) throws CommandSyntaxException {
        getContext(context);
        BlockPos pos = context.getSource().asPlayer().getPosition();
        TerraBiomeProvider biomeProvider = getBiomeProvider(context);
        try (Resource<Cell> cell = biomeProvider.lookupPos(pos.getX(), pos.getZ())) {
            Biome biome = biomeProvider.getBiome(cell.get(), pos.getX(), pos.getZ());
            context.getSource().sendFeedback(new StringTextComponent("At ")
                    .append(createTeleportMessage(pos))
                    .append(new StringTextComponent(": TerrainType = "))
                    .append(createPrimary(cell.get().terrain.getName()))
                    .append(new StringTextComponent(", Biome = "))
                    .append(createPrimary(biome.getRegistryName()))
                    .append(new StringTextComponent(", BiomeType = "))
                    .append(createPrimary(cell.get().biome.name())), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int stats(CommandContext<CommandSource> context) throws CommandSyntaxException {
        getContext(context);
        double sum = 0.0;
        context.getSource().sendFeedback(createText("===== Profiler Stats =====", PREFIX_FORMAT), false);

        for (Profiler profiler : Profiler.values()) {
            sum += profiler.averageMS();
            context.getSource().sendFeedback(profiler.toText(), false);
        }

        context.getSource().sendFeedback(createText("Chunk Average", PREFIX_FORMAT)
                .appendString(String.format(": %.3fms", sum)), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int dump(CommandContext<CommandSource> context) throws CommandSyntaxException {
        getContext(context);

        try {
            context.getSource().sendFeedback(new StringTextComponent("Exporting data"), true);
            DataGen.dumpData();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setDefaults(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);
        context.getSource().sendFeedback(new StringTextComponent("Setting generator defaults"), true);
        SettingsHelper.exportDefaults(terraContext.terraSettings);
        return Command.SINGLE_SUCCESS;
    }

    private static int debugBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        BlockPos position = player.getPosition();
        int x = position.getX();
        int z = position.getZ();

        long seed = player.getServerWorld().getSeed();
        BiomeProvider biomeProvider = player.getServerWorld().getChunkProvider().generator.getBiomeProvider();
        Biome actual = player.getServerWorld().getBiome(position);
        Biome biome = ColumnFuzzedBiomeMagnifier.INSTANCE.getBiome(seed, x, 0, z, biomeProvider);

        context.getSource().sendFeedback(new StringTextComponent("At ")
                .append(createTeleportMessage(position))
                .append(new StringTextComponent(": Actual Biome = "))
                .append(createPrimary(actual.getRegistryName()))
                .append(new StringTextComponent(", Lookup Biome = "))
                .append(createPrimary(biome.getRegistryName())), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrain(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);
        Terrain type = TerrainArgType.getTerrain(context, "terrain");
        BlockPos pos = context.getSource().asPlayer().getPosition();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        WorldGenerator generator = terraContext.factory.get().get();
        Search search = new TerrainSearchTask(pos, type, getChunkGenerator(context), generator);
        doSearch(server, playerID, search);
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                .append(new StringTextComponent(" Searching for "))
                .append(createPrimary(type.getName()))
                .append(new StringTextComponent("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int findBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        getContext(context);
        Biome biome = getBiome(context, "biome");
        BlockPos pos = context.getSource().asPlayer().getPosition();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        ServerWorld world = context.getSource().asPlayer().getServerWorld();
        Search search = new BiomeSearchTask(pos, biome, world.getChunkProvider().getChunkGenerator(), getBiomeProvider(context));
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                .append(new StringTextComponent(" Searching for "))
                .append(createPrimary(biome.getRegistryName()))
                .append(new StringTextComponent("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrainAndBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);
        Terrain target = TerrainArgType.getTerrain(context, "terrain");
        Biome biome = getBiome(context, "biome");
        BlockPos pos = context.getSource().asPlayer().getPosition();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        WorldGenerator generator = terraContext.factory.get().get();
        Search biomeSearch = new BiomeSearchTask(pos, biome, getChunkGenerator(context), getBiomeProvider(context));
        Search terrainSearch = new TerrainSearchTask(pos, target, getChunkGenerator(context), generator);
        Search search = new BothSearchTask(pos, biomeSearch, terrainSearch);
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                .append(new StringTextComponent(" Searching for "))
                .append(createPrimary(biome.getRegistryName()))
                .append(new StringTextComponent(" and "))
                .append(createPrimary(target.getName()))
                .append(new StringTextComponent("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int doSearch(MinecraftServer server, UUID userId, Supplier<BlockPos> supplier) {
        int identifier = SEARCH_IDS.compute(userId, INCREMENTER);
        CompletableFuture.supplyAsync(supplier).thenAccept(pos -> server.deferTask(() -> {
            PlayerEntity player = server.getPlayerList().getPlayerByUUID(userId);
            if (player == null) {
                SEARCH_IDS.remove(userId);
                return;
            }

            if (pos == BlockPos.ZERO) {
                ITextComponent message = createPrefix(identifier).append(new StringTextComponent(" Location not found :["));
                player.sendMessage(message, Util.DUMMY_UUID);
                return;
            }

            double distance = Math.sqrt(player.getPosition().distanceSq(pos));
            ITextComponent result = createPrefix(identifier)
                    .append(new StringTextComponent(" Nearest match: "))
                    .append(createTeleportMessage(pos))
                    .append(new StringTextComponent(String.format(" Distance: %.2f", distance)));

            player.sendMessage(result, Util.DUMMY_UUID);
        }));
        return identifier;
    }

    private static TerraContext getContext(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ChunkGenerator generator = context.getSource().getWorld().getChunkProvider().getChunkGenerator();
        if (generator instanceof TerraChunkGenerator) {
            TerraChunkGenerator gen = (TerraChunkGenerator) generator;
            return gen.getContext();
        }
        throw createException("Invalid world type", "This command can only be run in a TerraForged world!");
    }

    private static Biome getBiome(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
        ResourceLocation location = ResourceLocationArgument.getResourceLocation(context, name);
        return context.getSource().getServer().func_244267_aX().func_230521_a_(Registry.BIOME_KEY)
                .flatMap(registry -> registry.getOptional(location))
                .orElseThrow(() -> createException("biome", "Unrecognized biome %s", location));
    }

    private static String getBiomeName(CommandContext<CommandSource> context, Biome biome) {
        return context.getSource().getServer().func_244267_aX()
                .func_230521_a_(Registry.BIOME_KEY)
                .map(r -> r.getKey(biome))
                .map(Objects::toString)
                .orElse("unknown");
    }

    private static ChunkGenerator getChunkGenerator(CommandContext<CommandSource> context) {
        return context.getSource().getWorld().getChunkProvider().getChunkGenerator();
    }

    private static TerraBiomeProvider getBiomeProvider(CommandContext<CommandSource> context) {
        return (TerraBiomeProvider) context.getSource().getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider();
    }

    private static CommandSyntaxException createException(String type, String message, Object... args) {
        return new CommandSyntaxException(
                new SimpleCommandExceptionType(new StringTextComponent(type)),
                new StringTextComponent(String.format(message, args))
        );
    }

    private static IFormattableTextComponent createTeleportMessage(BlockPos pos) {
        return TextComponentUtils.wrapWithSquareBrackets(new TranslationTextComponent(
                "chat.coordinates", pos.getX(), pos.getY(), pos.getZ()
        )).modifyStyle(s -> s.applyFormatting(TextFormatting.GREEN)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")))
        );
    }

    private static IFormattableTextComponent createPrefix(int identifier) {
        return new StringTextComponent("")
                .append(TextComponentUtils.wrapWithSquareBrackets(new StringTextComponent(String.format("%03d", identifier)))
                        .modifyStyle(style -> style.applyFormatting(PREFIX_FORMAT)));
    }

    private static IFormattableTextComponent createPrimary(@Nullable Object name) {
        return createText(name, TITLE_FORMAT);
    }

    private static IFormattableTextComponent createText(@Nullable Object name, TextFormatting... formatting) {
        String title = name == null ? "null" : name.toString();
        return new StringTextComponent("").append(new StringTextComponent(title).modifyStyle(style -> {
            for (TextFormatting f : formatting) {
                style = style.applyFormatting(f);
            }
            return style;
        }));
    }
}
