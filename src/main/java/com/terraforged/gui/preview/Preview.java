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

package com.terraforged.gui.preview;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.cache.CacheEntry;
import com.terraforged.core.concurrent.pool.ThreadPools;
import com.terraforged.core.region.Region;
import com.terraforged.core.region.Size;
import com.terraforged.core.region.gen.RegionGenerator;
import com.terraforged.core.settings.Settings;
import com.terraforged.util.nbt.NBTHelper;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.continent.MutableVeci;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.world.terrain.Terrains;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.nbt.CompoundNBT;

import java.awt.*;
import java.util.Random;

public class Preview extends Button {

    private static final int FACTOR = 4;
    public static final int WIDTH = Size.chunkToBlock(1 << FACTOR);
    private static final int SLICE_HEIGHT = 64;
    public static final int HEIGHT = WIDTH + SLICE_HEIGHT;//Size.chunkToBlock(1 << FACTOR);
    private static final float[] LEGEND_SCALES = {1, 0.9F, 0.75F, 0.6F};

    private final int offsetX;
    private final int offsetZ;
    private final Random random = new Random(System.currentTimeMillis());
    private final PreviewSettings previewSettings = new PreviewSettings();
    private final DynamicTexture texture = new DynamicTexture(new NativeImage(WIDTH, HEIGHT, true));

    private int seed;
    private long lastUpdate = 0L;
    private Settings settings = new Settings();
    private CacheEntry<Region> task = null;
    private Region region = null;

    private String[] labels = {"Area: ", "Terrain: ", "Biome: "};
    private String[] values = {"", "", ""};

    public Preview(int seed) {
        super(0, 0, 0, 0, "", b -> {});
        this.seed = seed == -1 ? random.nextInt() : seed;
        this.offsetX = 0;
        this.offsetZ = 0;
    }

    public int getSeed() {
        return seed;
    }

    public void regenerate() {
        this.seed = random.nextInt();
    }

    public void close() {
        texture.close();
    }

    @Override
    public void render(int mx, int my, float partialTicks) {
        float scale = width / (float) WIDTH;
        height = width + NoiseUtil.round(SLICE_HEIGHT * scale);

        preRender();

        texture.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableRescaleNormal();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        AbstractGui.blit(x, y, 0, 0, width, height, width, height);
        RenderSystem.disableRescaleNormal();

        updateLegend(mx, my);

        renderLegend(labels, values, x, y + width, 10, 0xFFFFFF);
    }

    public void update(Settings settings, CompoundNBT prevSettings) {
        long time = System.currentTimeMillis();
        if (time - lastUpdate < 50) {
            return;
        }
        lastUpdate = time;

        NBTHelper.deserialize(prevSettings, previewSettings);
        settings.world.seed = seed;

        task = generate(settings, prevSettings);
    }

    private void preRender() {
        if (task != null && task.isDone()) {
            try {
                region = task.get();
                render(region);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                task = null;
            }
        }
    }

    private void render(Region region) {
        NativeImage image = texture.getTextureData();
        if (image == null) {
            return;
        }

        RenderMode renderer = previewSettings.mode;
        Levels levels = new Levels(settings.world);

        int stroke = 2;
        int width = region.getBlockSize().size;
        int zoom = (101 - previewSettings.zoom);
        int half = width / 2;

        int sliceStartY = image.getHeight() - 1 - SLICE_HEIGHT;

        float zoomUnit = 1F - (zoom / 100F);
        float zoomStrength = 0.5F;
        float unit = (1 - zoomStrength) + (zoomStrength * zoomUnit);
        float heightModifier = settings.world.levels.worldHeight / 256F;
        float waterLevelModifier = settings.world.levels.seaLevel / (float) settings.world.levels.worldHeight;
        float imageWaterLevelY = image.getHeight() - 1 - (waterLevelModifier * SLICE_HEIGHT * unit);

        region.iterate((cell, x, z) -> {
            if (x < stroke || z < stroke || x >= width - stroke || z >= width - stroke) {
                image.setPixelRGBA(x, z, Color.BLACK.getRGB());
            } else {
                image.setPixelRGBA(x, z, renderer.getColor(cell, levels));
            }

            if (z == half) {
                int height = (int) (cell.value * SLICE_HEIGHT * unit * heightModifier);
                float imageSurfaceLevelY = image.getHeight() - 1 - height;
                for (int dy = sliceStartY; dy < image.getHeight(); dy++) {
                    if (x < stroke  || x >= width - stroke || dy > image.getHeight() - 1 - stroke) {
                        image.setPixelRGBA(x, dy, Color.BLACK.getRGB());
                        continue;
                    }
                    if (dy > imageSurfaceLevelY) {
                        image.setPixelRGBA(x, dy, Color.BLACK.getRGB());
                    } else if (dy > imageWaterLevelY) {
                        image.setPixelRGBA(x, dy, Color.GRAY.getRGB());
                    } else {
                        image.setPixelRGBA(x, dy, Color.WHITE.getRGB());
                    }
                }
            }
        });

        texture.updateDynamicTexture();
    }

