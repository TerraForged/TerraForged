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

package com.terraforged.mod.command.arg;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TerrainArgType implements ArgumentType<Terrain> {

    private final List<Terrain> terrains = getTerrains();

    @Override
    public Terrain parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readString();
        for (Terrain terrain : terrains) {
            if (terrain.getName().equalsIgnoreCase(name)) {
                return terrain;
            }
        }
        reader.setCursor(cursor);
        throw createException("Invalid terrain", "%s is not a valid terrain type", name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder suggestions) {
        return ISuggestionProvider.suggest(terrains.stream().map(Terrain::getName).collect(Collectors.toList()), suggestions);
    }

    public static ArgumentType<Terrain> terrain() {
        return new TerrainArgType();
    }

    public static <S> Terrain getTerrain(CommandContext<S> context, String name) {
        return context.getArgument(name, Terrain.class);
    }

    private static CommandSyntaxException createException(String type, String message, Object... args) {
        return new CommandSyntaxException(
                new SimpleCommandExceptionType(new StringTextComponent(type)),
                new StringTextComponent(String.format(message, args))
        );
    }

    private static List<Terrain> getTerrains() {
        Terrains terrains = Terrains.create(new Settings());
        List<Terrain> result = new ArrayList<>();

        for (int i = 0; i < terrains.index.size(); i++) {
            Terrain terrain = terrains.index.get(i);
            result.add(terrain);
            if (dontMix(terrain, terrains)) {
                continue;
            }
            for (int j = i + 1; j < terrains.index.size(); j++) {
                Terrain other = terrains.index.get(j);
                if (dontMix(other, terrains)) {
                    continue;
                }
                Terrain mix = new Terrain(terrain.getName() + "-" + other.getName(), -1);
                result.add(mix);
            }
        }

        return result;
    }

    private static boolean dontMix(Terrain terrain, Terrains terrains) {
        return terrain == terrains.ocean
                || terrain == terrains.deepOcean
                || terrain == terrains.river
                || terrain == terrains.riverBanks
                || terrain == terrains.beach
                || terrain == terrains.coast
                || terrain == terrains.volcano
                || terrain == terrains.volcanoPipe
                || terrain == terrains.lake;
    }
}
