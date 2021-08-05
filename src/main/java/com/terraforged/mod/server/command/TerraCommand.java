/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.world.WorldGenerator;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.settings.preset.Preset;
import com.terraforged.mod.chunk.settings.preset.PresetManager;
import com.terraforged.mod.data.DataGen;
import com.terraforged.mod.profiler.Profiler;
import com.terraforged.mod.server.command.arg.TerrainArgType;
import com.terraforged.mod.server.command.search.BiomeSearchTask;
import com.terraforged.mod.server.command.search.BothSearchTask;
import com.terraforged.mod.server.command.search.Search;
import com.terraforged.mod.server.command.search.TerrainSearchTask;
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
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("benchmark")
                        .then(Commands.literal("reset")
                                .executes(TerraCommand::benchmarkStart))
                        .then(Commands.literal("stats")
                                .executes(TerraCommand::benchmarkStats)))
                .then(Commands.literal("query")
                        .executes(TerraCommand::query))
                .then(Commands.literal("data")
                        .then(Commands.literal("dump")
                                .executes(TerraCommand::dump)))
                .then(Commands.literal("preset")
                        .then(Commands.literal("save")
                                .then(Commands.argument("name", StringArgumentType.greedyString())
                                        .executes(TerraCommand::savePreset))))
                .then(Commands.literal("debug")
                        .executes(TerraCommand::debugBiome))
                .then(Commands.literal("locate")
                        .then(Commands.literal("biome")
                                .then(Commands.argument("biome", ResourceLocationArgument.id())
                                        .suggests(SuggestionProviders.AVAILABLE_BIOMES)
                                        .executes(TerraCommand::findBiome)))
                        .then(Commands.literal("terrain")
                                .then(Commands.argument("terrain", TerrainArgType.terrain())
                                        .executes(TerraCommand::findTerrain)))
                        .then(Commands.literal("both")
                                .then(Commands.argument("biome", ResourceLocationArgument.id())
                                        .suggests(SuggestionProviders.AVAILABLE_BIOMES)
                                        .then(Commands.argument("terrain", TerrainArgType.terrain())
                                                .executes(TerraCommand::findTerrainAndBiome)))));
    }

    private static int query(CommandContext<CommandSource> context) throws CommandSyntaxException {
        getContext(context);
        BlockPos pos = context.getSource().getPlayerOrException().blockPosition();
        TFBiomeProvider biomeProvider = getBiomeProvider(context);
        try (Resource<Cell> cell = Cell.getResource()) {
            Biome biome = biomeProvider.lookupBiome(cell.get(), pos.getX(), pos.getZ(), false);
            context.getSource().sendSuccess(new StringTextComponent("At ")
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

    private static int benchmarkStart(CommandContext<CommandSource> context) throws CommandSyntaxException {
        Profiler.reset();
        context.getSource().sendSuccess(createText("Reset profiler"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int benchmarkStats(CommandContext<CommandSource> context) throws CommandSyntaxException {
        long fastest = 0L;
        long slowest = 0L;
        double average = 0.0;

        for (Profiler profiler : Profiler.values()) {
            average += profiler.averageMS();
            slowest += profiler.maxMS();
            fastest += profiler.minMS();
            context.getSource().sendSuccess(profiler.toText(), false);
        }

        long min = fastest;
        long max = slowest;
        context.getSource().sendSuccess(createText("Chunk Average", PREFIX_FORMAT)
                .append(String.format(": %.3fms", average))
                .withStyle(style -> style.withHoverEvent(Profiler.createHoverStats(min, max))),false);

        return Command.SINGLE_SUCCESS;
    }

    private static int dump(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);

        try {
            context.getSource().sendSuccess(new StringTextComponent("Exporting data"), true);
            DataGen.dumpData(terraContext.biomeContext);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int savePreset(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);
        String name = StringArgumentType.getString(context, "name");
        Preset preset = new Preset(name, terraContext.terraSettings);
        context.getSource().sendSuccess(new StringTextComponent("Saving preset: " + preset.getName()), true);

        PresetManager presets = PresetManager.load();
        presets.add(preset);
        presets.saveAll();

        return Command.SINGLE_SUCCESS;
    }

    private static int debugBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrException();
        BlockPos position = player.blockPosition();
        int x = position.getX();
        int z = position.getZ();

        long seed = player.getLevel().getSeed();
        BiomeProvider biomeProvider = player.getLevel().getChunkSource().generator.getBiomeSource();
        Biome actual = player.getLevel().getBiome(position);
        Biome biome = ColumnFuzzedBiomeMagnifier.INSTANCE.getBiome(seed, x, 0, z, biomeProvider);

        context.getSource().sendSuccess(new StringTextComponent("At ")
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
        BlockPos pos = context.getSource().getPlayerOrException().blockPosition();
        UUID playerID = context.getSource().getPlayerOrException().getUUID();
        MinecraftServer server = context.getSource().getServer();
        WorldGenerator generator = terraContext.worldGenerator.get().get();
        Search search = new TerrainSearchTask(pos, type, getChunkGenerator(context), generator);
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendSuccess(createPrefix(identifier)
                .append(new StringTextComponent(" Searching for "))
                .append(createPrimary(type.getName()))
                .append(new StringTextComponent("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int findBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        getContext(context);
        Biome biome = getBiome(context, "biome");
        BlockPos pos = context.getSource().getPlayerOrException().blockPosition();
        UUID playerID = context.getSource().getPlayerOrException().getUUID();
        MinecraftServer server = context.getSource().getServer();
        ServerWorld world = context.getSource().getPlayerOrException().getLevel();
        Search search = new BiomeSearchTask(pos, biome, world.getChunkSource().getGenerator(), getBiomeProvider(context));
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendSuccess(createPrefix(identifier)
                .append(new StringTextComponent(" Searching for "))
                .append(createPrimary(biome.getRegistryName()))
                .append(new StringTextComponent("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrainAndBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context);
        Terrain target = TerrainArgType.getTerrain(context, "terrain");
        Biome biome = getBiome(context, "biome");
        BlockPos pos = context.getSource().getPlayerOrException().blockPosition();
        UUID playerID = context.getSource().getPlayerOrException().getUUID();
        MinecraftServer server = context.getSource().getServer();
        WorldGenerator generator = terraContext.worldGenerator.get().get();
        Search biomeSearch = new BiomeSearchTask(pos, biome, getChunkGenerator(context), getBiomeProvider(context));
        Search terrainSearch = new TerrainSearchTask(pos, target, getChunkGenerator(context), generator);
        Search search = new BothSearchTask(pos, biomeSearch, terrainSearch);
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendSuccess(createPrefix(identifier)
                .append(new StringTextComponent(" Searching for "))
                .append(createPrimary(biome.getRegistryName()))
                .append(new StringTextComponent(" and "))
                .append(createPrimary(target.getName()))
                .append(new StringTextComponent("...")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int doSearch(MinecraftServer server, UUID userId, Supplier<BlockPos> supplier) {
        int identifier = SEARCH_IDS.compute(userId, INCREMENTER);
        CompletableFuture.supplyAsync(supplier).thenAccept(pos -> server.submit(() -> {
            PlayerEntity player = server.getPlayerList().getPlayer(userId);
            if (player == null) {
                SEARCH_IDS.remove(userId);
                return;
            }

            if (pos == BlockPos.ZERO) {
                ITextComponent message = createPrefix(identifier).append(new StringTextComponent(" Location not found :["));
                player.sendMessage(message, Util.NIL_UUID);
                return;
            }

            double distance = Math.sqrt(player.blockPosition().distSqr(pos));
            ITextComponent result = createPrefix(identifier)
                    .append(new StringTextComponent(" Nearest match: "))
                    .append(createTeleportMessage(pos))
                    .append(new StringTextComponent(String.format(" Distance: %.2f", distance)));

            player.sendMessage(result, Util.NIL_UUID);
        }));
        return identifier;
    }

    private static TerraContext getContext(CommandContext<CommandSource> context) throws CommandSyntaxException {
        return getTFChunkGenerator(context).getContext();
    }

    private static Biome getBiome(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
        ResourceLocation location = ResourceLocationArgument.getId(context, name);
        return context.getSource().getServer().registryAccess().registry(Registry.BIOME_REGISTRY)
                .flatMap(registry -> registry.getOptional(location))
                .orElseThrow(() -> createException("biome", "Unrecognized biome %s", location));
    }

    private static String getBiomeName(CommandContext<CommandSource> context, Biome biome) {
        return context.getSource().getServer().registryAccess().registry(Registry.BIOME_REGISTRY)
                .map(r -> r.getKey(biome))
                .map(Objects::toString)
                .orElse("unknown");
    }

    private static TFChunkGenerator getTFChunkGenerator(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ChunkGenerator generator = context.getSource().getLevel().getChunkSource().getGenerator();
        if (generator instanceof TFChunkGenerator) {
            return (TFChunkGenerator) generator;
        }
        throw createException("Invalid world type", "This command can only be run in a TerraForged world!");
    }

    private static ChunkGenerator getChunkGenerator(CommandContext<CommandSource> context) {
        return context.getSource().getLevel().getChunkSource().getGenerator();
    }

    private static TFBiomeProvider getBiomeProvider(CommandContext<CommandSource> context) {
        return (TFBiomeProvider) context.getSource().getLevel().getChunkSource().getGenerator().getBiomeSource();
    }

    private static CommandSyntaxException createException(String type, String message, Object... args) {
        return new CommandSyntaxException(
                new SimpleCommandExceptionType(new StringTextComponent(type)),
                new StringTextComponent(String.format(message, args))
        );
    }

    private static IFormattableTextComponent createTeleportMessage(BlockPos pos) {
        return TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent(
                "chat.coordinates", pos.getX(), pos.getY(), pos.getZ()
        )).withStyle(s -> s.withColor(TextFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")))
        );
    }

    private static IFormattableTextComponent createPrefix(int identifier) {
        return new StringTextComponent("")
                .append(TextComponentUtils.wrapInSquareBrackets(new StringTextComponent(String.format("%03d", identifier)))
                        .withStyle(style -> style.withColor(PREFIX_FORMAT)));
    }

    private static IFormattableTextComponent createPrimary(@Nullable Object name) {
        return createText(name, TITLE_FORMAT);
    }

    private static IFormattableTextComponent createText(@Nullable Object name, TextFormatting... formatting) {
        String title = name == null ? "null" : name.toString();
        return new StringTextComponent("").append(new StringTextComponent(title).withStyle(style -> {
            style.applyFormats(formatting);
            return style;
        }));
    }
}
