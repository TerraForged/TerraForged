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

package com.terraforged.mod.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.data.gen.DataGen;
import com.terraforged.mod.worldgen.GeneratorPreset;
import com.terraforged.mod.worldgen.Regenerator;
import com.terraforged.mod.worldgen.datapack.DataPackExporter;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;

public class TFCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(getLocateTerrainCommand());
        dispatcher.register(getTFCommand());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> root(String name) {
        return Commands.literal(name).requires(s -> s.hasPermission(2));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getLocateTerrainCommand() {
        return root("locateterrain")
                .then(Arg.terrainType()
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                .executes(c -> locate(c, true)))
                        .executes(c -> locate(c, false)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> getTFCommand() {
        return root("tf")
                .then(Commands.literal("export")
                        .then(Commands.literal("structures")
                                .executes(TFCommands::export)))
                .then(Commands.literal("regen")
                        .then(net.minecraft.commands.Commands.argument("radius", IntegerArgumentType.integer(1))
                                .executes(TFCommands::regen)));
    }

    private static int regen(CommandContext<CommandSourceStack> context) {
        try {
            int radius = IntegerArgumentType.getInteger(context, "radius");
            var pos = context.getSource().getPosition();
            var chunk = new ChunkPos(((int) pos.x) >> 4, ((int) pos.z) >> 4);
            Regenerator.regenerateChunks(chunk, radius, context.getSource().getLevel(), context.getSource());

            return Command.SINGLE_SUCCESS;
        } catch (Throwable t) {
            t.printStackTrace();
            return Command.SINGLE_SUCCESS;
        }
    }

    private static int export(CommandContext<CommandSourceStack> context) {
        var access = context.getSource().registryAccess();
        var structures = context.getSource().getLevel().getChunkSource().getGenerator().getSettings();
        DataGen.exportStructureConfigs(DataPackExporter.DEFAULT_PACK_DIR, structures, access);

        var result = new TextComponent("Exported structure settings")
                .withStyle(s -> s.withColor(ChatFormatting.GREEN));

        context.getSource().sendSuccess(result, false);

        return Command.SINGLE_SUCCESS;
    }

    private static int locate(CommandContext<CommandSourceStack> context, boolean withRadius) throws CommandSyntaxException {
        var generator = GeneratorPreset.getGenerator(context.getSource().getLevel());
        if (generator == null) return Command.SINGLE_SUCCESS;

        String name = StringArgumentType.getString(context, "terrain");
        var terrain = TerrainType.get(name);
        int radius = withRadius ? IntegerArgumentType.getInteger(context, "radius") : 1;

        var player = context.getSource().getPlayerOrException();
        var at = player.blockPosition();

        Component result;
        if (terrain == null) {
            result = new TextComponent("Invalid terrain: " + name).withStyle(ChatFormatting.RED);
        } else {
            int maxRadius = Math.min(100, radius + 50);
            long pos = generator.getNoiseGenerator().find(at.getX(), at.getZ(), radius, maxRadius, terrain);

            if (pos == 0L) {
                result = new TextComponent("Unable to locate terrain: " + name).withStyle(ChatFormatting.RED);
            } else {
                int x = PosUtil.unpackLeft(pos);
                int z = PosUtil.unpackRight(pos);
                int y = generator.getFirstFreeHeight(x, z, Heightmap.Types.MOTION_BLOCKING, player.level);

                result = createTerrainTeleportMessage(at, x, y, z, terrain);
            }
        }

        player.sendMessage(result, ChatType.SYSTEM, Util.NIL_UUID);

        return Command.SINGLE_SUCCESS;
    }

    private static Component createTerrainTeleportMessage(BlockPos pos, int x, int y, int z, Terrain terrain) {
        double distance = Math.sqrt(pos.distSqr(x, y, z, true));
        String commandText = String.format("/tp %s %s %s", x, y, z);
        String distanceText = String.format("%.1f", distance);
        String positionText = String.format("%s;%s;%s", x, y, z);
        return new TextComponent("Found terrain: ").withStyle(ChatFormatting.GREEN)
                .append(new TextComponent(terrain.getName()).withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent(" Distance: ").withStyle(ChatFormatting.GREEN))
                .append(new TextComponent(distanceText).withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent(". ").withStyle(ChatFormatting.GREEN))
                .append(new TextComponent("Teleport")
                        .withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE)
                        .withStyle(style -> style
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, commandText))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new TextComponent("Location: ").withStyle(ChatFormatting.GREEN)
                                                .append(new TextComponent(positionText).withStyle(ChatFormatting.YELLOW))))));
    }
}