    private CacheEntry<Region> generate(Settings settings, CompoundNBT prevSettings) {
        NBTHelper.deserialize(prevSettings, previewSettings);
        settings.world.seed = seed;
        this.settings = settings;

        GeneratorContext context = GeneratorContext.createNoCache(Terrains.create(settings), settings);

        MutableVeci center = new MutableVeci();
        context.factory.getHeightmap().getContinent().getNearestCenter(offsetX, offsetZ, center);

        RegionGenerator renderer = RegionGenerator.builder()
                .pool(ThreadPools.getPool())
                .size(FACTOR, 0)
                .factory(context.factory)
                .batch(6)
                .build();

        float zoom = 101 - previewSettings.zoom;

        return renderer.getAsync(center.x, center.z, zoom, false);
    }

    private void updateLegend(int mx ,int my) {
        if (region != null) {
            int left = this.x;
            int top = this.y;
            float size = this.width;
            int zoom = (101 - previewSettings.zoom);
            int width = Math.max(1, region.getBlockSize().size * zoom);
            int height = Math.max(1, region.getBlockSize().size * zoom);
            values[0] = width + "x" + height;
            if (mx >= left && mx <= left + size && my >= top && my <= top + size) {
                float fx = (mx - left) / size;
                float fz = (my - top) / size;
                int ix = NoiseUtil.round(fx * region.getBlockSize().size);
                int iz = NoiseUtil.round(fz * region.getBlockSize().size);
                Cell cell = region.getCell(ix, iz);
                values[1] = getTerrainName(cell);
                values[2] = getBiomeName(cell);
            }
        }
    }

    private float getLegendScale() {
        int index = Minecraft.getInstance().gameSettings.guiScale - 1;
        if (index < 0 || index >= LEGEND_SCALES.length) {
            // index=-1 == GuiScale(AUTO) which is the same as GuiScale(4)
            // values above 4 don't exist but who knows what mods might try set it to
            // in both cases use the smallest acceptable scale
            index = LEGEND_SCALES.length - 1;
        }
        return LEGEND_SCALES[index];
    }

    private void renderLegend(String[] labels, String[] values, int left, int top, int lineHeight, int color) {
        float scale = getLegendScale();

        RenderSystem.pushMatrix();
        RenderSystem.translatef(left + 3.75F * scale, top - lineHeight * (3.2F * scale), 0);
        RenderSystem.scalef(scale, scale, 1);

        FontRenderer renderer = Minecraft.getInstance().fontRenderer;
        int spacing = 0;
        for (String s : labels) {
            spacing = Math.max(spacing, renderer.getStringWidth(s));
        }

        float maxWidth = (width - 4) / scale;
        for (int i = 0; i < labels.length && i < values.length; i++) {
            String label = labels[i];
            String value = values[i];

            while (value.length() > 0 && spacing + Minecraft.getInstance().fontRenderer.getStringWidth(value) > maxWidth) {
                value = value.substring(0, value.length() - 1);
            }

            drawString(renderer, label, 0, i * lineHeight, color);
            drawString(renderer, value, spacing, i * lineHeight, color);
        }

        RenderSystem.popMatrix();
    }

    private static String getTerrainName(Cell cell) {
        String terrain = cell.terrainType.getName().toLowerCase();
        if (terrain.contains("river")) {
            return "river";
        }
        return terrain;
    }

    private static String getBiomeName(Cell cell) {
        String terrain = cell.terrainType.getName().toLowerCase();
        if (terrain.contains("ocean")) {
            if (cell.temperature < 0.3) {
                return "cold_ocean";
            }
            if (cell.temperature > 0.6) {
                return "warm_ocean";
            }
            return "ocean";
        }
        if (terrain.contains("river")) {
            return "river";
        }
        return cell.biomeType.name().toLowerCase();
    }
}
