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

package com.terraforged.mod.featuremanager.util.delegate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.DimensionType;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class WorldDelegate<T extends IWorld> implements IWorld {

    protected T delegate;

    public WorldDelegate(T delegate) {
        this.delegate = delegate;
    }

    public T getDelegate() {
        return delegate;
    }

    public void setDelegate(T delegate) {
        this.delegate = delegate;
    }

    @Override
    public long dayTime() {
        return delegate.dayTime();
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
    public IWorldInfo getLevelData() {
        return delegate.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos p_175649_1_) {
        return delegate.getCurrentDifficultyAt(p_175649_1_);
    }

    @Override
    public Difficulty getDifficulty() {
        return delegate.getDifficulty();
    }

    @Override
    public AbstractChunkProvider getChunkSource() {
        return delegate.getChunkSource();
    }

    @Override
    public boolean hasChunk(int p_217354_1_, int p_217354_2_) {
        return delegate.hasChunk(p_217354_1_, p_217354_2_);
    }

    @Override
    public Random getRandom() {
        return delegate.getRandom();
    }

    @Override
    public void blockUpdated(BlockPos p_230547_1_, Block p_230547_2_) {
        delegate.blockUpdated(p_230547_1_, p_230547_2_);
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
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public void levelEvent(int p_217379_1_, BlockPos p_217379_2_, int p_217379_3_) {
        delegate.levelEvent(p_217379_1_, p_217379_2_, p_217379_3_);
    }

    @Override
    public Stream<VoxelShape> getEntityCollisions(@Nullable Entity p_230318_1_, AxisAlignedBB p_230318_2_, Predicate<Entity> p_230318_3_) {
        return delegate.getEntityCollisions(p_230318_1_, p_230318_2_, p_230318_3_);
    }

    @Override
    public boolean isUnobstructed(@Nullable Entity p_195585_1_, VoxelShape p_195585_2_) {
        return delegate.isUnobstructed(p_195585_1_, p_195585_2_);
    }

    @Override
    public BlockPos getHeightmapPos(Heightmap.Type p_205770_1_, BlockPos p_205770_2_) {
        return delegate.getHeightmapPos(p_205770_1_, p_205770_2_);
    }

    @Override
    public DynamicRegistries registryAccess() {
        return delegate.registryAccess();
    }

    @Override
    public Optional<RegistryKey<Biome>> getBiomeName(BlockPos p_242406_1_) {
        return delegate.getBiomeName(p_242406_1_);
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity p_175674_1_, AxisAlignedBB p_175674_2_, @Nullable Predicate<? super Entity> p_175674_3_) {
        return delegate.getEntities(p_175674_1_, p_175674_2_, p_175674_3_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> p_175647_1_, AxisAlignedBB p_175647_2_, @Nullable Predicate<? super T> p_175647_3_) {
        return delegate.getEntitiesOfClass(p_175647_1_, p_175647_2_, p_175647_3_);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, @Nullable Predicate<? super T> p_225316_3_) {
        return delegate.getLoadedEntitiesOfClass(p_225316_1_, p_225316_2_, p_225316_3_);
    }

    @Override
    public List<? extends PlayerEntity> players() {
        return delegate.players();
    }

    @Override
    public List<Entity> getEntities(@Nullable Entity p_72839_1_, AxisAlignedBB p_72839_2_) {
        return delegate.getEntities(p_72839_1_, p_72839_2_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesOfClass(Class<? extends T> p_217357_1_, AxisAlignedBB p_217357_2_) {
        return delegate.getEntitiesOfClass(p_217357_1_, p_217357_2_);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesOfClass(Class<? extends T> p_225317_1_, AxisAlignedBB p_225317_2_) {
        return delegate.getLoadedEntitiesOfClass(p_225317_1_, p_225317_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getNearestPlayer(double p_190525_1_, double p_190525_3_, double p_190525_5_, double p_190525_7_, @Nullable Predicate<Entity> p_190525_9_) {
        return delegate.getNearestPlayer(p_190525_1_, p_190525_3_, p_190525_5_, p_190525_7_, p_190525_9_);
    }

    @Override
    @Nullable
    public PlayerEntity getNearestPlayer(Entity p_217362_1_, double p_217362_2_) {
        return delegate.getNearestPlayer(p_217362_1_, p_217362_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getNearestPlayer(double p_217366_1_, double p_217366_3_, double p_217366_5_, double p_217366_7_, boolean p_217366_9_) {
        return delegate.getNearestPlayer(p_217366_1_, p_217366_3_, p_217366_5_, p_217366_7_, p_217366_9_);
    }

    @Override
    public boolean hasNearbyAlivePlayer(double p_217358_1_, double p_217358_3_, double p_217358_5_, double p_217358_7_) {
        return delegate.hasNearbyAlivePlayer(p_217358_1_, p_217358_3_, p_217358_5_, p_217358_7_);
    }

    @Override
    @Nullable
    public PlayerEntity getNearestPlayer(EntityPredicate p_217370_1_, LivingEntity p_217370_2_) {
        return delegate.getNearestPlayer(p_217370_1_, p_217370_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getNearestPlayer(EntityPredicate p_217372_1_, LivingEntity p_217372_2_, double p_217372_3_, double p_217372_5_, double p_217372_7_) {
        return delegate.getNearestPlayer(p_217372_1_, p_217372_2_, p_217372_3_, p_217372_5_, p_217372_7_);
    }

    @Override
    @Nullable
    public PlayerEntity getNearestPlayer(EntityPredicate p_217359_1_, double p_217359_2_, double p_217359_4_, double p_217359_6_) {
        return delegate.getNearestPlayer(p_217359_1_, p_217359_2_, p_217359_4_, p_217359_6_);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getNearestEntity(Class<? extends T> p_217360_1_, EntityPredicate p_217360_2_, @Nullable LivingEntity p_217360_3_, double p_217360_4_, double p_217360_6_, double p_217360_8_, AxisAlignedBB p_217360_10_) {
        return delegate.getNearestEntity(p_217360_1_, p_217360_2_, p_217360_3_, p_217360_4_, p_217360_6_, p_217360_8_, p_217360_10_);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getNearestLoadedEntity(Class<? extends T> p_225318_1_, EntityPredicate p_225318_2_, @Nullable LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, AxisAlignedBB p_225318_10_) {
        return delegate.getNearestLoadedEntity(p_225318_1_, p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_, p_225318_10_);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getNearestEntity(List<? extends T> p_217361_1_, EntityPredicate p_217361_2_, @Nullable LivingEntity p_217361_3_, double p_217361_4_, double p_217361_6_, double p_217361_8_) {
        return delegate.getNearestEntity(p_217361_1_, p_217361_2_, p_217361_3_, p_217361_4_, p_217361_6_, p_217361_8_);
    }

    @Override
    public List<PlayerEntity> getNearbyPlayers(EntityPredicate p_217373_1_, LivingEntity p_217373_2_, AxisAlignedBB p_217373_3_) {
        return delegate.getNearbyPlayers(p_217373_1_, p_217373_2_, p_217373_3_);
    }

    @Override
    public <T extends LivingEntity> List<T> getNearbyEntities(Class<? extends T> p_217374_1_, EntityPredicate p_217374_2_, LivingEntity p_217374_3_, AxisAlignedBB p_217374_4_) {
        return delegate.getNearbyEntities(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Override
    @Nullable
    public PlayerEntity getPlayerByUUID(UUID p_217371_1_) {
        return delegate.getPlayerByUUID(p_217371_1_);
    }

    @Override
    @Nullable
    public IChunk getChunk(int p_217353_1_, int p_217353_2_, ChunkStatus p_217353_3_, boolean p_217353_4_) {
        return delegate.getChunk(p_217353_1_, p_217353_2_, p_217353_3_, p_217353_4_);
    }

    @Override
    public int getHeight(Heightmap.Type p_201676_1_, int p_201676_2_, int p_201676_3_) {
        return delegate.getHeight(p_201676_1_, p_201676_2_, p_201676_3_);
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
    public Biome getBiome(BlockPos p_226691_1_) {
        return delegate.getBiome(p_226691_1_);
    }

    @Override
    public Stream<BlockState> getBlockStatesIfLoaded(AxisAlignedBB p_234939_1_) {
        return delegate.getBlockStatesIfLoaded(p_234939_1_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBlockTint(BlockPos p_225525_1_, ColorResolver p_225525_2_) {
        return delegate.getBlockTint(p_225525_1_, p_225525_2_);
    }

    @Override
    public Biome getNoiseBiome(int p_225526_1_, int p_225526_2_, int p_225526_3_) {
        return delegate.getNoiseBiome(p_225526_1_, p_225526_2_, p_225526_3_);
    }

    @Override
    public Biome getUncachedNoiseBiome(int p_225604_1_, int p_225604_2_, int p_225604_3_) {
        return delegate.getUncachedNoiseBiome(p_225604_1_, p_225604_2_, p_225604_3_);
    }

    @Override
    public boolean isClientSide() {
        return delegate.isClientSide();
    }

    @Override
    @Deprecated
    public int getSeaLevel() {
        return delegate.getSeaLevel();
    }

    @Override
    public DimensionType dimensionType() {
        return delegate.dimensionType();
    }

    @Override
    public boolean isEmptyBlock(BlockPos p_175623_1_) {
        return delegate.isEmptyBlock(p_175623_1_);
    }

    @Override
    public boolean canSeeSkyFromBelowWater(BlockPos p_175710_1_) {
        return delegate.canSeeSkyFromBelowWater(p_175710_1_);
    }

    @Override
    @Deprecated
    public float getBrightness(BlockPos p_205052_1_) {
        return delegate.getBrightness(p_205052_1_);
    }

    @Override
    public int getDirectSignal(BlockPos p_175627_1_, Direction p_175627_2_) {
        return delegate.getDirectSignal(p_175627_1_, p_175627_2_);
    }

    @Override
    public IChunk getChunk(BlockPos p_217349_1_) {
        return delegate.getChunk(p_217349_1_);
    }

    @Override
    public IChunk getChunk(int p_212866_1_, int p_212866_2_) {
        return delegate.getChunk(p_212866_1_, p_212866_2_);
    }

    @Override
    public IChunk getChunk(int p_217348_1_, int p_217348_2_, ChunkStatus p_217348_3_) {
        return delegate.getChunk(p_217348_1_, p_217348_2_, p_217348_3_);
    }

    @Override
    @Nullable
    public IBlockReader getChunkForCollisions(int p_225522_1_, int p_225522_2_) {
        return delegate.getChunkForCollisions(p_225522_1_, p_225522_2_);
    }

    @Override
    public boolean isWaterAt(BlockPos p_201671_1_) {
        return delegate.isWaterAt(p_201671_1_);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB p_72953_1_) {
        return delegate.containsAnyLiquid(p_72953_1_);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos p_201696_1_) {
        return delegate.getMaxLocalRawBrightness(p_201696_1_);
    }

    @Override
    public int getMaxLocalRawBrightness(BlockPos p_205049_1_, int p_205049_2_) {
        return delegate.getMaxLocalRawBrightness(p_205049_1_, p_205049_2_);
    }

    @Override
    @Deprecated
    public boolean hasChunkAt(BlockPos p_175667_1_) {
        return delegate.hasChunkAt(p_175667_1_);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int range) {
        return delegate.isAreaLoaded(center, range);
    }

    @Override
    @Deprecated
    public boolean hasChunksAt(BlockPos p_175707_1_, BlockPos p_175707_2_) {
        return delegate.hasChunksAt(p_175707_1_, p_175707_2_);
    }

    @Override
    @Deprecated
    public boolean hasChunksAt(int p_217344_1_, int p_217344_2_, int p_217344_3_, int p_217344_4_, int p_217344_5_, int p_217344_6_) {
        return delegate.hasChunksAt(p_217344_1_, p_217344_2_, p_217344_3_, p_217344_4_, p_217344_5_, p_217344_6_);
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
    public int getBrightness(LightType p_226658_1_, BlockPos p_226658_2_) {
        return delegate.getBrightness(p_226658_1_, p_226658_2_);
    }

    @Override
    public int getRawBrightness(BlockPos p_226659_1_, int p_226659_2_) {
        return delegate.getRawBrightness(p_226659_1_, p_226659_2_);
    }

    @Override
    public boolean canSeeSky(BlockPos p_226660_1_) {
        return delegate.canSeeSky(p_226660_1_);
    }

    @Override
    @Nullable
    public TileEntity getBlockEntity(BlockPos p_175625_1_) {
        return delegate.getBlockEntity(p_175625_1_);
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
    public int getLightEmission(BlockPos p_217298_1_) {
        return delegate.getLightEmission(p_217298_1_);
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
    }

    @Override
    public int getMaxBuildHeight() {
        return delegate.getMaxBuildHeight();
    }

    @Override
    public Stream<BlockState> getBlockStates(AxisAlignedBB p_234853_1_) {
        return delegate.getBlockStates(p_234853_1_);
    }

    @Override
    public BlockRayTraceResult clip(RayTraceContext p_217299_1_) {
        return delegate.clip(p_217299_1_);
    }

    @Override
    @Nullable
    public BlockRayTraceResult clipWithInteractionOverride(Vector3d p_217296_1_, Vector3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_, BlockState p_217296_5_) {
        return delegate.clipWithInteractionOverride(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape p_242402_1_, Supplier<VoxelShape> p_242402_2_) {
        return delegate.getBlockFloorHeight(p_242402_1_, p_242402_2_);
    }

    @Override
    public double getBlockFloorHeight(BlockPos p_242403_1_) {
        return delegate.getBlockFloorHeight(p_242403_1_);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public boolean isUnobstructed(BlockState p_226663_1_, BlockPos p_226663_2_, ISelectionContext p_226663_3_) {
        return delegate.isUnobstructed(p_226663_1_, p_226663_2_, p_226663_3_);
    }

    @Override
    public boolean isUnobstructed(Entity p_226668_1_) {
        return delegate.isUnobstructed(p_226668_1_);
    }

    @Override
    public boolean noCollision(AxisAlignedBB p_226664_1_) {
        return delegate.noCollision(p_226664_1_);
    }

    @Override
    public boolean noCollision(Entity p_226669_1_) {
        return delegate.noCollision(p_226669_1_);
    }

    @Override
    public boolean noCollision(Entity p_226665_1_, AxisAlignedBB p_226665_2_) {
        return delegate.noCollision(p_226665_1_, p_226665_2_);
    }

    @Override
    public boolean noCollision(@Nullable Entity p_234865_1_, AxisAlignedBB p_234865_2_, Predicate<Entity> p_234865_3_) {
        return delegate.noCollision(p_234865_1_, p_234865_2_, p_234865_3_);
    }

    @Override
    public Stream<VoxelShape> getCollisions(@Nullable Entity p_234867_1_, AxisAlignedBB p_234867_2_, Predicate<Entity> p_234867_3_) {
        return delegate.getCollisions(p_234867_1_, p_234867_2_, p_234867_3_);
    }

    @Override
    public Stream<VoxelShape> getBlockCollisions(@Nullable Entity p_226666_1_, AxisAlignedBB p_226666_2_) {
        return delegate.getBlockCollisions(p_226666_1_, p_226666_2_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean noBlockCollision(@Nullable Entity p_242405_1_, AxisAlignedBB p_242405_2_, BiPredicate<BlockState, BlockPos> p_242405_3_) {
        return delegate.noBlockCollision(p_242405_1_, p_242405_2_, p_242405_3_);
    }

    @Override
    public Stream<VoxelShape> getBlockCollisions(@Nullable Entity p_241457_1_, AxisAlignedBB p_241457_2_, BiPredicate<BlockState, BlockPos> p_241457_3_) {
        return delegate.getBlockCollisions(p_241457_1_, p_241457_2_, p_241457_3_);
    }

    @Override
    public boolean isStateAtPosition(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return delegate.isStateAtPosition(p_217375_1_, p_217375_2_);
    }

    @Override
    public boolean setBlock(BlockPos p_241211_1_, BlockState p_241211_2_, int p_241211_3_, int p_241211_4_) {
        return delegate.setBlock(p_241211_1_, p_241211_2_, p_241211_3_, p_241211_4_);
    }

    @Override
    public boolean setBlock(BlockPos p_180501_1_, BlockState p_180501_2_, int p_180501_3_) {
        return delegate.setBlock(p_180501_1_, p_180501_2_, p_180501_3_);
    }

    @Override
    public boolean removeBlock(BlockPos p_217377_1_, boolean p_217377_2_) {
        return delegate.removeBlock(p_217377_1_, p_217377_2_);
    }

    @Override
    public boolean destroyBlock(BlockPos p_175655_1_, boolean p_175655_2_) {
        return delegate.destroyBlock(p_175655_1_, p_175655_2_);
    }

    @Override
    public boolean destroyBlock(BlockPos p_225521_1_, boolean p_225521_2_, @Nullable Entity p_225521_3_) {
        return delegate.destroyBlock(p_225521_1_, p_225521_2_, p_225521_3_);
    }

    @Override
    public boolean destroyBlock(BlockPos p_241212_1_, boolean p_241212_2_, @Nullable Entity p_241212_3_, int p_241212_4_) {
        return delegate.destroyBlock(p_241212_1_, p_241212_2_, p_241212_3_, p_241212_4_);
    }

    @Override
    public boolean addFreshEntity(Entity p_217376_1_) {
        return delegate.addFreshEntity(p_217376_1_);
    }

    @Override
    public float getMoonBrightness() {
        return delegate.getMoonBrightness();
    }

    @Override
    public float getTimeOfDay(float p_242415_1_) {
        return delegate.getTimeOfDay(p_242415_1_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getMoonPhase() {
        return delegate.getMoonPhase();
    }
}
