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

package com.terraforged.mod.worldgen.util.delegate;

import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipBlockStateContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.UpgradeData;
import net.minecraft.world.level.gameevent.GameEventDispatcher;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.blending.BlendingData;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickContainerAccess;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class DelegateChunk extends ChunkAccess {
    private static final LevelHeightAccessor ZERO_HEIGHT = new ZeroHeight();
    private static final Registry<Biome> EMPTY_REGISTRY = new MappedRegistry<>(Registry.BIOME_REGISTRY, Lifecycle.stable());

    protected ChunkAccess delegate;

    protected DelegateChunk() {
        super(ChunkPos.ZERO, UpgradeData.EMPTY, ZERO_HEIGHT, EMPTY_REGISTRY, 0L, null, null);
    }

    protected void set(ChunkAccess chunk) {
        this.delegate = chunk;
    }

    @Override
    public GameEventDispatcher getEventDispatcher(int p_156113_) {
        return delegate.getEventDispatcher(p_156113_);
    }

    @Override
    public BlockState setBlockState(BlockPos p_62087_, BlockState p_62088_, boolean p_62089_) {
        return delegate.setBlockState(p_62087_, p_62088_, p_62089_);
    }

    @Override
    public void setBlockEntity(BlockEntity p_156114_) {
        delegate.setBlockEntity(p_156114_);
    }

    @Override
    public void addEntity(Entity p_62078_) {
        delegate.addEntity(p_62078_);
    }

    @Override
    public LevelChunkSection getHighestSection() {
        return delegate.getHighestSection();
    }

    @Override
    public int getHighestSectionPosition() {
        return delegate.getHighestSectionPosition();
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos() {
        return delegate.getBlockEntitiesPos();
    }

    @Override
    public LevelChunkSection[] getSections() {
        return delegate.getSections();
    }

    @Override
    public LevelChunkSection getSection(int p_187657_) {
        return delegate.getSection(p_187657_);
    }

    @Override
    public Collection<Map.Entry<Heightmap.Types, Heightmap>> getHeightmaps() {
        return delegate.getHeightmaps();
    }

    @Override
    public void setHeightmap(Heightmap.Types p_62083_, long[] p_62084_) {
        delegate.setHeightmap(p_62083_, p_62084_);
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Types p_62079_) {
        return delegate.getOrCreateHeightmapUnprimed(p_62079_);
    }

    @Override
    public boolean hasPrimedHeightmap(Heightmap.Types p_187659_) {
        return delegate.hasPrimedHeightmap(p_187659_);
    }

    @Override
    public int getHeight(Heightmap.Types p_62080_, int p_62081_, int p_62082_) {
        return delegate.getHeight(p_62080_, p_62081_, p_62082_);
    }

    @Override
    public ChunkPos getPos() {
        return delegate.getPos();
    }

    @Override
    public StructureStart<?> getStartForFeature(StructureFeature<?> p_187648_) {
        return delegate.getStartForFeature(p_187648_);
    }

    @Override
    public void setStartForFeature(StructureFeature<?> p_187653_, StructureStart<?> p_187654_) {
        delegate.setStartForFeature(p_187653_, p_187654_);
    }

    @Override
    public Map<StructureFeature<?>, StructureStart<?>> getAllStarts() {
        return delegate.getAllStarts();
    }

    @Override
    public void setAllStarts(Map<StructureFeature<?>, StructureStart<?>> p_62090_) {
        delegate.setAllStarts(p_62090_);
    }

    @Override
    public LongSet getReferencesForFeature(StructureFeature<?> p_187661_) {
        return delegate.getReferencesForFeature(p_187661_);
    }

    @Override
    public void addReferenceForFeature(StructureFeature<?> p_187650_, long p_187651_) {
        delegate.addReferenceForFeature(p_187650_, p_187651_);
    }

    @Override
    public Map<StructureFeature<?>, LongSet> getAllReferences() {
        return delegate.getAllReferences();
    }

    @Override
    public void setAllReferences(Map<StructureFeature<?>, LongSet> p_187663_) {
        delegate.setAllReferences(p_187663_);
    }

    @Override
    public boolean isYSpaceEmpty(int p_62075_, int p_62076_) {
        return delegate.isYSpaceEmpty(p_62075_, p_62076_);
    }

    @Override
    public void setUnsaved(boolean p_62094_) {
        delegate.setUnsaved(p_62094_);
    }

    @Override
    public boolean isUnsaved() {
        return delegate.isUnsaved();
    }

    @Override
    public ChunkStatus getStatus() {
        return delegate.getStatus();
    }

    @Override
    public void removeBlockEntity(BlockPos p_62101_) {
        delegate.removeBlockEntity(p_62101_);
    }

    @Override
    public void markPosForPostprocessing(BlockPos p_62102_) {
        delegate.markPosForPostprocessing(p_62102_);
    }

    @Override
    public ShortList[] getPostProcessing() {
        return delegate.getPostProcessing();
    }

    @Override
    public void addPackedPostProcess(short p_62092_, int p_62093_) {
        delegate.addPackedPostProcess(p_62092_, p_62093_);
    }

    @Override
    public void setBlockEntityNbt(CompoundTag p_62091_) {
        delegate.setBlockEntityNbt(p_62091_);
    }

    @Override
    public CompoundTag getBlockEntityNbt(BlockPos p_62103_) {
        return delegate.getBlockEntityNbt(p_62103_);
    }

    @Override
    public CompoundTag getBlockEntityNbtForSaving(BlockPos p_62104_) {
        return delegate.getBlockEntityNbtForSaving(p_62104_);
    }

    @Override
    public Stream<BlockPos> getLights() {
        return delegate.getLights();
    }

    @Override
    public TickContainerAccess<Block> getBlockTicks() {
        return delegate.getBlockTicks();
    }

    @Override
    public TickContainerAccess<Fluid> getFluidTicks() {
        return delegate.getFluidTicks();
    }

    @Override
    public TicksToSave getTicksForSerialization() {
        return delegate.getTicksForSerialization();
    }

    @Override
    public UpgradeData getUpgradeData() {
        return delegate.getUpgradeData();
    }

    @Override
    public boolean isOldNoiseGeneration() {
        return delegate.isOldNoiseGeneration();
    }

    @Override
    public BlendingData getBlendingData() {
        return delegate.getBlendingData();
    }

    @Override
    public void setBlendingData(BlendingData p_187646_) {
        delegate.setBlendingData(p_187646_);
    }

    @Override
    public long getInhabitedTime() {
        return delegate.getInhabitedTime();
    }

    @Override
    public void incrementInhabitedTime(long p_187633_) {
        delegate.incrementInhabitedTime(p_187633_);
    }

    @Override
    public void setInhabitedTime(long p_62099_) {
        delegate.setInhabitedTime(p_62099_);
    }

    @Override
    public boolean isLightCorrect() {
        return delegate.isLightCorrect();
    }

    @Override
    public void setLightCorrect(boolean p_62100_) {
        delegate.setLightCorrect(p_62100_);
    }

    @Override
    public int getMinBuildHeight() {
        return delegate.getMinBuildHeight();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public NoiseChunk getOrCreateNoiseChunk(NoiseSampler p_187641_, Supplier<NoiseChunk.NoiseFiller> p_187642_, NoiseGeneratorSettings p_187643_, Aquifer.FluidPicker p_187644_, Blender p_187645_) {
        return delegate.getOrCreateNoiseChunk(p_187641_, p_187642_, p_187643_, p_187644_, p_187645_);
    }

    @Override
    @Deprecated
    public Biome carverBiome(Supplier<Biome> p_187656_) {
        return delegate.carverBiome(p_187656_);
    }

    @Override
    public Biome getNoiseBiome(int p_187671_, int p_187672_, int p_187673_) {
        return delegate.getNoiseBiome(p_187671_, p_187672_, p_187673_);
    }

    @Override
    public void fillBiomesFromNoise(BiomeResolver p_187638_, Climate.Sampler p_187639_) {
        delegate.fillBiomesFromNoise(p_187638_, p_187639_);
    }

    @Override
    public boolean hasAnyStructureReferences() {
        return delegate.hasAnyStructureReferences();
    }

    @Override
    public BelowZeroRetrogen getBelowZeroRetrogen() {
        return delegate.getBelowZeroRetrogen();
    }

    @Override
    public boolean isUpgrading() {
        return delegate.isUpgrading();
    }

    @Override
    public LevelHeightAccessor getHeightAccessorForGeneration() {
        return delegate.getHeightAccessorForGeneration();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos p_45570_) {
        return delegate.getBlockEntity(p_45570_);
    }

    @Override
    public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos p_151367_, BlockEntityType<T> p_151368_) {
        return delegate.getBlockEntity(p_151367_, p_151368_);
    }

    @Override
    public BlockState getBlockState(BlockPos p_45571_) {
        return delegate.getBlockState(p_45571_);
    }

    @Override
    public FluidState getFluidState(BlockPos p_45569_) {
        return delegate.getFluidState(p_45569_);
    }

    @Override
    public int getLightEmission(BlockPos p_45572_) {
        return delegate.getLightEmission(p_45572_);
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
    }

    @Override
    public Stream<BlockState> getBlockStates(AABB p_45557_) {
        return delegate.getBlockStates(p_45557_);
    }

    @Override
    public BlockHitResult isBlockInLine(ClipBlockStateContext p_151354_) {
        return delegate.isBlockInLine(p_151354_);
    }

    @Override
    public BlockHitResult clip(ClipContext p_45548_) {
        return delegate.clip(p_45548_);
    }

    @Override
    public BlockHitResult clipWithInteractionOverride(Vec3 p_45559_, Vec3 p_45560_, BlockPos p_45561_, VoxelShape p_45562_, BlockState p_45563_) {
        return delegate.clipWithInteractionOverride(p_45559_, p_45560_, p_45561_, p_45562_, p_45563_);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape p_45565_, Supplier<VoxelShape> p_45566_) {
        return delegate.getBlockFloorHeight(p_45565_, p_45566_);
    }

    @Override
    public double getBlockFloorHeight(BlockPos p_45574_) {
        return delegate.getBlockFloorHeight(p_45574_);
    }

    @Override
    public int getMaxBuildHeight() {
        return delegate.getMaxBuildHeight();
    }

    @Override
    public int getSectionsCount() {
        return delegate.getSectionsCount();
    }

    @Override
    public int getMinSection() {
        return delegate.getMinSection();
    }

    @Override
    public int getMaxSection() {
        return delegate.getMaxSection();
    }

    @Override
    public boolean isOutsideBuildHeight(BlockPos p_151571_) {
        return delegate.isOutsideBuildHeight(p_151571_);
    }

    @Override
    public boolean isOutsideBuildHeight(int p_151563_) {
        return delegate.isOutsideBuildHeight(p_151563_);
    }

    @Override
    public int getSectionIndex(int p_151565_) {
        return delegate.getSectionIndex(p_151565_);
    }

    @Override
    public int getSectionIndexFromSectionY(int p_151567_) {
        return delegate.getSectionIndexFromSectionY(p_151567_);
    }

    @Override
    public int getSectionYFromSectionIndex(int p_151569_) {
        return delegate.getSectionYFromSectionIndex(p_151569_);
    }

    private static class ZeroHeight implements LevelHeightAccessor {
        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public int getMinBuildHeight() {
            return 0;
        }
    }
}
