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

package com.terraforged.mod.client.gui.screen.preview;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.cache.CacheEntry;
import com.terraforged.engine.concurrent.thread.ThreadPool;
import com.terraforged.engine.concurrent.thread.ThreadPools;
import com.terraforged.engine.settings.Settings;
import com.terraforged.engine.tile.Size;
import com.terraforged.engine.tile.Tile;
import com.terraforged.engine.tile.gen.TileGenerator;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.util.nbt.NBTHelper;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.continent.MutableVeci;
import com.terraforged.engine.world.continent.SpawnType;
import com.terraforged.engine.world.heightmap.Levels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

import java.awt.*;
import java.util.Random;

public class Preview extends Button {

    private static final int FACTOR = 4;
    public static final int SIZE = Size.chunkToBlock(1 << FACTOR);
    private static final float[] LEGEND_SCALES = {1, 0.9F, 0.75F, 0.6F};

    private final int offsetX;
    private final int offsetZ;
    private final ThreadPool threadPool = ThreadPools.createDefault();
    private final Random random = new Random(System.currentTimeMillis());
    private final PreviewSettings previewSettings = new PreviewSettings();
    private final DynamicTexture texture = new DynamicTexture(new NativeImage(SIZE, SIZE, true));

    private int seed;
    private long lastUpdate = 0L;
    private MutableVeci center = new MutableVeci();
    private Settings settings = new Settings();
    private CacheEntry<Tile> task = null;
    private Tile tile = null;

    private String hoveredCoords = "";
    private String[] values = {"", "", ""};
    private String[] labels = {GuiKeys.PREVIEW_AREA.get(), GuiKeys.PREVIEW_TERRAIN.get(), GuiKeys.PREVIEW_BIOME.get()};

    public Preview(int seed) {
        super(0, 0, 0, 0, new StringTextComponent(""), b -> {});
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
        threadPool.shutdown();
    }

    public boolean click(double mx, double my) {
        if (updateLegend((int) mx, (int) my) && !hoveredCoords.isEmpty()) {
            super.playDownSound(Minecraft.getInstance().getSoundHandler());
            Minecraft.getInstance().keyboardListener.setClipboardString(hoveredCoords);
            return true;
        }
        return false;
    }

    @Override
    public void render(MatrixStack matrixStack, int mx, int my, float partialTicks) {
        this.height = getSize();

        preRender();

        texture.bindTexture();
        RenderSystem.enableBlend();
        RenderSystem.enableRescaleNormal();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        AbstractGui.blit(matrixStack, x, y, 0, 0, width, height, width, height);
        RenderSystem.disableRescaleNormal();

        updateLegend(mx, my);

        renderLegend(matrixStack, mx, my, labels, values, x, y + width, 10, 0xFFFFFF);
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

    private int getSize() {
        return width;
    }

    private void preRender() {
        if (task != null && task.isDone()) {
            try {
                tile = task.get();
                render(tile);
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                task = null;
            }
        }
    }

    private void render(Tile tile) {
        NativeImage image = texture.getTextureData();
        if (image == null) {
            return;
        }

        RenderMode renderer = previewSettings.display;
        Levels levels = new Levels(settings.world);

        int stroke = 2;
        int width = tile.getBlockSize().size;

        tile.iterate((cell, x, z) -> {
            if (x < stroke || z < stroke || x >= width - stroke || z >= width - stroke) {
                image.setPixelRGBA(x, z, Color.BLACK.getRGB());
            } else {
                image.setPixelRGBA(x, z, renderer.getColor(cell, levels));
            }
        });

        texture.updateDynamicTexture();
    }

    private CacheEntry<Tile> generate(Settings settings, CompoundNBT prevSettings) {
        NBTHelper.deserialize(prevSettings, previewSettings);
        settings.world.seed = seed;
        this.settings = settings;

        GeneratorContext context = GeneratorContext.createNoCache(settings);

        if (settings.world.properties.spawnType == SpawnType.CONTINENT_CENTER) {
            long center = context.factory.get().getHeightmap().getContinent().getNearestCenter(offsetX, offsetZ);
            this.center.x = PosUtil.unpackLeft(center);
            this.center.z = PosUtil.unpackRight(center);
        } else {
            center.x = 0;
            center.z = 0;
        }

        TileGenerator renderer = TileGenerator.builder()
                .pool(threadPool)
                .size(FACTOR, 0)
                .factory(context.factory.get())
                .batch(6)
                .build();

        return renderer.getAsync(center.x, center.z, getZoom(), false);
    }

    private boolean updateLegend(int mx ,int my) {
        if (tile != null) {
            int left = this.x;
            int top = this.y;
            float size = this.width;

            int zoom = getZoom();
            int width = Math.max(1, tile.getBlockSize().size * zoom);
            int height = Math.max(1, tile.getBlockSize().size * zoom);
            values[0] = width + "x" + height;
            if (mx >= left && mx <= left + size && my >= top && my <= top + size) {
                float fx = (mx - left) / size;
                float fz = (my - top) / size;
                int ix = NoiseUtil.round(fx * tile.getBlockSize().size);
                int iz = NoiseUtil.round(fz * tile.getBlockSize().size);
                Cell cell = tile.getCell(ix, iz);
                values[1] = getTerrainName(cell);
                values[2] = getBiomeName(cell);

                int dx = (ix - (tile.getBlockSize().size / 2)) * zoom;
                int dz = (iz - (tile.getBlockSize().size / 2)) * zoom;

                hoveredCoords = (center.x + dx) + ":" + (center.z + dz);
                return true;
            } else {
                hoveredCoords = "";
            }
        }
        return false;
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

    private void renderLegend(MatrixStack matrixStack, int mx, int my, String[] labels, String[] values, int left, int top, int lineHeight, int color) {
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

            while (value.length() > 0 && spacing + renderer.getStringWidth(value) > maxWidth) {
                value = value.substring(0, value.length() - 1);
            }

            drawString(matrixStack, renderer, label, 0, i * lineHeight, color);
            drawString(matrixStack, renderer, value, spacing, i * lineHeight, color);
        }

        RenderSystem.popMatrix();

        if (PreviewSettings.showCoords && !hoveredCoords.isEmpty()) {
            drawCenteredString(matrixStack, renderer, hoveredCoords, mx, my - 10, 0xFFFFFF);
        }
    }

    private int getZoom() {
        return NoiseUtil.round(1.5F * (101 - previewSettings.zoom));
    }

    private static String getTerrainName(Cell cell) {
        if (cell.terrain.isRiver()) {
            return "river";
        }
        return cell.terrain.getName().toLowerCase();
    }

    private static String getBiomeName(Cell cell) {
        String terrain = cell.terrain.getName().toLowerCase();
        if (terrain.contains("ocean")) {
            if (cell.temperature < 0.3) {
                return "cold_" + terrain;
            }
            if (cell.temperature > 0.6) {
                return "warm_" + terrain;
            }
            return terrain;
        }
        if (terrain.contains("river")) {
            return "river";
        }
        return cell.biome.name().toLowerCase();
    }
}
