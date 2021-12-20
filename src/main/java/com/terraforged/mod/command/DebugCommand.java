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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.profiler.GeneratorProfiler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.levelgen.Heightmap;

public class DebugCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("locateterrain")
                .then(Commands.argument("terrain", StringArgumentType.string())
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                .executes(c -> locate(c, true)))
                        .executes(c -> locate(c, false))));
    }

    private static int locate(CommandContext<CommandSourceStack> context, boolean withRadius) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        var source = player.level.getChunkSource();
        if (source instanceof ServerChunkCache serverSource) {
            var chunkGenerator = serverSource.getGenerator();
            if (chunkGenerator instanceof GeneratorProfiler profiler) {
                chunkGenerator = profiler.getGenerator();
            }

            if (chunkGenerator instanceof Generator generator) {
                var at = player.blockPosition();

                String name = StringArgumentType.getString(context, "terrain");
                var terrain = TerrainType.get(name);

                Component result;
                if (terrain == null) {
                    result = new TextComponent("Invalid terrain: " + name).withStyle(ChatFormatting.RED);
                } else {
                    int radius = withRadius ? IntegerArgumentType.getInteger(context, "radius") : 1;
                    int maxRadius = Math.min(100, radius + 50);
                    long pos = generator.getNoiseGenerator().find(at.getX(), at.getZ(), radius, maxRadius, terrain);

                    if (pos == 0L) {
                        result = new TextComponent("Unable to locate terrain: " + name).withStyle(ChatFormatting.RED);
                    } else {
                        int x = PosUtil.unpackLeft(pos);
                        int z = PosUtil.unpackRight(pos);
                        int y = player.level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) + 1;

                        result = createTerrainTeleportMessage(at, x, y, z, terrain);
                    }
                }

                player.sendMessage(result, ChatType.SYSTEM, Util.NIL_UUID);
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static Component createTerrainTeleportMessage(BlockPos pos, int x, int y, int z, Terrain terrain) {
        double distance = Math.sqrt(pos.distSqr(x, y, z, true));
        String commandText = String.format("/tp %s %s %s", x, y, z);
        String distanceText = String.format("%.1f", distance);
        String positionText=  String.format("%s;%s;%s", x, y, z);
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
