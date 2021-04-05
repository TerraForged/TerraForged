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

package com.terraforged.mod.chunk.fix;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.ITickList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RegionDelegate extends WorldGenRegion {

    protected final WorldGenRegion delegate;

    public RegionDelegate(WorldGenRegion delegate) {
        super(delegate.getLevel(), Collections.singletonList(delegate.getChunk(delegate.getCenterX(), delegate.getCenterZ())));
        this.delegate = delegate;
    }

    public WorldGenRegion getDelegate() {
        return delegate;
    }

    // NOTE: do not override - getLevel is used in constructor
//    public ServerWorld getLevel()

    @Override
    public int getCenterX() {
        return delegate.getCenterX();
    }

    @Override
    public int getCenterZ() {
        return delegate.getCenterZ();
    }

    @Override
    public IChunk getChunk(int p_212866_1_, int p_212866_2_) {
        return delegate.getChunk(p_212866_1_, p_212866_2_);
    }

    @Override
    @Nullable
    public IChunk getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_) {
        return delegate.getChunk(p_217353_1_, p_217353_2_, p_217353_3_, p_217353_4_);
    }

    @Override
    public boolean hasChunk(int p_217354_1_, int p_217354_2_) {
        return delegate.hasChunk(p_217354_1_, p_217354_2_);
    }

    @Override
    public BlockState getBlockState(BlockPos p_180495_1_) {
        return delegate.getBlockState(p_180495_1_);
    }

    @Override
    public FluidState getFluidState(BlockPos p_204610_1_) {
        return delegate.getFluidState(p_204610_1_);
    }

    @Override
    @Nullable
    public PlayerEntity getNearestPlayer(double p_190525_1_, double p_190525_3_, double p_190525_5_, double p_190525_7_, Predicate<Entity> p_190525_9_) {
        return delegate.getNearestPlayer(p_190525_1_, p_190525_3_, p_190525_5_, p_190525_7_, p_190525_9_);
    }

    @Override
    public int getSkyDarken() {
        return delegate.getSkyDarken();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return delegate.getBiomeManager();
    }

    @Override
    public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return delegate.getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float getShade(Direction p_230487_1_, boolean p_230487_2_) {
        return delegate.getShade(p_230487_1_, p_230487_2_);
    }

    @Override
    public WorldLightManager getLightEngine() {
        return delegate.getLightEngine();
    }

    @Override
    public boolean destroyBlock(BlockPos p_241212_1_, boolean p_241212_2_, @Nullable Entity p_241212_3_, int p_241212_4_) {
        return delegate.destroyBlock(p_241212_1_, p_241212_2_, p_241212_3_, p_241212_4_);
    }

    @Override
    @Nullable
    public TileEntity getBlockEntity(BlockPos p_175625_1_) {
        return delegate.getBlockEntity(p_175625_1_);
    }

    @Override
    public boolean setBlock(BlockPos p_241211_1_, BlockState p_241211_2_, int p_241211_3_, int p_241211_4_) {
        return delegate.setBlock(p_241211_1_, p_241211_2_, p_241211_3_, p_241211_4_);
    }

    @Override
    public boolean addFreshEntity(Entity p_217376_1_) {
        return delegate.addFreshEntity(p_217376_1_);
    }

    @Override
    public boolean removeBlock(BlockPos p_217377_1_, boolean p_217377_2_) {
        return delegate.removeBlock(p_217377_1_, p_217377_2_);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public boolean isClientSide() {
        return delegate.isClientSide();
    }

    @Override
    public DynamicRegistries registryAccess() {
        return delegate.registryAccess();
    }

    @Override
    public IWorldInfo getLevelData() {
        return delegate.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos p_175649_1_) {
        return delegate.getCurrentDifficultyAt(p_175649_1_);
    }

    @Override
    public AbstractChunkProvider getChunkSource() {
        return delegate.getChunkSource();
    }

    @Override
    public long getSeed() {
        return delegate.getSeed();
    }

    @Override
    public ITickList<Block> getBlockTicks() {
        return delegate.getBlockTicks();
    }

    @Override
    public ITickList<Fluid> getLiquidTicks() {
        return delegate.getLiquidTicks();
    }

    @Override
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public Random getRandom() {
        return delegate.getRandom();
    }

    @Override
    public int getHeight(Heightmap.Type p_201676_1_, int p_201676_2_, int p_201676_3_) {
        return delegate.getHeight(p_201676_1_, p_201676_2_, p_201676_3_);
    }

    @Override
    public void playSound(@Nullable PlayerEntity p_184133_1_, BlockPos p_184133_2_, SoundEvent p_184133_3_, SoundCategory p_184133_4_, float p_184133_5_, float p_184133_6_) {
        delegate.playSound(p_184133_1_, p_184133_2_, p_184133_3_, p_184133_4_, p_184133_5_, p_184133_6_);
    }

    @Override
    public void addParticle(IParticleData p_195594_1_, double p_195594_2_, double p_195594_4_, double p_195594_6_, double p_195594_8_, double p_195594_10_, double p_195594_12_) {
        delegate.addParticle(p_195594_1_, p_195594_2_, p_195594_4_, p_195594_6_, p_195594_8_, p_195594_10_, p_195594_12_);
    }

    @Override
    public void levelEvent(@Nullable PlayerEntity p_217378_1_, int p_217378_2_, BlockPos p_217378_3_, int p_217378_4_) {
        delegate.levelEvent(p_217378_1_, p_217378_2_, p_217378_3_, p_217378_4_);
    }

    @Override
    public DimensionType dimensionType() {
        return delegate.dimensionType();
    }

    @Override
    public boolean isStateAtPosition(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return delegate.isStateAtPosition(p_217375_1_, p_217375_2_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> p_175647_1_, AxisAlignedBB p_175647_2_, @Nullable Predicate<? super T> p_175647_3_) {
        return delegate.getEntitiesOfClass(p_175647_1_, p_175647_2_, p_175647_3_);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity p_175674_1_, AxisAlignedBB p_175674_2_, @Nullable Predicate<? super Entity> p_175674_3_) {
        return delegate.getEntities(p_175674_1_, p_175674_2_, p_175674_3_);
    }

    @Override
    public List<PlayerEntity> players() {
        return delegate.players();
    }

    @Override
    public Stream<? extends StructureStart<?>> startsForFeature(SectionPos p_241827_1_, Structure<?> p_241827_2_) {
        return delegate.startsForFeature(p_241827_1_, p_241827_2_);
    }
}
