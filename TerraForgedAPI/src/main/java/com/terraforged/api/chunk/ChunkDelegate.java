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
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public interface ChunkDelegate extends IChunk {

    IChunk getDelegate();

    @Override
    @Nullable
    default BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        return getDelegate().setBlockState(pos, state, isMoving);
    }

    @Override
    default void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
        getDelegate().addTileEntity(pos, tileEntityIn);
    }

    @Override
    default void addEntity(Entity entityIn) {
        getDelegate().addEntity(entityIn);
    }

    @Override
    @Nullable
    default ChunkSection getLastExtendedBlockStorage() {
        return getDelegate().getLastExtendedBlockStorage();
    }

    @Override
    default int getTopFilledSegment() {
        return getDelegate().getTopFilledSegment();
    }

    @Override
    default Set<BlockPos> getTileEntitiesPos() {
        return getDelegate().getTileEntitiesPos();
    }

    @Override
    default ChunkSection[] getSections() {
        return getDelegate().getSections();
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
    default int getTopBlockY(Heightmap.Type heightmapType, int x, int z) {
        return getDelegate().getTopBlockY(heightmapType, x, z);
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
    default boolean isEmptyBetween(int startY, int endY) {
        return getDelegate().isEmptyBetween(startY, endY);
    }

    @Override
    @Nullable
    default BiomeContainer getBiomes() {
        return getDelegate().getBiomes();
    }

    @Override
    default void setModified(boolean modified) {
        getDelegate().setModified(modified);
    }

    @Override
    default boolean isModified() {
        return getDelegate().isModified();
    }

    @Override
    default ChunkStatus getStatus() {
        return getDelegate().getStatus();
    }

    @Override
    default void removeTileEntity(BlockPos pos) {
        getDelegate().removeTileEntity(pos);
    }

    @Override
    default void markBlockForPostprocessing(BlockPos pos) {
        getDelegate().markBlockForPostprocessing(pos);
    }

    @Override
    default ShortList[] getPackedPositions() {
        return getDelegate().getPackedPositions();
    }

    @Override
    default void func_201636_b(short packedPosition, int index) {
        getDelegate().func_201636_b(packedPosition, index);
    }

    @Override
    default void addTileEntity(CompoundNBT nbt) {
        getDelegate().addTileEntity(nbt);
    }

    @Override
    @Nullable
    default CompoundNBT getDeferredTileEntity(BlockPos pos) {
        return getDelegate().getDeferredTileEntity(pos);
    }

    @Override
    @Nullable
    default CompoundNBT getTileEntityNBT(BlockPos pos) {
        return getDelegate().getTileEntityNBT(pos);
    }

    @Override
    default Stream<BlockPos> getLightSources() {
        return getDelegate().getLightSources();
    }

    @Override
    default ITickList<Block> getBlocksToBeTicked() {
        return getDelegate().getBlocksToBeTicked();
    }

    @Override
    default ITickList<Fluid> getFluidsToBeTicked() {
        return getDelegate().getFluidsToBeTicked();
    }

    @Override
    default BitSet getCarvingMask(GenerationStage.Carving type) {
        return getDelegate().getCarvingMask(type);
    }

    @Override
    default UpgradeData getUpgradeData() {
        return getDelegate().getUpgradeData();
    }

    @Override
    default void setInhabitedTime(long newInhabitedTime) {
        getDelegate().setInhabitedTime(newInhabitedTime);
    }

    @Override
    default long getInhabitedTime() {
        return getDelegate().getInhabitedTime();
    }

    @Override
    default boolean hasLight() {
        return getDelegate().hasLight();
    }

    @Override
    default void setLight(boolean p_217305_1_) {
        getDelegate().setLight(p_217305_1_);
    }

    @Override
    @Nullable
    default IWorld getWorldForge() {
        return getDelegate().getWorldForge();
    }

    @Override
    @Nullable
    default TileEntity getTileEntity(BlockPos pos) {
        return getDelegate().getTileEntity(pos);
    }

    @Override
    default BlockState getBlockState(BlockPos pos) {
        return getDelegate().getBlockState(pos);
    }

    @Override
    default IFluidState getFluidState(BlockPos pos) {
        return getDelegate().getFluidState(pos);
    }

    @Override
    default int getLightValue(BlockPos pos) {
        return getDelegate().getLightValue(pos);
    }

    @Override
    default int getMaxLightLevel() {
        return getDelegate().getMaxLightLevel();
    }

    @Override
    default int getHeight() {
        return getDelegate().getHeight();
    }

    @Override
    default BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
        return getDelegate().rayTraceBlocks(context);
    }

    @Override
    @Nullable
    default BlockRayTraceResult rayTraceBlocks(Vec3d p_217296_1_, Vec3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_, BlockState p_217296_5_) {
        return getDelegate().rayTraceBlocks(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    @Nullable
    default StructureStart getStructureStart(String stucture) {
        return getDelegate().getStructureStart(stucture);
    }

    @Override
    default void putStructureStart(String structureIn, StructureStart structureStartIn) {
        getDelegate().putStructureStart(structureIn, structureStartIn);
    }

    @Override
    default LongSet getStructureReferences(String structureIn) {
        return getDelegate().getStructureReferences(structureIn);
    }

    @Override
    default void addStructureReference(String strucutre, long reference) {
        getDelegate().addStructureReference(strucutre, reference);
    }

    @Override
    default Map<String, LongSet> getStructureReferences() {
        return getDelegate().getStructureReferences();
    }

    @Override
    default void setStructureReferences(Map<String, LongSet> p_201606_1_) {
        getDelegate().setStructureReferences(p_201606_1_);
    }
}
