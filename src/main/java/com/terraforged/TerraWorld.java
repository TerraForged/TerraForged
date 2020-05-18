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

package com.terraforged;

import com.terraforged.biome.provider.BiomeProvider;
import com.terraforged.chunk.ChunkGeneratorFactory;
import com.terraforged.chunk.TerraChunkGenerator;
import com.terraforged.chunk.TerraContext;
import com.terraforged.chunk.TerraGenSettings;
import com.terraforged.chunk.test.TestChunkGenerator;
import com.terraforged.gui.SettingsScreen;
import com.terraforged.settings.DimesionSettings;
import com.terraforged.settings.SettingsHelper;
import com.terraforged.settings.TerraSettings;
import com.terraforged.util.Environment;
import com.terraforged.util.nbt.NBTHelper;
import com.terraforged.world.terrain.TerrainTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.provider.OverworldBiomeProviderSettings;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.OverworldGenSettings;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class TerraWorld extends WorldType {

    private static final Set<WorldType> types = new HashSet<>();

    private final ChunkGeneratorFactory<?> factory;

    public TerraWorld(String name, ChunkGeneratorFactory<?> factory) {
        super(name);
        this.factory = factory;
        setCustomOptions(true);
        TerraWorld.types.add(this);
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator(World world) {
        if (world.getDimension().getType() == DimensionType.OVERWORLD) {
            WorldInfo info = world.getWorldInfo();
            TerraSettings settings = SettingsHelper.getSettings(info);
            settings.world.seed = world.getSeed();
            settings.dimensions.dimensions.apply(world.getWorldInfo());

            TerrainTypes terrains = TerrainTypes.create(settings);

            OverworldGenSettings genSettings = new TerraGenSettings(settings.structures);
            OverworldBiomeProviderSettings biomeSettings = new OverworldBiomeProviderSettings(world.getWorldInfo());
            biomeSettings.setGeneratorSettings(genSettings);
            world.getWorldInfo().setGeneratorOptions(NBTHelper.serializeCompact(settings));

            TerraContext context = new TerraContext(world, terrains, settings);
            BiomeProvider biomeProvider = new BiomeProvider(context);

            Log.debug("Creating Terra {} generator", world.getDimension().getType().getRegistryName());
            return factory.create(context, biomeProvider, genSettings);
        }

        if (world.getDimension().getType() == DimensionType.THE_NETHER) {
            WorldType type = DimesionSettings.getWorldType(world.getWorldInfo(), DimensionType.THE_NETHER);
            Log.debug("Creating {} {} generator", type.getName(), world.getDimension().getType().getRegistryName());
            return type.createChunkGenerator(world);
        }

        if (world.getDimension().getType() == DimensionType.THE_END) {
            WorldType type = DimesionSettings.getWorldType(world.getWorldInfo(), DimensionType.THE_END);
            Log.debug("Creating {} {} generator", type.getName(), world.getDimension().getType().getRegistryName());
            return type.createChunkGenerator(world);
        }

        return super.createChunkGenerator(world);
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
    @OnlyIn(Dist.CLIENT)
    public void onCustomizeButton(Minecraft mc, CreateWorldScreen gui) {
        mc.displayGuiScreen(new SettingsScreen(gui));
    }

    public static void init() {
        Log.info("Registered world type");
        new TerraWorld("terraforged", TerraChunkGenerator::new);
        if (Environment.isDev()) {
            Log.info("Registered developer world type");
            new TerraWorld("terratest", TestChunkGenerator::new);
        }
    }

    public static void forEach(Consumer<WorldType> consumer) {
        types.forEach(consumer);
    }

    public static boolean isTerraType(WorldType type) {
        return types.contains(type);
    }

    public static boolean isTerraWorld(IWorld world) {
        if (world instanceof World) {
            return isTerraType(((World) world).getWorldType());
        }
        return false;
    }
}
