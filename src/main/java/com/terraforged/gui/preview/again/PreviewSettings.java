package com.terraforged.gui.preview.again;

import com.terraforged.core.render.RenderMode;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;

@Serializable
public class PreviewSettings {

    @Range(min = 1, max = 200)
    public float zoom = 10F;

    public RenderMode renderMode = RenderMode.BIOME_TYPE;
}
