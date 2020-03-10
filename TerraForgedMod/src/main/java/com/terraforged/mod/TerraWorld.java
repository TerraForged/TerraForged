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

package com.terraforged.mod;

import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.mod.biome.provider.BiomeProvider;
import com.terraforged.mod.chunk.ChunkGeneratorFactory;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.TerraGenSettings;
import com.terraforged.mod.chunk.test.TestChunkGenerator;
import com.terraforged.mod.gui.SettingsScreen;
import com.terraforged.mod.settings.SettingsHelper;
import com.terraforged.mod.settings.TerraSettings;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;

public class TerraWorld extends WorldType {
    public static final int VERSION = 1;

    private static final Set<WorldType> types = new HashSet<>();
    public static final TerraWorld TERRA = new TerraWorld("terraforged", TerraChunkGenerator::new);
    public static final TerraWorld TEST = new TerraWorld("terratest", TestChunkGenerator::new);

    private final ChunkGeneratorFactory<?> factory;

    public TerraWorld(String name, ChunkGeneratorFactory<?> factory) {
        super(name);
        this.factory = factory;
        setCustomOptions(true);
        TerraWorld.types.add(this);
    }

    @Override
    public double getHorizon(World world) {
        return 0;
    }

    @Override
    public float getCloudHeight() {
        return 260.0F;
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator(World world) {
        if (world.getDimension().getType() != DimensionType.OVERWORLD) {
            return world.getDimension().createChunkGenerator();
        }

        Log.debug("Creating {} generator", world.getDimension().getType());

        int version = SettingsHelper.getVersion(world.getWorldInfo());
        TerraSettings settings = SettingsHelper.getSettings(world);
        SettingsHelper.syncSettings(world.getWorldInfo(), settings, version);

        Terrains terrains = Terrains.create(settings);

        OverworldGenSettings genSettings = new TerraGenSettings(settings.structures);
        OverworldBiomeProviderSettings biomeSettings = new OverworldBiomeProviderSettings(world.getWorldInfo());
        biomeSettings.setGeneratorSettings(genSettings);
        world.getWorldInfo().setGeneratorOptions(NBTHelper.serializeCompact(settings));

        TerraContext context = new TerraContext(world, terrains, settings);
        BiomeProvider biomeProvider = new BiomeProvider(context);

        return getGeneratorFactory().create(context, biomeProvider, genSettings);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onCustomizeButton(Minecraft mc, CreateWorldScreen gui) {
        mc.displayGuiScreen(new SettingsScreen(gui));
    }

    public ChunkGeneratorFactory<?> getGeneratorFactory() {
        return factory;
    }

    public static void init() {
        Log.info("Registered world type(s)");
    }

    public static boolean isTerraWorld(IWorld world) {
        if (world instanceof World) {
            return types.contains(((World) world).getWorldType());
        }
        return false;
    }
}
