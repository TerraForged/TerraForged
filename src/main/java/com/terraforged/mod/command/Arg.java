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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.registry.ModRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class Arg {
    public static RequiredArgumentBuilder<CommandSourceStack, String> terrainType() {
        return Commands.argument("terrain", StringArgumentType.string()).suggests((context, builder) -> {
            var registries = context.getSource().getServer().registryAccess();
            var terrainTypes = registries.ownedRegistry(ModRegistry.TERRAIN_TYPE.get());
            if (terrainTypes.isEmpty()) {
                TerrainType.forEach(type -> builder.suggest(type.getName()));
            } else {
                terrainTypes.get().forEach(type -> builder.suggest(type.getName()));
            }
            return builder.buildFuture();
        });
    }
}
