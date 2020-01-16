package com.terraforged.core.world.heightmap;

import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.EdgeFunc;
import me.dags.noise.func.Interpolation;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.module.Blender;
import com.terraforged.core.module.Lerp;
import com.terraforged.core.module.MultiBlender;
import com.terraforged.core.module.Selector;
import com.terraforged.core.settings.GeneratorSettings;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.continent.ContinentBlender;
import com.terraforged.core.world.continent.ContinentMultiBlender;
import com.terraforged.core.world.continent.VoronoiContinentModule;
import com.terraforged.core.world.river.RiverManager;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.TerrainPopulator;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.core.world.terrain.provider.TerrainProvider;

public class WorldHeightmap implements Heightmap {

    private static final float DEEP_OCEAN_VALUE = 0.2F;
    private static final float OCEAN_VALUE = 0.3F;
    private static final float BEACH_VALUE = 0.34F;
    private static final float COAST_VALUE = 0.4F;
    private static final float INLAND_VALUE = 0.6F;

    private final Levels levels;
    private final Terrains terrain;
    private final Settings settings;

    private final Climate climate;
    private final Populator root;
    private final Populator continent;
    private final RiverManager riverManager;
    private final TerrainProvider terrainProvider;

    public WorldHeightmap(GeneratorContext context) {
        context = context.copy();

        this.levels = context.levels;
        this.terrain = context.terrain;
        this.settings = context.settings;
        this.climate = new Climate(context, this);

        Seed seed = context.seed;
        Levels levels = context.levels;
        GeneratorSettings genSettings = context.settings.generator;

        Seed regionSeed = seed.nextSeed();
        Seed regionWarp = seed.nextSeed();

        int regionWarpScale = 400;
        int regionWarpStrength = 200;
        RegionConfig regionConfig = new RegionConfig(
                regionSeed.get(),
                context.settings.generator.land.regionSize,
                Source.simplex(regionWarp.next(), regionWarpScale, 2),
                Source.simplex(regionWarp.next(), regionWarpScale, 2),
                regionWarpStrength
        );

        // controls where mountain chains form in the world
        Module mountainShapeBase = Source.cellEdge(seed.next(), genSettings.land.mountainScale, EdgeFunc.DISTANCE_2_ADD)
                .add(Source.cubic(seed.next(), genSettings.land.mountainScale, 1).scale(-0.05));

        // sharpens the transition to create steeper mountains
        Module mountainShape = mountainShapeBase
                .curve(Interpolation.CURVE3)
                .clamp(0, 0.9)
                .map(0, 1);

        // controls the shape of terrain regions
        Module regionShape = Source.cell(regionConfig.seed, regionConfig.scale)
                .warp(regionConfig.warpX, regionConfig.warpZ, regionConfig.warpStrength);

        // the corresponding edges of terrain regions so we can fade out towards borders
        Module regionEdge = Source.cellEdge(regionConfig.seed, regionConfig.scale, EdgeFunc.DISTANCE_2_DIV).invert()
                .warp(regionConfig.warpX, regionConfig.warpZ, regionConfig.warpStrength)
                .pow(1.5)
                .clamp(0, 0.75)
                .map(0, 1);

        this.terrainProvider = context.terrainFactory.create(context, regionConfig, this);

        // the voronoi controlled terrain regions
        Populator terrainRegions = new Selector(regionShape, terrainProvider.getPopulators());
        // the terrain type at region edges
        Populator terrainRegionBorders = new TerrainPopulator(terrainProvider.getLandforms().steppe(seed), context.terrain.steppe);

        // transitions between the unique terrain regions and the common border terrain
        Populator terrain = new Lerp(
                regionEdge,
                terrainRegionBorders,
                terrainRegions
        );

        // mountain populator
        Populator mountains = register(terrainProvider.getLandforms().mountains(seed), context.terrain.mountains);

        // controls what's ocean and what's land
        this.continent = createContinent(context);

        // blends between normal terrain and mountain chains
        Populator land = new Blender(
                mountainShape,
                terrain,
                mountains,
                0.1F,
                0.9F,
                0.6F
        );

        // uses the continent noise to blend between deep ocean, to ocean, to coast
        MultiBlender oceans = new ContinentMultiBlender(
                climate,
                continent,
                register(terrainProvider.getLandforms().deepOcean(seed.next()), context.terrain.deepOcean),
                register(Source.constant(levels.water(-7)), context.terrain.ocean),
                register(Source.constant(levels.water), context.terrain.coast),
                DEEP_OCEAN_VALUE, // below == deep, above == transition to shallow
                OCEAN_VALUE,  // below == transition to deep, above == transition to coast
                COAST_VALUE   // below == transition to shallow, above == coast
        );

        // blends between the ocean/coast terrain and land terrains
        root = new ContinentBlender(
                continent,
                oceans,
                land,
                OCEAN_VALUE, // below == pure ocean
                INLAND_VALUE, // above == pure land
                COAST_VALUE, // split point
                COAST_VALUE - 0.05F
        ).mask();

        this.riverManager = new RiverManager(this, context);
    }

    public RiverManager getRiverManager() {
        return riverManager;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float z) {
        // initial type
        cell.tag = terrain.steppe;

        // apply continent value/edge noise
        continent.apply(cell, x, z);

        // apply actuall heightmap
        root.apply(cell, x, z);

        // apply rivers
        riverManager.apply(cell, x, z);

        // apply climate data
        if (cell.value <= levels.water) {
            climate.apply(cell, x, z, false);
            if (cell.tag == terrain.coast) {
                cell.tag = terrain.ocean;
            }
        } else {
            int range = settings.generator.biomeEdgeNoise.strength;
            float dx = climate.getOffsetX(x, z, range);
            float dz = climate.getOffsetZ(x, z, range);
            float px = x + dx;
            float pz = z + dz;
            tag(cell, px, pz);
            climate.apply(cell, px, pz, false);
            climate.apply(cell, x, z, true);
        }
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float z) {
        continent.apply(cell, x, z);
        root.tag(cell, x, z);
    }

    public Climate getClimate() {
        return climate;
    }

    public Populator getPopulator(Terrain terrain) {
        return terrainProvider.getPopulator(terrain);
    }

    private TerrainPopulator register(Module module, Terrain terrain) {
        TerrainPopulator populator = new TerrainPopulator(module, terrain);
        terrainProvider.registerMixable(populator);
        return populator;
    }

    private Populator createContinent(GeneratorContext context) {
        return new VoronoiContinentModule(context.seed, context.settings.generator);
    }
}
