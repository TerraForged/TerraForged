/*
 *
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
import com.terraforged.mod.chunk.settings.SettingsHelper;
import com.terraforged.mod.data.DataGen;
import com.terraforged.mod.server.command.arg.BiomeArgType;
import com.terraforged.mod.server.command.arg.TerrainArgType;
import com.terraforged.mod.server.command.search.BiomeSearchTask;
import com.terraforged.mod.server.command.search.BothSearchTask;
import com.terraforged.mod.server.command.search.Search;
import com.terraforged.mod.server.command.search.TerrainSearchTask;
import com.terraforged.world.WorldGenerator;
import com.terraforged.world.terrain.Terrain;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.command.arguments.ArgumentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TerraCommand {

    private static final Map<UUID, Integer> SEARCH_IDS = Collections.synchronizedMap(new HashMap<>());
    private static final BiFunction<UUID, Integer, Integer> INCREMENTER = (k, v) -> v == null ? 0 : v + 1;

    private static final TextFormatting TITLE_FORMAT = TextFormatting.ITALIC;
    private static final TextFormatting PREFIX_FORMAT = TextFormatting.GOLD;

    public static void init() {
        ArgumentTypes.register("terraforged:biome", BiomeArgType.class, new ArgumentSerializer<>(BiomeArgType::new));
        ArgumentTypes.register("terraforged:terrain", TerrainArgType.class, new ArgumentSerializer<>(TerrainArgType::new));
    }

    @SubscribeEvent
    public static void register(FMLServerStartingEvent event) {
        Log.info("Registering /terra command");
        register(event.getCommandDispatcher());
    }

    @SubscribeEvent
    public static void disconnect(PlayerEvent.PlayerLoggedOutEvent event) {
        SEARCH_IDS.remove(event.getPlayer().getUniqueID());
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(command());
    }

    private static LiteralArgumentBuilder<CommandSource> command() {
        return Commands.literal("terra")
                .requires(source -> source.hasPermissionLevel(2))
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
                                .then(Commands.argument("biome", BiomeArgType.biome())
                                        .executes(TerraCommand::findBiome)))
                        .then(Commands.literal("terrain")
                                .then(Commands.argument("terrain", TerrainArgType.terrain())
                                        .executes(TerraCommand::findTerrain)))
                        .then(Commands.literal("both")
                                .then(Commands.argument("biome", BiomeArgType.biome())
                                        .then(Commands.argument("terrain", TerrainArgType.terrain())
                                                .executes(TerraCommand::findTerrainAndBiome)))));
    }

    private static int query(CommandContext<CommandSource> context) throws CommandSyntaxException {
        getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        BlockPos pos = context.getSource().asPlayer().getPosition();
        TerraBiomeProvider biomeProvider = getBiomeProvider(context);
        try (Resource<Cell> cell = biomeProvider.lookupPos(pos.getX(), pos.getZ())) {
            Biome biome = biomeProvider.getBiome(cell.get(), pos.getX(), pos.getZ());
            context.getSource().sendFeedback(new StringTextComponent("At ")
                    .appendSibling(createTeleportMessage(pos))
                    .appendSibling(new StringTextComponent(": Terrain = "))
                    .appendSibling(createTitle(cell.get().terrain.getName()))
                    .appendSibling(new StringTextComponent(", Biome = "))
                    .appendSibling(createTitle(biome.getRegistryName().toString()))
                    .appendSibling(new StringTextComponent(", BiomeType = "))
                    .appendSibling(createTitle(cell.get().biomeType.name())),
                    false
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int dump(CommandContext<CommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(
                new StringTextComponent("Exporting data"),
                true
        );
        DataGen.dumpData();
        return Command.SINGLE_SUCCESS;
    }

    private static int setDefaults(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        context.getSource().sendFeedback(
                new StringTextComponent("Setting generator defaults"),
                true
        );
        SettingsHelper.exportDefaults(terraContext.terraSettings);
        return Command.SINGLE_SUCCESS;
    }

    private static int debugBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().asPlayer();
        BlockPos position = player.getPosition();
        int x = position.getX();
        int z = position.getZ();

        long seed = player.getServerWorld().getSeed();
        Biome actual = player.getServerWorld().getBiome(position);
        Biome biome2 = ColumnFuzzedBiomeMagnifier.INSTANCE.getBiome(seed, x, 0, z, player.getServerWorld().getWorldServer().getChunkProvider().getChunkGenerator().getBiomeProvider());

        context.getSource().sendFeedback(new StringTextComponent("At ")
                        .appendSibling(createTeleportMessage(position))
                        .appendSibling(new StringTextComponent(": Actual Biome = "))
                        .appendSibling(createTitle(actual.getRegistryName().toString()))
                        .appendSibling(new StringTextComponent(", Lookup Biome = "))
                        .appendSibling(createTitle(biome2.getRegistryName().toString())),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrain(CommandContext<CommandSource> context) throws CommandSyntaxException {
        // get the generator's context
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        Terrain terrain = TerrainArgType.getTerrain(context, "terrain");
        Terrain type = getTerrainInstance(terrain, terraContext.terrain);
        BlockPos pos = context.getSource().asPlayer().getPosition();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        WorldGenerator generator = terraContext.factory.get();
        Search search = new TerrainSearchTask(pos, type, getChunkGenerator(context), generator);
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                                         .appendSibling(new StringTextComponent(" Searching for "))
                                         .appendSibling(createTitle(type.getName()))
                                         .appendSibling(new StringTextComponent("..."))
                                         , false);

        return Command.SINGLE_SUCCESS;
    }

    private static int findBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        // get the generator's context
        getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        Biome biome = BiomeArgType.getBiome(context, "biome");
        BlockPos pos = context.getSource().asPlayer().getPosition();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        ServerWorld world = context.getSource().asPlayer().getServerWorld();
        Search search = new BiomeSearchTask(pos, biome, world.getChunkProvider().getChunkGenerator(), getBiomeProvider(context));
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                                         .appendSibling(new StringTextComponent(" Searching for "))
                                         .appendSibling(createTitle(biome.getRegistryName().toString()))
                                         .appendSibling(new StringTextComponent("..."))
                                         , false);

        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrainAndBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        // get the generator's context
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        Terrain terrain = TerrainArgType.getTerrain(context, "terrain");
        Terrain target = getTerrainInstance(terrain, terraContext.terrain);
        Biome biome = BiomeArgType.getBiome(context, "biome");
        BlockPos pos = context.getSource().asPlayer().getPosition();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        WorldGenerator generator = terraContext.factory.get();
        Search biomeSearch = new BiomeSearchTask(pos, biome, getChunkGenerator(context), getBiomeProvider(context));
        Search terrainSearch = new TerrainSearchTask(pos, target, getChunkGenerator(context), generator);
        Search search = new BothSearchTask(pos, biomeSearch, terrainSearch);
        int identifier = doSearch(server, playerID, search);
        context.getSource().sendFeedback(createPrefix(identifier)
                                         .appendSibling(new StringTextComponent(" Searching for "))
                                         .appendSibling(createTitle(biome.getRegistryName().toString()))
                                         .appendSibling(new StringTextComponent(" and "))
                                         .appendSibling(createTitle(target.getName()))
                                         .appendSibling(new StringTextComponent("..."))
                                         , false);

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
                player.sendMessage(createPrefix(identifier)
                                   .appendSibling(new StringTextComponent(" Location not found :[")));
                return;
            }

            double distance = Math.sqrt(player.getPosition().distanceSq(pos));

            ITextComponent result = createPrefix(identifier)
                                    .appendSibling(new StringTextComponent(" Nearest match: "))
                                    .appendSibling(createTeleportMessage(pos))
                                    .appendSibling(new StringTextComponent(String.format(" Distance: %.2f", distance)));

            player.sendMessage(result);
        }));
        return identifier;
    }

    private static Optional<TerraContext> getContext(CommandContext<CommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();
        DimensionType dimension = context.getSource().asPlayer().dimension;
        ChunkGenerator<?> generator = server.getWorld(dimension).getChunkProvider().getChunkGenerator();
        if (generator instanceof TerraChunkGenerator) {
            TerraChunkGenerator gen = (TerraChunkGenerator) generator;
            return Optional.of(gen.getContext());
        }
        return Optional.empty();
    }

    // the terrain parsed from the command will not be the same instance as used in the
    // world generator, so find the matching instance by name
    private static Terrain getTerrainInstance(Terrain find, Terrains terrains) {
        for (Terrain t : terrains.index) {
            if (t.getName().equals(find.getName())) {
                return t;
            }
        }
        return find;
    }

    private static ChunkGenerator<?> getChunkGenerator(CommandContext<CommandSource> context) {
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

    private static ITextComponent createTeleportMessage(BlockPos pos) {
        return TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent(
                "chat.coordinates", pos.getX(), pos.getY(), pos.getZ()
        )).applyTextStyle((style) -> style.setColor(TextFormatting.GREEN)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " " + pos.getY() + " " + pos.getZ()))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")))
        );
    }

    private static ITextComponent createPrefix(int identifier) {
        return new StringTextComponent("") // Gotta make sure parent style is default
                   .appendSibling(TextComponentUtils.wrapInSquareBrackets(new StringTextComponent(
                        String.format("%03d", identifier)
                   )).applyTextStyle(PREFIX_FORMAT));
    }

    private static ITextComponent createTitle(String name) {
        return new StringTextComponent("") // Gotta make sure parent style is default
                   .appendSibling(new StringTextComponent(name).applyTextStyle(TITLE_FORMAT));
    }
}
