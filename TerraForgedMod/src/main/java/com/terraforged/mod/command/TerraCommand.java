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
import com.terraforged.mod.Log;
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
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TerraCommand {

    @SubscribeEvent
    public static void start(FMLServerStartingEvent event) {
        Log.info("Registering find command!");
        register(event.getCommandDispatcher());
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
                .then(Commands.literal("locate")
                        .then(Commands.argument("biome", BiomeArgType.biome())
                                .executes(TerraCommand::findBiome)
                                .then(Commands.argument("terrain", TerrainArgType.terrain())
                                        .executes(TerraCommand::findTerrainAndBiome)))
                        .then(Commands.argument("terrain", TerrainArgType.terrain())
                                .executes(TerraCommand::findTerrain)
                                .then(Commands.argument("biome", BiomeArgType.biome())
                                        .executes(TerraCommand::findTerrainAndBiome))));
    }

    private static int query(CommandContext<CommandSource> context) throws CommandSyntaxException {
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        BlockPos pos = context.getSource().asPlayer().getPosition();
        WorldGenerator worldGenerator = terraContext.factory.get();
        BiomeProvider biomeProvider = getBiomeProvider(context);
        Cell<Terrain> cell = new Cell<>();
        worldGenerator.getHeightmap().apply(cell, pos.getX(), pos.getZ());
        Biome biome = biomeProvider.getBiome(cell, pos.getX(), pos.getZ());
        context.getSource().sendFeedback(
                new StringTextComponent("Terrain=" + cell.tag.getName() + ", Biome=" + biome.getRegistryName()),
                false
        );

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

    private static int findTerrain(CommandContext<CommandSource> context) throws CommandSyntaxException {
        // get the generator's context
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        Terrain terrain = TerrainArgType.getTerrain(context, "terrain");
        Terrain target = getTerrainInstance(terrain, terraContext.terrain);
        BlockPos pos = context.getSource().asPlayer().getPosition();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        WorldGenerator worldGenerator = terraContext.factory.get();
        Search search = new TerrainSearchTask(pos, worldGenerator, target);
        doSearch(server, playerID, search);
        context.getSource().sendFeedback(new StringTextComponent("Searching..."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static int findBiome(CommandContext<CommandSource> context) throws CommandSyntaxException {
        // get the generator's context
        TerraContext terraContext = getContext(context).orElseThrow(() -> createException(
                "Invalid world type",
                "This command can only be run in a TerraForged world!"
        ));

        Biome biome = BiomeArgType.getBiome(context, "biome");
        BlockPos pos = context.getSource().asPlayer().getPosition();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        IWorldReader reader = context.getSource().asPlayer().getServerWorld();
        Search search = new BiomeSearchTask(pos, reader, biome);
        doSearch(server, playerID, search);
        context.getSource().sendFeedback(new StringTextComponent("Searching..."), false);

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
        IWorldReader world = context.getSource().asPlayer().getServerWorld();
        UUID playerID = context.getSource().asPlayer().getUniqueID();
        MinecraftServer server = context.getSource().getServer();
        WorldGenerator worldGenerator = terraContext.factory.get();
        Search biomeSearch = new BiomeSearchTask(pos, world, biome);
        Search terrainSearch = new TerrainSearchTask(pos, worldGenerator, target);
        Search search = new BothSearchTask(pos, biomeSearch, terrainSearch);
        doSearch(server, playerID, search);
        context.getSource().sendFeedback(new StringTextComponent("Searching..."), false);

        return Command.SINGLE_SUCCESS;
    }

    private static void doSearch(MinecraftServer server, UUID userId, Supplier<BlockPos> supplier) {
        CompletableFuture.supplyAsync(supplier).thenAccept(pos -> server.deferTask(() -> {
            PlayerEntity player = server.getPlayerList().getPlayerByUUID(userId);
            if (player == null) {
                return;
            }

            if (pos.getX() == 0 && pos.getZ() == 0) {
                player.sendMessage(new StringTextComponent("Location not found :["));
                return;
            }

            ITextComponent result = new StringTextComponent("Nearest match: ")
                    .appendSibling(createTeleportMessage(pos));

            player.sendMessage(result);
        }));
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

    private static BiomeProvider getBiomeProvider(CommandContext<CommandSource> context) {
        return (BiomeProvider) context.getSource().getWorld().getChunkProvider().getChunkGenerator().getBiomeProvider();
    }

    private static CommandSyntaxException createException(String type, String message, Object... args) {
        return new CommandSyntaxException(
                new SimpleCommandExceptionType(new StringTextComponent(type)),
                new StringTextComponent(String.format(message, args))
        );
    }

    private static ITextComponent createTeleportMessage(BlockPos pos) {
        return TextComponentUtils.wrapInSquareBrackets(new TranslationTextComponent(
                "chat.coordinates", pos.getX(), "~", pos.getZ()
        )).applyTextStyle((style) -> style.setColor(TextFormatting.GREEN)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " ~ " + pos.getZ()))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("chat.coordinates.tooltip")))
        );
    }
}
