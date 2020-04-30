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

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.terraforged.core.world.terrain.Terrain;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.IArgumentSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TerrainArgType implements ArgumentType<Terrain> {

    private final List<Terrain> terrains;

    public TerrainArgType() {
        this(Terrain.getRegistered());
    }

    public TerrainArgType(List<Terrain> terrains) {
        this.terrains = terrains;
    }

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

    public static class Serializer implements IArgumentSerializer<TerrainArgType> {

        @Override
        public void write(TerrainArgType type, PacketBuffer buffer) {

        }

        @Override
        public TerrainArgType read(PacketBuffer buffer) {
            return new TerrainArgType();
        }

        @Override
        public void write(TerrainArgType type, JsonObject json) {

        }
    }
}
