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

package com.terraforged.mod.server.command.arg;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TerrainArgType implements ArgumentType<Terrain> {

    @Override
    public Terrain parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();
        String name = reader.readString();
        Optional<Terrain> terrain = TerrainType.find(t -> t.getName().equalsIgnoreCase(name));
        if (terrain.isPresent()) {
            return terrain.get();
        }
        reader.setCursor(cursor);
        throw createException("Invalid terrain", "%s is not a valid terrain type", name);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder suggestions) {
        List<String> options = new ArrayList<>();
        TerrainType.forEach(t -> options.add(t.getName()));
        return ISuggestionProvider.suggest(options, suggestions);
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
}
