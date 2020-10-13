/*
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

package com.terraforged.mod.chunk.fix;

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
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.*;
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
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegionDelegate extends WorldGenRegion {
    
    private final WorldGenRegion delegate;

    public RegionDelegate(ServerWorld world, WorldGenRegion delegate) {
        super(world, Collections.singletonList(getMainChunk(delegate)));
        this.delegate = delegate;
    }

    public WorldGenRegion getDelegate() {
        return delegate;
    }

    @Override
    public int getMainChunkX() {
        return delegate.getMainChunkX();
    }

    @Override
    public int getMainChunkZ() {
        return delegate.getMainChunkZ();
    }

    @Override
    public IChunk getChunk(int chunkX, int chunkZ) {
        return delegate.getChunk(chunkX, chunkZ);
    }

    @Override
    @Nullable
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return delegate.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public boolean chunkExists(int chunkX, int chunkZ) {
        return delegate.chunkExists(chunkX, chunkZ);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return delegate.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return delegate.getFluidState(pos);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        return delegate.getClosestPlayer(x, y, z, distance, predicate);
    }

    @Override
    public int getSkylightSubtracted() {
        return delegate.getSkylightSubtracted();
    }

    @Override
    public BiomeManager getBiomeManager() {
        return delegate.getBiomeManager();
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        return delegate.getNoiseBiomeRaw(x, y, z);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_) {
        return delegate.func_230487_a_(p_230487_1_, p_230487_2_);
    }

    @Override
    public WorldLightManager getLightManager() {
        return delegate.getLightManager();
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, Entity entity, int recursionLeft) {
        return delegate.destroyBlock(pos, dropBlock, entity, recursionLeft);
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        return delegate.getTileEntity(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
        return delegate.setBlockState(pos, state, flags, recursionLeft);
    }

    @Override
    public boolean addEntity(Entity entityIn) {
        return delegate.addEntity(entityIn);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return delegate.removeBlock(pos, isMoving);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return delegate.getWorldBorder();
    }

    @Override
    public boolean isRemote() {
        return delegate.isRemote();
    }

    @Override
    @Deprecated
    public ServerWorld getWorld() {
        return delegate.getWorld();
    }

    @Override
    public DynamicRegistries func_241828_r() {
        return delegate.func_241828_r();
    }

    @Override
    public IWorldInfo getWorldInfo() {
        return delegate.getWorldInfo();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return delegate.getDifficultyForLocation(pos);
    }

    @Override
    public AbstractChunkProvider getChunkProvider() {
        return delegate.getChunkProvider();
    }

    @Override
    public long getSeed() {
        return delegate.getSeed();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return delegate.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return delegate.getPendingFluidTicks();
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
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return delegate.getHeight(heightmapType, x, z);
    }

    @Override
    public void playSound(PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        delegate.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        delegate.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void playEvent(PlayerEntity player, int type, BlockPos pos, int data) {
        delegate.playEvent(player, type, pos, data);
    }

    @Override
    public DimensionType func_230315_m_() {
        return delegate.func_230315_m_();
    }

    @Override
    public boolean hasBlockState(BlockPos pos, Predicate<BlockState> state) {
        return delegate.hasBlockState(pos, state);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, Predicate<? super T> filter) {
        return delegate.getEntitiesWithinAABB(clazz, aabb, filter);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate) {
        return delegate.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Override
    public List<PlayerEntity> getPlayers() {
        return delegate.getPlayers();
    }

    @Override
    public Stream<? extends StructureStart<?>> func_241827_a(SectionPos p_241827_1_, Structure<?> p_241827_2_) {
        return delegate.func_241827_a(p_241827_1_, p_241827_2_);
    }

    @Override
    public void func_242417_l(Entity p_242417_1_) {
        delegate.func_242417_l(p_242417_1_);
    }

    @Override
    public long func_241851_ab() {
        return delegate.func_241851_ab();
    }

    @Override
    public Difficulty getDifficulty() {
        return delegate.getDifficulty();
    }

    @Override
    public void func_230547_a_(BlockPos p_230547_1_, Block p_230547_2_) {
        delegate.func_230547_a_(p_230547_1_, p_230547_2_);
    }

    @Override
    public int func_234938_ad_() {
        return delegate.func_234938_ad_();
    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {
        delegate.playEvent(type, pos, data);
    }

    @Override
    public Stream<VoxelShape> func_230318_c_(Entity p_230318_1_, AxisAlignedBB p_230318_2_, Predicate<Entity> p_230318_3_) {
        return delegate.func_230318_c_(p_230318_1_, p_230318_2_, p_230318_3_);
    }

    @Override
    public boolean checkNoEntityCollision(Entity entityIn, VoxelShape shape) {
        return delegate.checkNoEntityCollision(entityIn, shape);
    }

    @Override
    public BlockPos getHeight(Heightmap.Type heightmapType, BlockPos pos) {
        return delegate.getHeight(heightmapType, pos);
    }

    @Override
    public Optional<RegistryKey<Biome>> func_242406_i(BlockPos p_242406_1_) {
        return delegate.func_242406_i(p_242406_1_);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesWithinAABB(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, Predicate<? super T> p_225316_3_) {
        return delegate.getLoadedEntitiesWithinAABB(p_225316_1_, p_225316_2_, p_225316_3_);
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entityIn, AxisAlignedBB bb) {
        return delegate.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> p_217357_1_, AxisAlignedBB p_217357_2_) {
        return delegate.getEntitiesWithinAABB(p_217357_1_, p_217357_2_);
    }

    @Override
    public <T extends Entity> List<T> getLoadedEntitiesWithinAABB(Class<? extends T> p_225317_1_, AxisAlignedBB p_225317_2_) {
        return delegate.getLoadedEntitiesWithinAABB(p_225317_1_, p_225317_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(Entity entityIn, double distance) {
        return delegate.getClosestPlayer(entityIn, distance);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, boolean creativePlayers) {
        return delegate.getClosestPlayer(x, y, z, distance, creativePlayers);
    }

    @Override
    public boolean isPlayerWithin(double x, double y, double z, double distance) {
        return delegate.isPlayerWithin(x, y, z, distance);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, LivingEntity target) {
        return delegate.getClosestPlayer(predicate, target);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, LivingEntity target, double p_217372_3_, double p_217372_5_, double p_217372_7_) {
        return delegate.getClosestPlayer(predicate, target, p_217372_3_, p_217372_5_, p_217372_7_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, double x, double y, double z) {
        return delegate.getClosestPlayer(predicate, x, y, z);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntityWithinAABB(Class<? extends T> entityClazz, EntityPredicate p_217360_2_, LivingEntity target, double x, double y, double z, AxisAlignedBB boundingBox) {
        return delegate.getClosestEntityWithinAABB(entityClazz, p_217360_2_, target, x, y, z, boundingBox);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T func_225318_b(Class<? extends T> p_225318_1_, EntityPredicate p_225318_2_, LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, AxisAlignedBB p_225318_10_) {
        return delegate.func_225318_b(p_225318_1_, p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_, p_225318_10_);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntity(List<? extends T> entities, EntityPredicate predicate, LivingEntity target, double x, double y, double z) {
        return delegate.getClosestEntity(entities, predicate, target, x, y, z);
    }

    @Override
    public List<PlayerEntity> getTargettablePlayersWithinAABB(EntityPredicate predicate, LivingEntity target, AxisAlignedBB box) {
        return delegate.getTargettablePlayersWithinAABB(predicate, target, box);
    }

    @Override
    public <T extends LivingEntity> List<T> getTargettableEntitiesWithinAABB(Class<? extends T> p_217374_1_, EntityPredicate p_217374_2_, LivingEntity p_217374_3_, AxisAlignedBB p_217374_4_) {
        return delegate.getTargettableEntitiesWithinAABB(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Override
    @Nullable
    public PlayerEntity getPlayerByUuid(UUID uniqueIdIn) {
        return delegate.getPlayerByUuid(uniqueIdIn);
    }

    @Override
    public Biome getBiome(BlockPos p_226691_1_) {
        return delegate.getBiome(p_226691_1_);
    }

    @Override
    public Stream<BlockState> func_234939_c_(AxisAlignedBB p_234939_1_) {
        return delegate.func_234939_c_(p_234939_1_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return delegate.getBlockColor(blockPosIn, colorResolverIn);
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return delegate.getNoiseBiome(x, y, z);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return delegate.isAirBlock(pos);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos) {
        return delegate.canBlockSeeSky(pos);
    }

    @Override
    @Deprecated
    public float getBrightness(BlockPos pos) {
        return delegate.getBrightness(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return delegate.getStrongPower(pos, direction);
    }

    @Override
    public IChunk getChunk(BlockPos pos) {
        return delegate.getChunk(pos);
    }

    @Override
    public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
        return delegate.getChunk(chunkX, chunkZ, requiredStatus);
    }

    @Override
    @Nullable
    public IBlockReader getBlockReader(int chunkX, int chunkZ) {
        return delegate.getBlockReader(chunkX, chunkZ);
    }

    @Override
    public boolean hasWater(BlockPos pos) {
        return delegate.hasWater(pos);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB bb) {
        return delegate.containsAnyLiquid(bb);
    }

    @Override
    public int getLight(BlockPos pos) {
        return delegate.getLight(pos);
    }

    @Override
    public int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
        return delegate.getNeighborAwareLightSubtracted(pos, amount);
    }

    @Override
    @Deprecated
    public boolean isBlockLoaded(BlockPos pos) {
        return delegate.isBlockLoaded(pos);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int range) {
        return delegate.isAreaLoaded(center, range);
    }

    @Override
    @Deprecated
    public boolean isAreaLoaded(BlockPos from, BlockPos to) {
        return delegate.isAreaLoaded(from, to);
    }

    @Override
    @Deprecated
    public boolean isAreaLoaded(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return delegate.isAreaLoaded(fromX, fromY, fromZ, toX, toY, toZ);
    }

    @Override
    public int getLightFor(LightType lightTypeIn, BlockPos blockPosIn) {
        return delegate.getLightFor(lightTypeIn, blockPosIn);
    }

    @Override
    public int getLightSubtracted(BlockPos blockPosIn, int amount) {
        return delegate.getLightSubtracted(blockPosIn, amount);
    }

    @Override
    public boolean canSeeSky(BlockPos blockPosIn) {
        return delegate.canSeeSky(blockPosIn);
    }

    @Override
    public int getLightValue(BlockPos pos) {
        return delegate.getLightValue(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public Stream<BlockState> func_234853_a_(AxisAlignedBB p_234853_1_) {
        return delegate.func_234853_a_(p_234853_1_);
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
        return delegate.rayTraceBlocks(context);
    }

    @Override
    @Nullable
    public BlockRayTraceResult rayTraceBlocks(Vector3d startVec, Vector3d endVec, BlockPos pos, VoxelShape shape, BlockState state) {
        return delegate.rayTraceBlocks(startVec, endVec, pos, shape, state);
    }

    @Override
    public double func_242402_a(VoxelShape p_242402_1_, Supplier<VoxelShape> p_242402_2_) {
        return delegate.func_242402_a(p_242402_1_, p_242402_2_);
    }

    @Override
    public double func_242403_h(BlockPos p_242403_1_) {
        return delegate.func_242403_h(p_242403_1_);
    }

    @Override
    public boolean func_226663_a_(BlockState p_226663_1_, BlockPos p_226663_2_, ISelectionContext p_226663_3_) {
        return delegate.func_226663_a_(p_226663_1_, p_226663_2_, p_226663_3_);
    }

    @Override
    public boolean checkNoEntityCollision(Entity p_226668_1_) {
        return delegate.checkNoEntityCollision(p_226668_1_);
    }

    @Override
    public boolean hasNoCollisions(AxisAlignedBB p_226664_1_) {
        return delegate.hasNoCollisions(p_226664_1_);
    }

    @Override
    public boolean hasNoCollisions(Entity p_226669_1_) {
        return delegate.hasNoCollisions(p_226669_1_);
    }

    @Override
    public boolean hasNoCollisions(Entity p_226665_1_, AxisAlignedBB p_226665_2_) {
        return delegate.hasNoCollisions(p_226665_1_, p_226665_2_);
    }

    @Override
    public boolean func_234865_b_(Entity p_234865_1_, AxisAlignedBB p_234865_2_, Predicate<Entity> p_234865_3_) {
        return delegate.func_234865_b_(p_234865_1_, p_234865_2_, p_234865_3_);
    }

    @Override
    public Stream<VoxelShape> func_234867_d_(Entity p_234867_1_, AxisAlignedBB p_234867_2_, Predicate<Entity> p_234867_3_) {
        return delegate.func_234867_d_(p_234867_1_, p_234867_2_, p_234867_3_);
    }

    @Override
    public Stream<VoxelShape> getCollisionShapes(Entity p_226666_1_, AxisAlignedBB p_226666_2_) {
        return delegate.getCollisionShapes(p_226666_1_, p_226666_2_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean func_242405_a(Entity p_242405_1_, AxisAlignedBB p_242405_2_, BiPredicate<BlockState, BlockPos> p_242405_3_) {
        return delegate.func_242405_a(p_242405_1_, p_242405_2_, p_242405_3_);
    }

    @Override
    public Stream<VoxelShape> func_241457_a_(Entity p_241457_1_, AxisAlignedBB p_241457_2_, BiPredicate<BlockState, BlockPos> p_241457_3_) {
        return delegate.func_241457_a_(p_241457_1_, p_241457_2_, p_241457_3_);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        return delegate.setBlockState(pos, newState, flags);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return delegate.destroyBlock(pos, dropBlock);
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock, Entity entity) {
        return delegate.destroyBlock(pos, dropBlock, entity);
    }

    @Override
    public float func_242413_ae() {
        return delegate.func_242413_ae();
    }

    @Override
    public float func_242415_f(float p_242415_1_) {
        return delegate.func_242415_f(p_242415_1_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int func_242414_af() {
        return delegate.func_242414_af();
    }

    private static IChunk getMainChunk(WorldGenRegion region) {
        int x = region.getMainChunkX();
        int z = region.getMainChunkZ();
        return region.getChunk(x, z);
    }
}
