package com.terraforged.mod.chunk.fix;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.AbstractChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RegionDelegate extends WorldGenRegion {

    protected final WorldGenRegion region;

    // WorldGenRegion init notes:
    // - list of chunks passed in must be square (ie (sqrt(size) * sqrt(size) == size)
    // - list of chunks must not be empty due to: "list.get(list.size() / 2).getPos()"
    public RegionDelegate(ServerWorld world, WorldGenRegion region) {
        super(world, Collections.singletonList(getMainChunk(region)));
        this.region = region;
    }

    @Override
    public int getMainChunkX() {
        return region.getMainChunkX();
    }

    @Override
    public int getMainChunkZ() {
        return region.getMainChunkZ();
    }

    @Override
    public IChunk getChunk(int chunkX, int chunkZ) {
        return region.getChunk(chunkX, chunkZ);
    }

    @Override
    @Nullable
    public IChunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return region.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public boolean chunkExists(int chunkX, int chunkZ) {
        return region.chunkExists(chunkX, chunkZ);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return region.getBlockState(pos);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return region.getFluidState(pos);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        return region.getClosestPlayer(x, y, z, distance, predicate);
    }

    @Override
    public int getSkylightSubtracted() {
        return region.getSkylightSubtracted();
    }

    @Override
    public BiomeManager func_225523_d_() {
        return region.func_225523_d_();
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        return region.getNoiseBiomeRaw(x, y, z);
    }

    @Override
    public WorldLightManager getLightManager() {
        return region.getLightManager();
    }

    @Override
    public boolean func_225521_a_(BlockPos p_225521_1_, boolean p_225521_2_, @Nullable Entity p_225521_3_) {
        return region.func_225521_a_(p_225521_1_, p_225521_2_, p_225521_3_);
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        return region.getTileEntity(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        return region.setBlockState(pos, newState, flags);
    }

    @Override
    public boolean addEntity(Entity entityIn) {
        return region.addEntity(entityIn);
    }

    @Override
    public boolean removeBlock(BlockPos pos, boolean isMoving) {
        return region.removeBlock(pos, isMoving);
    }

    @Override
    public WorldBorder getWorldBorder() {
        return region.getWorldBorder();
    }

    @Override
    public boolean isRemote() {
        return region.isRemote();
    }

    @Override
    @Deprecated
    public ServerWorld getWorld() {
        return region.getWorld();
    }

    @Override
    public WorldInfo getWorldInfo() {
        return region.getWorldInfo();
    }

    @Override
    public DifficultyInstance getDifficultyForLocation(BlockPos pos) {
        return region.getDifficultyForLocation(pos);
    }

    @Override
    public AbstractChunkProvider getChunkProvider() {
        return region.getChunkProvider();
    }

    @Override
    public long getSeed() {
        return region.getSeed();
    }

    @Override
    public ITickList<Block> getPendingBlockTicks() {
        return region.getPendingBlockTicks();
    }

    @Override
    public ITickList<Fluid> getPendingFluidTicks() {
        return region.getPendingFluidTicks();
    }

    @Override
    public int getSeaLevel() {
        return region.getSeaLevel();
    }

    @Override
    public Random getRandom() {
        return region.getRandom();
    }

    @Override
    public void notifyNeighbors(BlockPos pos, Block blockIn) {
        region.notifyNeighbors(pos, blockIn);
    }

    @Override
    public int getHeight(Heightmap.Type heightmapType, int x, int z) {
        return region.getHeight(heightmapType, x, z);
    }

    @Override
    public void playSound(@Nullable PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        region.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void addParticle(IParticleData particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        region.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void playEvent(@Nullable PlayerEntity player, int type, BlockPos pos, int data) {
        region.playEvent(player, type, pos, data);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockPos getSpawnPoint() {
        return region.getSpawnPoint();
    }

    @Override
    public Dimension getDimension() {
        return region.getDimension();
    }

    @Override
    public boolean hasBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return region.hasBlockState(p_217375_1_, p_217375_2_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb, @Nullable Predicate<? super T> filter) {
        return region.getEntitiesWithinAABB(clazz, aabb, filter);
    }

    @Override
    public List<Entity> getEntitiesInAABBexcluding(@Nullable Entity entityIn, AxisAlignedBB boundingBox, @Nullable Predicate<? super Entity> predicate) {
        return region.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }

    @Override
    public List<PlayerEntity> getPlayers() {
        return region.getPlayers();
    }

    @Override
    public float getCurrentMoonPhaseFactor() {
        return region.getCurrentMoonPhaseFactor();
    }

    @Override
    public float getCelestialAngle(float partialTicks) {
        return region.getCelestialAngle(partialTicks);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getMoonPhase() {
        return region.getMoonPhase();
    }

    @Override
    public Difficulty getDifficulty() {
        return region.getDifficulty();
    }

    @Override
    public void playEvent(int type, BlockPos pos, int data) {
        region.playEvent(type, pos, data);
    }

    @Override
    public Stream<VoxelShape> getEmptyCollisionShapes(@Nullable Entity entityIn, AxisAlignedBB aabb, Set<Entity> entitiesToIgnore) {
        return region.getEmptyCollisionShapes(entityIn, aabb, entitiesToIgnore);
    }

    @Override
    public boolean checkNoEntityCollision(@Nullable Entity entityIn, VoxelShape shape) {
        return region.checkNoEntityCollision(entityIn, shape);
    }

    @Override
    public BlockPos getHeight(Heightmap.Type heightmapType, BlockPos pos) {
        return region.getHeight(heightmapType, pos);
    }

    @Override
    public <T extends Entity> List<T> func_225316_b(Class<? extends T> p_225316_1_, AxisAlignedBB p_225316_2_, @Nullable Predicate<? super T> p_225316_3_) {
        return region.func_225316_b(p_225316_1_, p_225316_2_, p_225316_3_);
    }

    @Override
    public List<Entity> getEntitiesWithinAABBExcludingEntity(@Nullable Entity entityIn, AxisAlignedBB bb) {
        return region.getEntitiesWithinAABBExcludingEntity(entityIn, bb);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> p_217357_1_, AxisAlignedBB p_217357_2_) {
        return region.getEntitiesWithinAABB(p_217357_1_, p_217357_2_);
    }

    @Override
    public <T extends Entity> List<T> func_225317_b(Class<? extends T> p_225317_1_, AxisAlignedBB p_225317_2_) {
        return region.func_225317_b(p_225317_1_, p_225317_2_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(Entity entityIn, double distance) {
        return region.getClosestPlayer(entityIn, distance);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, boolean creativePlayers) {
        return region.getClosestPlayer(x, y, z, distance, creativePlayers);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(double x, double y, double z) {
        return region.getClosestPlayer(x, y, z);
    }

    @Override
    public boolean isPlayerWithin(double x, double y, double z, double distance) {
        return region.isPlayerWithin(x, y, z, distance);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, LivingEntity target) {
        return region.getClosestPlayer(predicate, target);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, LivingEntity target, double p_217372_3_, double p_217372_5_, double p_217372_7_) {
        return region.getClosestPlayer(predicate, target, p_217372_3_, p_217372_5_, p_217372_7_);
    }

    @Override
    @Nullable
    public PlayerEntity getClosestPlayer(EntityPredicate predicate, double x, double y, double z) {
        return region.getClosestPlayer(predicate, x, y, z);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntityWithinAABB(Class<? extends T> entityClazz, EntityPredicate p_217360_2_, @Nullable LivingEntity target, double x, double y, double z, AxisAlignedBB boundingBox) {
        return region.getClosestEntityWithinAABB(entityClazz, p_217360_2_, target, x, y, z, boundingBox);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T func_225318_b(Class<? extends T> p_225318_1_, EntityPredicate p_225318_2_, @Nullable LivingEntity p_225318_3_, double p_225318_4_, double p_225318_6_, double p_225318_8_, AxisAlignedBB p_225318_10_) {
        return region.func_225318_b(p_225318_1_, p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_, p_225318_10_);
    }

    @Override
    @Nullable
    public <T extends LivingEntity> T getClosestEntity(List<? extends T> entities, EntityPredicate predicate, @Nullable LivingEntity target, double x, double y, double z) {
        return region.getClosestEntity(entities, predicate, target, x, y, z);
    }

    @Override
    public List<PlayerEntity> getTargettablePlayersWithinAABB(EntityPredicate predicate, LivingEntity target, AxisAlignedBB box) {
        return region.getTargettablePlayersWithinAABB(predicate, target, box);
    }

    @Override
    public <T extends LivingEntity> List<T> getTargettableEntitiesWithinAABB(Class<? extends T> p_217374_1_, EntityPredicate p_217374_2_, LivingEntity p_217374_3_, AxisAlignedBB p_217374_4_) {
        return region.getTargettableEntitiesWithinAABB(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Override
    @Nullable
    public PlayerEntity getPlayerByUuid(UUID uniqueIdIn) {
        return region.getPlayerByUuid(uniqueIdIn);
    }

    @Override
    public Biome getBiome(BlockPos p_226691_1_) {
        return region.getBiome(p_226691_1_);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return region.getBlockColor(blockPosIn, colorResolverIn);
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return region.getNoiseBiome(x, y, z);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
        return region.isAirBlock(pos);
    }

    @Override
    public boolean canBlockSeeSky(BlockPos pos) {
        return region.canBlockSeeSky(pos);
    }

    @Override
    @Deprecated
    public float getBrightness(BlockPos pos) {
        return region.getBrightness(pos);
    }

    @Override
    public int getStrongPower(BlockPos pos, Direction direction) {
        return region.getStrongPower(pos, direction);
    }

    @Override
    public IChunk getChunk(BlockPos pos) {
        return region.getChunk(pos);
    }

    @Override
    public IChunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
        return region.getChunk(chunkX, chunkZ, requiredStatus);
    }

    @Override
    @Nullable
    public IBlockReader getBlockReader(int p_225522_1_, int p_225522_2_) {
        return region.getBlockReader(p_225522_1_, p_225522_2_);
    }

    @Override
    public boolean hasWater(BlockPos pos) {
        return region.hasWater(pos);
    }

    @Override
    public boolean containsAnyLiquid(AxisAlignedBB bb) {
        return region.containsAnyLiquid(bb);
    }

    @Override
    public int getLight(BlockPos pos) {
        return region.getLight(pos);
    }

    @Override
    public int getNeighborAwareLightSubtracted(BlockPos pos, int amount) {
        return region.getNeighborAwareLightSubtracted(pos, amount);
    }

    @Override
    @Deprecated
    public boolean isBlockLoaded(BlockPos pos) {
        return region.isBlockLoaded(pos);
    }

    @Override
    public boolean isAreaLoaded(BlockPos center, int range) {
        return region.isAreaLoaded(center, range);
    }

    @Override
    @Deprecated
    public boolean isAreaLoaded(BlockPos from, BlockPos to) {
        return region.isAreaLoaded(from, to);
    }

    @Override
    @Deprecated
    public boolean isAreaLoaded(int fromX, int fromY, int fromZ, int toX, int toY, int toZ) {
        return region.isAreaLoaded(fromX, fromY, fromZ, toX, toY, toZ);
    }

    @Override
    public int getLightFor(LightType lightTypeIn, BlockPos blockPosIn) {
        return region.getLightFor(lightTypeIn, blockPosIn);
    }

    @Override
    public int getLightSubtracted(BlockPos blockPosIn, int amount) {
        return region.getLightSubtracted(blockPosIn, amount);
    }

    @Override
    public boolean canSeeSky(BlockPos blockPosIn) {
        return region.canSeeSky(blockPosIn);
    }

    @Override
    public int getLightValue(BlockPos pos) {
        return region.getLightValue(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return region.getMaxLightLevel();
    }

    @Override
    public int getHeight() {
        return region.getHeight();
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
        return region.rayTraceBlocks(context);
    }

    @Override
    @Nullable
    public BlockRayTraceResult rayTraceBlocks(Vec3d p_217296_1_, Vec3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_, BlockState p_217296_5_) {
        return region.rayTraceBlocks(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    public boolean func_226663_a_(BlockState p_226663_1_, BlockPos p_226663_2_, ISelectionContext p_226663_3_) {
        return region.func_226663_a_(p_226663_1_, p_226663_2_, p_226663_3_);
    }

    @Override
    public boolean func_226668_i_(Entity p_226668_1_) {
        return region.func_226668_i_(p_226668_1_);
    }

    @Override
    public boolean func_226664_a_(AxisAlignedBB p_226664_1_) {
        return region.func_226664_a_(p_226664_1_);
    }

    @Override
    public boolean func_226669_j_(Entity p_226669_1_) {
        return region.func_226669_j_(p_226669_1_);
    }

    @Override
    public boolean func_226665_a__(Entity p_226665_1_, AxisAlignedBB p_226665_2_) {
        return region.func_226665_a__(p_226665_1_, p_226665_2_);
    }

    @Override
    public boolean func_226662_a_(@Nullable Entity p_226662_1_, AxisAlignedBB p_226662_2_, Set<Entity> p_226662_3_) {
        return region.func_226662_a_(p_226662_1_, p_226662_2_, p_226662_3_);
    }

    @Override
    public Stream<VoxelShape> func_226667_c_(@Nullable Entity p_226667_1_, AxisAlignedBB p_226667_2_, Set<Entity> p_226667_3_) {
        return region.func_226667_c_(p_226667_1_, p_226667_2_, p_226667_3_);
    }

    @Override
    public Stream<VoxelShape> func_226666_b_(@Nullable Entity p_226666_1_, AxisAlignedBB p_226666_2_) {
        return region.func_226666_b_(p_226666_1_, p_226666_2_);
    }

    @Override
    public int getMaxHeight() {
        return region.getMaxHeight();
    }

    @Override
    public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
        return region.destroyBlock(pos, dropBlock);
    }

    private static IChunk getMainChunk(WorldGenRegion region) {
        int x = region.getMainChunkX();
        int z = region.getMainChunkZ();
        return region.getChunk(x, z);
    }
}
