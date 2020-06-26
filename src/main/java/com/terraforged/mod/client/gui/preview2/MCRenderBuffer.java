package com.terraforged.mod.client.gui.preview2;

import com.terraforged.core.render.RenderBuffer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class MCRenderBuffer implements RenderBuffer {

    private final Object lock = new Object();
    private final BufferBuilder buffer;

    private BufferBuilder.State state = null;

    private float r, g, b;

    public MCRenderBuffer(BufferBuilder buffer) {
        this.buffer = buffer;
    }

    @Override
    public void beginQuads() {
        buffer.reset();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
    }

    @Override
    public void endQuads() {
        synchronized (lock) {
            state = buffer.getVertexState();
            buffer.finishDrawing();
        }
    }

    @Override
    public void vertex(float x, float y, float z) {
        buffer.pos(x, y, z).color(r, g, b, 1).endVertex();
    }

    @Override
    public void color(float hue, float saturation, float brightness) {
        Color color = Color.getHSBColor(hue / 100F, saturation / 100F, brightness / 100F);
        r = color.getRed() / 255F;
        g = color.getGreen() / 255F;
        b = color.getBlue() / 255F;
    }

    @Override
    public void draw() {
        synchronized (lock) {
            if (state != null) {
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder builder = tessellator.getBuffer();
                builder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                builder.setVertexState(state);
                builder.sortVertexData(0, 0, 5000);
                builder.finishDrawing();
                WorldVertexBufferUploader.draw(builder);
            }
        }
    }
}
