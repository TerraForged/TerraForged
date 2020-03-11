package com.terraforged.mod.chunk.fix;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LightingProvider;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelProperties;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class RegionDelegate extends ChunkRegion {

    protected final ChunkRegion region;

    // ChunkRegion init notes:
    // - list of chunks passed in must be square (ie (sqrt(size) * sqrt(size) == size)
    // - list of chunks must not be empty due to: "list.get(list.size() / 2).getPos()"
    public RegionDelegate(ServerWorld world, ChunkRegion region) {
        super(world, Collections.singletonList(getMainChunk(region)));
        this.region = region;
    }

    @Override
    public int getCenterChunkX() {
        return region.getCenterChunkX();
    }

    @Override
    public int getCenterChunkZ() {
        return region.getCenterChunkZ();
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ) {
        return region.getChunk(chunkX, chunkZ);
    }

    @Override

    public Chunk getChunk(int x, int z, ChunkStatus requiredStatus, boolean nonnull) {
        return region.getChunk(x, z, requiredStatus, nonnull);
    }

    @Override
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return region.isChunkLoaded(chunkX, chunkZ);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return region.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return region.getFluidState(pos);
    }

    @Override

    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, Predicate<Entity> predicate) {
        return region.getClosestPlayer(x, y, z, distance, predicate);
    }

    @Override
    public int getAmbientDarkness() {
        return region.getAmbientDarkness();
    }

    @Override
    public BiomeAccess getBiomeAccess() {
        return region.getBiomeAccess();
    }

    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return region.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
    }


    @Override
    public LightingProvider getLightingProvider() {
        return region.getLightingProvider();
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean drop, Entity breakingEntity) {
        return region.breakBlock(pos, drop, breakingEntity);
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return region.getBlockEntity(pos);
    }

    @Override
    public boolean setBlockState(BlockPos pos, BlockState newState, int flags) {
        return region.setBlockState(pos, newState, flags);
    }

    @Override
    public boolean spawnEntity(Entity entityIn) {
        return region.spawnEntity(entityIn);
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
    public boolean isClient() {
        return region.isClient();
    }

    @Override
    @Deprecated
    public ServerWorld getWorld() {
        return region.getWorld();
    }


    @Override
    public LevelProperties getLevelProperties() {
        return region.getLevelProperties();
    }

    @Override
    public LocalDifficulty getLocalDifficulty(BlockPos pos) {
        return region.getLocalDifficulty(pos);
    }

    @Override
    public ChunkManager getChunkManager() {
        return region.getChunkManager();
    }

    @Override
    public long getSeed() {
        return region.getSeed();
    }

    @Override
    public TickScheduler<Block> getBlockTickScheduler() {
        return region.getBlockTickScheduler();
    }

    @Override
    public TickScheduler<Fluid> getFluidTickScheduler() {
        return region.getFluidTickScheduler();
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
    public void updateNeighbors(BlockPos pos, Block blockIn) {
        region.updateNeighbors(pos, blockIn);
    }

    @Override
    public int getTopY(Heightmap.Type heightmapType, int x, int z) {
        return region.getTopY(heightmapType, x, z);
    }

    @Override
    public void playSound(PlayerEntity player, BlockPos pos, SoundEvent soundIn, SoundCategory category, float volume, float pitch) {
        region.playSound(player, pos, soundIn, category, volume, pitch);
    }

    @Override
    public void addParticle(ParticleEffect particleData, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        region.addParticle(particleData, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    @Override
    public void playLevelEvent(PlayerEntity player, int type, BlockPos pos, int data) {
        region.playLevelEvent(player, type, pos, data);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BlockPos getSpawnPos() {
        return region.getSpawnPos();
    }

    @Override
    public Dimension getDimension() {
        return region.getDimension();
    }

    @Override
    public boolean testBlockState(BlockPos p_217375_1_, Predicate<BlockState> p_217375_2_) {
        return region.testBlockState(p_217375_1_, p_217375_2_);
    }

    @Override
    public <T extends Entity> List<T> getEntities(Class<? extends T> clazz, Box aabb, Predicate<? super T> filter) {
        return region.getEntities(clazz, aabb, filter);
    }

    @Override
    public List<Entity> getEntities(Entity entityIn, Box boundingBox, Predicate<? super Entity> predicate) {
        return region.getEntities(entityIn, boundingBox, predicate);
    }

    @Override
    public List<PlayerEntity> getPlayers() {
        return region.getPlayers();
    }

    @Override
    public float getMoonSize() {
        return region.getMoonSize();
    }

    @Override
    public float getSkyAngle(float partialTicks) {
        return region.getSkyAngle(partialTicks);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getMoonPhase() {
        return region.getMoonPhase();
    }

    @Override
    public Difficulty getDifficulty() {
        return region.getDifficulty();
    }

    @Override
    public void playLevelEvent(int type, BlockPos pos, int data) {
        region.playLevelEvent(type, pos, data);
    }

    @Override
    public Stream<VoxelShape> getEntityCollisions(Entity entityIn, Box aabb, Set<Entity> entitiesToIgnore) {
        return region.getEntityCollisions(entityIn, aabb, entitiesToIgnore);
    }


    @Override
    public boolean intersectsEntities(Entity entityIn, VoxelShape shape) {
        return region.intersectsEntities(entityIn, shape);
    }

    @Override
    public BlockPos getTopPosition(Heightmap.Type heightmapType, BlockPos pos) {
        return region.getTopPosition(heightmapType, pos);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> entityClass, Box box, Predicate<? super T> predicate) {
        return region.getEntitiesIncludingUngeneratedChunks(entityClass, box, predicate);
    }

    @Override
    public List<Entity> getEntities(Entity entityIn, Box bb) {
        return region.getEntities(entityIn, bb);
    }

    @Override
    public <T extends Entity> List<T> getNonSpectatingEntities(Class<? extends T> p_217357_1_, Box p_217357_2_) {
        return region.getNonSpectatingEntities(p_217357_1_, p_217357_2_);
    }

    @Override
    public <T extends Entity> List<T> getEntitiesIncludingUngeneratedChunks(Class<? extends T> p_225317_1_, Box p_225317_2_) {
        return region.getEntitiesIncludingUngeneratedChunks(p_225317_1_, p_225317_2_);
    }

    @Override

    public PlayerEntity getClosestPlayer(Entity entityIn, double distance) {
        return region.getClosestPlayer(entityIn, distance);
    }

    @Override
    public PlayerEntity getClosestPlayer(double x, double y, double z, double distance, boolean creativePlayers) {
        return region.getClosestPlayer(x, y, z, distance, creativePlayers);
    }

    @Override
    public PlayerEntity getClosestPlayer(double x, double y, double z) {
        return region.getClosestPlayer(x, y, z);
    }

    @Override
    public boolean isPlayerInRange(double x, double y, double z, double distance) {
        return region.isPlayerInRange(x, y, z, distance);
    }

    @Override
    public PlayerEntity getClosestPlayer(TargetPredicate predicate, LivingEntity target) {
        return region.getClosestPlayer(predicate, target);
    }

    @Override
    public PlayerEntity getClosestPlayer(TargetPredicate predicate, LivingEntity target, double p_217372_3_, double p_217372_5_, double p_217372_7_) {
        return region.getClosestPlayer(predicate, target, p_217372_3_, p_217372_5_, p_217372_7_);
    }

    @Override
    public PlayerEntity getClosestPlayer(TargetPredicate predicate, double x, double y, double z) {
        return region.getClosestPlayer(predicate, x, y, z);
    }

    @Override
    public <T extends LivingEntity> T getClosestEntity(Class<? extends T> entityClazz, TargetPredicate p_217360_2_, LivingEntity target,
            double x, double y, double z, Box boundingBox) {
        return region.getClosestEntity(entityClazz, p_217360_2_, target, x, y, z, boundingBox);
    }

    @Override
    public <T extends LivingEntity> T getClosestEntityIncludingUngeneratedChunks(Class<? extends T> p_225318_1_, TargetPredicate p_225318_2_,
            LivingEntity p_225318_3_,
            double p_225318_4_, double p_225318_6_, double p_225318_8_, Box p_225318_10_) {
        return region.getClosestEntityIncludingUngeneratedChunks(p_225318_1_, p_225318_2_, p_225318_3_, p_225318_4_, p_225318_6_, p_225318_8_,
                p_225318_10_);
    }

    @Override

    public <T extends LivingEntity> T getClosestEntity(List<? extends T> entities, TargetPredicate predicate, LivingEntity target, double x, double y,
            double z) {
        return region.getClosestEntity(entities, predicate, target, x, y, z);
    }

    @Override
    public List<PlayerEntity> getPlayers(TargetPredicate predicate, LivingEntity target, Box box) {
        return region.getPlayers(predicate, target, box);
    }

    @Override
    public <T extends LivingEntity> List<T> getTargets(Class<? extends T> p_217374_1_, TargetPredicate p_217374_2_,
            LivingEntity p_217374_3_, Box p_217374_4_) {
        return region.getTargets(p_217374_1_, p_217374_2_, p_217374_3_, p_217374_4_);
    }

    @Override

    public PlayerEntity getPlayerByUuid(UUID uniqueIdIn) {
        return region.getPlayerByUuid(uniqueIdIn);
    }

    @Override
    public Biome getBiome(BlockPos p_226691_1_) {
        return region.getBiome(p_226691_1_);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public int getColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
        return region.getColor(blockPosIn, colorResolverIn);
    }

    @Override
    public Biome getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ) {
        return region.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }

    @Override
    public boolean isAir(BlockPos pos) {
        return region.isAir(pos);
    }

    @Override
    public boolean isSkyVisible(BlockPos pos) {
        return region.isSkyVisible(pos);
    }

    @Override
    @Deprecated
    public float getBrightness(BlockPos pos) {
        return region.getBrightness(pos);
    }

    @Override
    public int getStrongRedstonePower(BlockPos pos, Direction direction) {
        return region.getStrongRedstonePower(pos, direction);
    }

    @Override
    public Chunk getChunk(BlockPos pos) {
        return region.getChunk(pos);
    }

    @Override
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus) {
        return region.getChunk(chunkX, chunkZ, requiredStatus);
    }

    @Override
    public BlockView getExistingChunk(int p_225522_1_, int p_225522_2_) {
        return region.getExistingChunk(p_225522_1_, p_225522_2_);
    }

    @Override
    public boolean isWater(BlockPos pos) {
        return region.isWater(pos);
    }

    @Override
    public boolean containsFluid(Box bb) {
        return region.containsFluid(bb);
    }

    @Override
    public int getLightLevel(BlockPos pos) {
        return region.getLightLevel(pos);
    }

    @Override
    public int getBaseLightLevel(BlockPos pos, int amount) {
        return region.getBaseLightLevel(pos, amount);
    }

    @Override
    @Deprecated
    public boolean isChunkLoaded(BlockPos pos) {
        return region.isChunkLoaded(pos);
    }


    //    @Override todo lol what is this
    //    public boolean isRegionLoaded(BlockPos center, int range) {
    //        return region.isRegionLoaded(center, range);
    //    }

    @Override
    @Deprecated
    public boolean isRegionLoaded(BlockPos from, BlockPos to) {
        return region.isRegionLoaded(from, to);
    }

    @Override
    @Deprecated
    public boolean isRegionLoaded(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return region.isRegionLoaded(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public int getLightLevel(LightType lightTypeIn, BlockPos blockPosIn) {
        return region.getLightLevel(lightTypeIn, blockPosIn);
    }

    @Override
    public int getLightLevel(BlockPos blockPosIn, int amount) {
        return region.getLightLevel(blockPosIn, amount);
    }

    @Override
    public boolean isSkyVisibleAllowingSea(BlockPos pos) {
        return region.isSkyVisibleAllowingSea(pos);
    }

    @Override
    public int getLuminance(BlockPos pos) {
        return region.getLuminance(pos);
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
    public BlockHitResult rayTrace(RayTraceContext context) {
        return region.rayTrace(context);
    }

    @Override
    public BlockHitResult rayTraceBlock(Vec3d p_217296_1_, Vec3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_,
            BlockState p_217296_5_) {
        return region.rayTraceBlock(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    public boolean canPlace(BlockState state, BlockPos pos, EntityContext context) {
        return region.canPlace(state, pos, context);
    }

    @Override
    public boolean intersectsEntities(Entity entity) {
        return region.intersectsEntities(entity);
    }

    @Override
    public boolean doesNotCollide(Box box) {
        return region.doesNotCollide(box);
    }

    @Override
    public boolean doesNotCollide(Entity entity) {
        return region.doesNotCollide(entity);
    }

    @Override
    public boolean doesNotCollide(Entity entity, Box box) {
        return region.doesNotCollide(entity, box);
    }

    @Override
    public boolean doesNotCollide(Entity entity, Box entityBoundingBox, Set<Entity> otherEntities) {
        return region.doesNotCollide(entity, entityBoundingBox, otherEntities);
    }

    @Override
    public Stream<VoxelShape> getCollisions(Entity entity, Box box, Set<Entity> excluded) {
        return region.getCollisions(entity, box, excluded);
    }

    @Override
    public Stream<VoxelShape> getBlockCollisions(Entity entity, Box box) {
        return region.getBlockCollisions(entity, box);
    }

    @Override
    public boolean breakBlock(BlockPos pos, boolean drop) {
        return region.breakBlock(pos, drop);
    }


    private static Chunk getMainChunk(ChunkRegion region) {
        int x = region.getCenterChunkX();
        int z = region.getCenterChunkZ();
        return region.getChunk(x, z);
    }
}
