package com.terraforged.core.world.terrain.provider;

import me.dags.noise.Module;
import me.dags.noise.Source;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.heightmap.RegionConfig;
import com.terraforged.core.world.terrain.LandForms;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.TerrainPopulator;
import com.terraforged.core.world.terrain.VolcanoPopulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class StandardTerrainProvider implements TerrainProvider {

    private final List<TerrainPopulator> mixable = new ArrayList<>();
    private final List<TerrainPopulator> unmixable = new ArrayList<>();
    private final Map<Terrain, Populator> populators = new HashMap<>();

    private final LandForms landForms;
    private final RegionConfig config;
    private final GeneratorContext context;
    private final Populator defaultPopulator;

    public StandardTerrainProvider(GeneratorContext context, RegionConfig config, Populator defaultPopulator) {
        this.config = config;
        this.context = context;
        this.landForms = new LandForms(context.levels);
        this.defaultPopulator = defaultPopulator;
        init();
    }

    public void init() {
        registerMixable(context.terrain.steppe, landForms.steppe(context.seed));
        registerMixable(context.terrain.plains, landForms.plains(context.seed));
        registerMixable(context.terrain.hills, landForms.hills1(context.seed));
        registerMixable(context.terrain.hills, landForms.hills2(context.seed));
        registerMixable(context.terrain.dales, landForms.dales(context.seed));
        registerMixable(context.terrain.badlands, landForms.badlands2(context.seed));
        registerMixable(context.terrain.plateau, landForms.plateau(context.seed));
        registerMixable(context.terrain.torridonian, landForms.torridonian(context.seed));

        registerUnMixable(new VolcanoPopulator(context.seed, config, context.levels, context.terrain));
        registerUnMixable(context.terrain.badlands, landForms.badlands2(context.seed));
        registerUnMixable(context.terrain.mountains, landForms.mountains2(context.seed));
        registerUnMixable(context.terrain.mountains, landForms.mountains3(context.seed));
    }

    @Override
    public void registerMixable(TerrainPopulator populator) {
        populators.putIfAbsent(populator.getType(), populator);
        mixable.add(populator);
    }

    @Override
    public void registerUnMixable(TerrainPopulator populator) {
        populators.putIfAbsent(populator.getType(), populator);
        unmixable.add(populator);
    }

    @Override
    public Populator getPopulator(Terrain terrain) {
        return populators.getOrDefault(terrain, defaultPopulator);
    }

    @Override
    public LandForms getLandforms() {
        return landForms;
    }

    @Override
    public List<Populator> getPopulators() {
        List<TerrainPopulator> mixed = combine(mixable, this::combine);
        List<Populator> result = new ArrayList<>(mixed.size() + unmixable.size());
        result.addAll(mixed);
        result.addAll(unmixable);
        return result;
    }

    private TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2) {
        return combine(tp1, tp2, context.seed, config.scale / 2);
    }

    private static TerrainPopulator combine(TerrainPopulator tp1, TerrainPopulator tp2, Seed seed, int scale) {
        Module combined = Source.perlin(seed.next(), scale, 1)
                .warp(seed.next(), scale / 2, 2, scale / 2)
                .blend(tp1.getSource(), tp2.getSource(), 0.5, 0.25);

        String name = tp1.getType().getName() + "-" + tp2.getType().getName();
        int id = Terrain.ID_START + 1 + tp1.getType().getId() * tp2.getType().getId();
        float weight = Math.min(tp1.getType().getWeight(), tp2.getType().getWeight());
        Terrain type = new Terrain(name, id, weight);

        return new TerrainPopulator(combined, type);
    }

    private static <T> List<T> combine(List<T> input, BiFunction<T, T, T> operator) {
        int length = input.size();
        for (int i = 1; i < input.size(); i++) {
            length += (input.size() - i);
        }

        List<T> result = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            result.add(null);
        }

        for (int i = 0, k = input.size(); i < input.size(); i++) {
            T t1 = input.get(i);
            result.set(i, t1);
            for (int j = i + 1; j < input.size(); j++, k++) {
                T t2 = input.get(j);
                T t3 = operator.apply(t1, t2);
                result.set(k, t3);
            }
        }

        return result;
    }
}
