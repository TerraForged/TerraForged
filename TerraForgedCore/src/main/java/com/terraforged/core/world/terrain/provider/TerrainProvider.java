package com.terraforged.core.world.terrain.provider;

import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.terrain.LandForms;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.TerrainPopulator;
import me.dags.noise.Module;

import java.util.List;

/**
 * Provides the heightmap generator with terrain specific noise generation modules (TerrainPopulators)
 */
public interface TerrainProvider {

    LandForms getLandforms();

    /**
     * Returns the resulting set of TerrainPopulators
     */
    List<Populator> getPopulators();

    /**
     * Returns a populator for the provided Terrain, or the default if not registered
     */
    Populator getPopulator(Terrain terrain);

    /**
     * Add a TerrainPopulator to world generation.
     *
     * 'Mixable' TerrainPopulators are used to create additional terrain types, created by blending two
     * different mixable TerrainPopulators together (this is in addition to the unmixed version of the populator)
     */
    void registerMixable(TerrainPopulator populator);

    /**
     * Add a TerrainPopulator to world generation
     *
     * 'UnMixable' TerrainPopulators are NOT blended together to create additional terrain types
     */
    void registerUnMixable(TerrainPopulator populator);

    /**
     * Add a TerrainPopulator to world generation.
     *
     * 'Mixable' TerrainPopulators are used to create additional terrain types, created by blending two
     * different mixable TerrainPopulators together (this is in addition to the unmixed version of the populator)
     */
    default void registerMixable(Terrain type, Module source) {
        registerMixable(new TerrainPopulator(source, type));
    }

    /**
     * Add a TerrainPopulator to world generation
     *
     * 'UnMixable' TerrainPopulators are NOT blended together to create additional terrain types
     */
    default void registerUnMixable(Terrain type, Module source) {
        registerUnMixable(new TerrainPopulator(source, type));
    }
}
