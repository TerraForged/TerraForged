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

package com.terraforged.api.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Heightmap;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.gen.GenerationStep;

import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface ChunkDelegate extends Chunk {

    Chunk getDelegate();

    @Override
    //@Nullable
    default BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        return getDelegate().setBlockState(pos, state, isMoving);
    }

    @Override
    default void setBlockEntity(BlockPos pos, BlockEntity tileEntityIn) {
        getDelegate().setBlockEntity(pos, tileEntityIn);
    }

    @Override
    default void addEntity(Entity entityIn) {
        getDelegate().addEntity(entityIn);
    }

    @Override
    //@Nullable
    default ChunkSection getHighestNonEmptySection() {
        return getDelegate().getHighestNonEmptySection();
    }

    @Override
    default int getHighestNonEmptySectionYOffset() {
        return getDelegate().getHighestNonEmptySectionYOffset();
    }

    @Override
    default Set<BlockPos> getBlockEntityPositions() {
        return getDelegate().getBlockEntityPositions();
    }

    @Override
    default ChunkSection[] getSectionArray() {
        return getDelegate().getSectionArray();
    }


    @Override
    default Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
        return getDelegate().getHeightmaps();
    }

    @Override
    default void setHeightmap(Heightmap.Type type, long[] data) {
        getDelegate().setHeightmap(type, data);
    }

    @Override
    default Heightmap getHeightmap(Heightmap.Type type) {
        return getDelegate().getHeightmap(type);
    }

    @Override
    default int sampleHeightmap(Heightmap.Type heightmapType, int x, int z) {
        return getDelegate().sampleHeightmap(heightmapType, x, z);
    }

    @Override
    default ChunkPos getPos() {
        return getDelegate().getPos();
    }

    @Override
    default void setLastSaveTime(long saveTime) {
        getDelegate().setLastSaveTime(saveTime);
    }

    @Override
    default Map<String, StructureStart> getStructureStarts() {
        return getDelegate().getStructureStarts();
    }

    @Override
    default void setStructureStarts(Map<String, StructureStart> structureStartsIn) {
        getDelegate().setStructureStarts(structureStartsIn);
    }

    @Override
    default boolean method_12228(int startY, int endY) {
        return getDelegate().method_12228(startY, endY);
    }

    @Override
    //@Nullable
    default BiomeArray getBiomeArray() {
        return getDelegate().getBiomeArray();
    }

    @Override
    default void setLightOn(boolean modified) {
        getDelegate().setLightOn(modified);
    }

    @Override
    default boolean isLightOn() {
        return getDelegate().isLightOn();
    }

    @Override
    default ChunkStatus getStatus() {
        return getDelegate().getStatus();
    }

    @Override
    default void removeBlockEntity(BlockPos pos) {
        getDelegate().removeBlockEntity(pos);
    }

    @Override
    default void markBlockForPostProcessing(BlockPos pos) {
        getDelegate().markBlockForPostProcessing(pos);
    }

    @Override
    default ShortList[] getPostProcessingLists() {
        return getDelegate().getPostProcessingLists();
    }

    @Override
    default void markBlockForPostProcessing(short packedPosition, int index) {
        getDelegate().markBlockForPostProcessing(packedPosition, index);
    }

    @Override
    default void addPendingBlockEntityTag(CompoundTag nbt) {
        getDelegate().addPendingBlockEntityTag(nbt);
    }

    @Override
    //@Nullable
    default CompoundTag method_20598(BlockPos pos) {
        return getDelegate().method_20598(pos);
    }

    @Override
    //@Nullable
    default CompoundTag getBlockEntityTagAt(BlockPos pos) {
        return getDelegate().getBlockEntityTagAt(pos);
    }

    @Override
    default Stream<BlockPos> getLightSourcesStream() {
        return getDelegate().getLightSourcesStream();
    }

    @Override
    default TickScheduler<Block> getBlockTickScheduler() {
        return getDelegate().getBlockTickScheduler();
    }

    @Override
    default TickScheduler<Fluid> getFluidTickScheduler() {
        return getDelegate().getFluidTickScheduler();
    }

    @Override
    default BitSet getCarvingMask(GenerationStep.Carver type) {
        return getDelegate().getCarvingMask(type);
    }

    @Override
    default void setShouldSave(boolean shouldSave) {
        getDelegate().setShouldSave(shouldSave);
    }

    @Override
    default boolean needsSaving() {
        return getDelegate().needsSaving();
    }

    @Override
    default void setStructureStart(String structure, StructureStart start) {
        getDelegate().setStructureStart(structure, start);
    }

    @Override
    default int getLuminance(BlockPos pos) {
        return getDelegate().getLuminance(pos);
    }

    @Override
    default UpgradeData getUpgradeData() {
        return getDelegate().getUpgradeData();
    }

    @Override
    default void setInhabitedTime(long inhabitedTime) {
        getDelegate().setInhabitedTime(inhabitedTime);
    }

    @Override
    default long getInhabitedTime() {
        return getDelegate().getInhabitedTime();
    }

    @Override
    default BlockEntity getBlockEntity(BlockPos pos) {
        return getDelegate().getBlockEntity(pos);
    }

    @Override
    default BlockState getBlockState(BlockPos pos) {
        return getDelegate().getBlockState(pos);
    }

    @Override
    default FluidState getFluidState(BlockPos pos) {
        return getDelegate().getFluidState(pos);
    }

    @Override
    default StructureStart getStructureStart(String structure) {
        return getDelegate().getStructureStart(structure);
    }

    @Override
    default LongSet getStructureReferences(String structure) {
        return getDelegate().getStructureReferences(structure);
    }

    @Override
    default void addStructureReference(String structure, long reference) {
        getDelegate().addStructureReference(structure, reference);
    }

    @Override
    default Map<String, LongSet> getStructureReferences() {
        return getDelegate().getStructureReferences();
    }

    @Override
    default void setStructureReferences(Map<String, LongSet> structureReferences) {
        getDelegate().setStructureReferences(structureReferences);
    }

    @Override
    default int getMaxLightLevel() {
        return getDelegate().getMaxLightLevel();
    }

    @Override
    default int getHeight() {
        return getDelegate().getHeight();
    }
}
