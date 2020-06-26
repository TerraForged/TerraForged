package com.terraforged.mod.client.gui.preview2;

import com.terraforged.core.render.RenderMode;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;

@Serializable
public class PreviewSettings {

    @Range(min = 1, max = 100)
    public float zoom = 90F;

    public RenderMode display = RenderMode.BIOME_TYPE;

    public float getZoom(float scale) {
        float perc = (101F - zoom) / 100F;
        return perc * scale;
    }
}
