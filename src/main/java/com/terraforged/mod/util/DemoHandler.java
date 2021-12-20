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

package com.terraforged.mod.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.profiler.GeneratorProfiler;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class DemoHandler {
    public static void renderOverlay(PoseStack stack) {
        if (Environment.DEV_ENV) return;

        var client = Minecraft.getInstance();
        if (!client.isWindowActive()) return;

        var player = client.player;
        if (player == null) return;
        if (!isTFWorld(player.level)) return;

        var window = client.getWindow();
        var font = client.font;

        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();
        var text = "TerraForged Demo";

        int x = width - font.width(text) - 5;
        int y = height - font.lineHeight - 5;

        font.drawShadow(stack, text, x, y, 0xFFFF0000);
    }

    public static void warn(Player player) {
        if (player == null || Environment.DEV_ENV) return;

        var chunkSource = player.level.getChunkSource();
        if (chunkSource instanceof ServerChunkCache serverSource && isTFGenerator(serverSource.getGenerator())) {
            player.sendMessage(new TextComponent(WARNING).withStyle(ChatFormatting.RED), Util.NIL_UUID);
        }
    }

    private static final String WARNING = "This version of TerraForged has been provided for demo purposes only and may not be suitable for survival play."
            + " Please do NOT report bugs, compatibility issues, or feedback regarding this version of the mod.";

    private static boolean isTFWorld(Level level) {
        return level.dimensionType().effectsLocation().getNamespace().equals(TerraForged.MODID);
    }

    private static boolean isTFGenerator(ChunkGenerator generator) {
        return generator instanceof GeneratorProfiler || generator instanceof Generator;
    }
}
