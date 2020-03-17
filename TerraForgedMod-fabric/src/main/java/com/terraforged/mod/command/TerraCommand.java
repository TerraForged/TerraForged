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

package com.terraforged.mod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.WorldGenerator;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.mod.biome.provider.BiomeProvider;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.command.arg.BiomeArgType;
import com.terraforged.mod.command.arg.TerrainArgType;
import com.terraforged.mod.command.search.BiomeSearchTask;
import com.terraforged.mod.command.search.BothSearchTask;
import com.terraforged.mod.command.search.Search;
import com.terraforged.mod.command.search.TerrainSearchTask;
import com.terraforged.mod.data.DataGen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.HorizontalVoronoiBiomeAccessType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class TerraCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(command());
    }

    private static LiteralArgumentBuilder<ServerCommandSource> command() {
        return CommandManager.literal("terra")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("query")
                        .executes(TerraCommand::query))
                .then(CommandManager.literal("data")
                        .then(CommandManager.literal("dump")
                                .executes(TerraCommand::dump)))
                .then(CommandManager.literal("debug")
                        .executes(TerraCommand::debugBiome))
                .then(CommandManager.literal("locate")
                        .then(CommandManager.argument("biome", BiomeArgType.biome())
                                .executes(TerraCommand::findBiome)
                                .then(CommandManager.argument("terrain", TerrainArgType.terrain())
                                        .executes(TerraCommand::findTerrainAndBiome)))
                        .then(CommandManager.argument("terrain", TerrainArgType.terrain())
                                .executes(TerraCommand::findTerrain)
                                .then(CommandManager.argument("biome", BiomeArgType.biome())
                                        .executes(TerraCommand::findTerrainAndBiome))));
    }

    private static int query(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        BlockPos pos = context.getSource().getPlayer().getSenseCenterPos();
        BiomeProvider biomeProvider = getBiomeProvider(context);
        Cell<Terrain> cell = biomeProvider.lookupPos(pos.getX(), pos.getZ());
        Biome biome = biomeProvider.getBiome(cell, pos.getX(), pos.getZ());
        context.getSource().sendFeedback(
                new LiteralText("Terrain=" + cell.tag.getName() + ", Biome=" + Registry.BIOME.getId(biome)),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int dump(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(
                new LiteralText("Exporting data"),
                true
        );
        DataGen.dumpData();
        return Command.SINGLE_SUCCESS;
    }

    private static int debugBiome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        BlockPos position = player.getSenseCenterPos();
        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();

        long seed = player.getServerWorld().getSeed();
        Biome actual = player.getServerWorld().getBiome(position);
        Biome biome2 = HorizontalVoronoiBiomeAccessType.INSTANCE
                .getBiome(seed, x, 0, z, player.getServerWorld().getChunkManager().getChunkGenerator().getBiomeSource());

        context.getSource().sendFeedback(new LiteralText(
                        "Actual Biome = " + Registry.BIOME.getId(actual)
                                + "\nLookup Biome = " + Registry.BIOME.getId(biome2)),
                false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrain(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // get the generator's context
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        Terrain terrain = TerrainArgType.getTerrain(context, "terrain");
        Terrain target = getTerrainInstance(terrain, terraContext.terrain);
        BlockPos pos = context.getSource().getPlayer().getSenseCenterPos();
        UUID playerID = context.getSource().getPlayer().getUuid();
        MinecraftServer server = context.getSource().getMinecraftServer();
        WorldGenerator worldGenerator = terraContext.factory.get();
        Search search = new TerrainSearchTask(pos, worldGenerator, target);
        doSearch(server, playerID, search);
        context.getSource().sendFeedback(new LiteralText("Searching..."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int findBiome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // get the generator's context
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        Biome biome = BiomeArgType.getBiome(context, "biome");
        BlockPos pos = context.getSource().getPlayer().getSenseCenterPos();
        UUID playerID = context.getSource().getPlayer().getUuid();
        MinecraftServer server = context.getSource().getMinecraftServer();
        ServerWorld reader = context.getSource().getPlayer().getServerWorld();
        Search search = new BiomeSearchTask(pos, reader, biome);
        doSearch(server, playerID, search);
        context.getSource().sendFeedback(new LiteralText("Searching..."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int findTerrainAndBiome(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        // get the generator's context
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        Terrain terrain = TerrainArgType.getTerrain(context, "terrain");
        Terrain target = getTerrainInstance(terrain, terraContext.terrain);
        Biome biome = BiomeArgType.getBiome(context, "biome");
        BlockPos pos = context.getSource().getPlayer().getSenseCenterPos();
        ServerWorld world = context.getSource().getPlayer().getServerWorld();
        UUID playerID = context.getSource().getPlayer().getUuid();
        MinecraftServer server = context.getSource().getMinecraftServer();
        WorldGenerator worldGenerator = terraContext.factory.get();
        Search biomeSearch = new BiomeSearchTask(pos, world, biome);
        Search terrainSearch = new TerrainSearchTask(pos, worldGenerator, target);
        Search search = new BothSearchTask(pos, biomeSearch, terrainSearch);
        doSearch(server, playerID, search);
        context.getSource().sendFeedback(new LiteralText("Searching..."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static void doSearch(MinecraftServer server, UUID userId, Supplier<BlockPos> supplier) {
        CompletableFuture.supplyAsync(supplier).thenAcceptAsync(pos -> {
            PlayerEntity player = server.getPlayerManager().getPlayer(userId);
            if (player == null) {
                return;
            }

            if (pos.getX() == 0 && pos.getZ() == 0) {
                player.sendMessage(new LiteralText("Location not found :["));
                return;
            }

            Text result = new LiteralText("Nearest match: ")
                    .append(createTeleportMessage(pos));

            player.sendMessage(result);
        }, server);
    }

    private static Optional<TerraContext> getContext(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getMinecraftServer();
        DimensionType dimension = context.getSource().getPlayer().dimension;
        ChunkGenerator<?> generator = server.getWorld(dimension).getChunkManager().getChunkGenerator();
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

    private static BiomeProvider getBiomeProvider(CommandContext<ServerCommandSource> context) {
        return (BiomeProvider) context.getSource().getWorld().getChunkManager().getChunkGenerator().getBiomeSource();
    }

    private static CommandSyntaxException createException(String type, String message, Object... args) {
        return new CommandSyntaxException(
                new SimpleCommandExceptionType(new LiteralText(type)),
                new LiteralText(String.format(message, args))
        );
    }

    private static Text createTeleportMessage(BlockPos pos) {
        return Texts.bracketed(new TranslatableText(
                "chat.coordinates", pos.getX(), "~", pos.getZ()
        )).styled((style) -> style.setColor(Formatting.GREEN)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " ~ " + pos.getZ()))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.coordinates.tooltip")))
        );
    }
}
