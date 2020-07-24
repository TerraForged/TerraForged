package com.terraforged.mod.client.gui.preview2;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraforged.core.concurrent.thread.ThreadPool;
import com.terraforged.core.concurrent.thread.ThreadPools;
import com.terraforged.core.render.RenderAPI;
import com.terraforged.core.render.RenderSettings;
import com.terraforged.core.render.RenderWorld;
import com.terraforged.core.tile.gen.TileGenerator;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.continent.MutableVeci;
import com.terraforged.world.continent.SpawnType;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.RenderHelper;

public class Preview extends Widget {

    private static final float ZOOM_SCALE = 200F;

    private final ThreadPool threadPool = ThreadPools.createDefault();
    private final TerraSettings settings;

    private int seed;
    private float zoom = 50;
    private float offsetX = 0;
    private float offsetZ = 0;
    private float angle = 45F;
    private RenderWorld world;
    private PreviewSettings previewSettings = new PreviewSettings();

    public Preview(TerraSettings settings, int seed) {
        super(0, 0, "preview");
        this.seed = seed;
        this.settings = settings;
        this.world = createWorld(settings);
        this.world.update(offsetX, offsetZ, zoom, true);
    }

    public int getSeed() {
        return seed;
    }

    public void nextSeed() {
        seed++;
        world = createWorld(settings);
        world.update(offsetX, offsetZ, zoom, true);
    }

    public void close() {
        threadPool.shutdown();
    }

    @Override
    public void render(int mx, int my, float ticks) {
        try {
            float scale = 0.75F;

            angle += 0.35F;
            if (angle >= 360) {
                angle = 0;
            }

            RenderSystem.pushMatrix();
            RenderSystem.enableLighting();
            RenderHelper.enableStandardItemLighting();
            RenderHelper.setupGui3DDiffuseLighting();

            RenderSystem.translatef(x, y, 0);
            RenderSystem.translatef(width / 2F, width / 2F, 0);
            RenderSystem.rotatef(70, 1, 0, 0);
            RenderSystem.rotatef(angle, 0, 0, 1);
            RenderSystem.scalef(scale, scale, scale);

            world.refresh();
            world.render();

            RenderHelper.disableStandardItemLighting();
            RenderSystem.popMatrix();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void update(PreviewSettings settings, TerraSettings genSettings) {
        this.previewSettings = settings;
        this.world = createWorld(genSettings);
        world.update(offsetX, offsetZ, previewSettings.getZoom(ZOOM_SCALE), true);
    }

    private RenderWorld createWorld(TerraSettings settings) {
        int size = 4;
        int regions = 1;
        settings.world.seed = seed;
        GeneratorContext context = GeneratorContext.createNoCache(Terrains.create(settings), settings);

        MutableVeci center = new MutableVeci();
        if (settings.world.properties.spawnType == SpawnType.CONTINENT_CENTER) {
            context.factory.getHeightmap().getContinent().getNearestCenter(0, 0, center);
        }

        offsetX = center.x;
        offsetZ = center.z;

        TileGenerator generator = TileGenerator.builder()
                .pool(threadPool)
                .size(size, 0)
                .factory(context.factory)
                .batch(6)
                .build();

        RenderAPI renderAPI = new MCRenderAPI();
        RenderSettings renderSettings = new RenderSettings(context);
        renderSettings.width = width;
        renderSettings.height = height;
        renderSettings.zoom = previewSettings.getZoom(ZOOM_SCALE);
        renderSettings.renderMode = previewSettings.display;

        return new RenderWorld(threadPool, generator, renderAPI, renderSettings, regions, size);
    }
}
