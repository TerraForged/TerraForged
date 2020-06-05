package com.terraforged.gui.preview2;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraforged.core.render.RenderAPI;
import com.terraforged.core.render.RenderBuffer;
import net.minecraft.client.renderer.BufferBuilder;

public class MCRenderAPI implements RenderAPI {

    @Override
    public void pushMatrix() {
        RenderSystem.pushMatrix();
    }

    @Override
    public void popMatrix() {
        RenderSystem.popMatrix();
    }

    @Override
    public void translate(float x, float y, float z) {
        RenderSystem.translatef(x, y, z);
    }

    @Override
    public void rotateX(float angle) {

    }

    @Override
    public void rotateY(float angle) {

    }

    @Override
    public void rotateZ(float angle) {

    }

    @Override
    public RenderBuffer createBuffer() {
        return new MCRenderBuffer(new BufferBuilder(4096));
    }
}
