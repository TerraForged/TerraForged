package com.terraforged.mod.client.gui.element;

import com.terraforged.n2d.util.NoiseUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.client.gui.widget.Slider;

public class TerraBoundSlider extends TerraSlider.Float {

    private final float pad;
    private final String lower;
    private final String upper;

    public TerraBoundSlider(String name, CompoundNBT value) {
        this(name, value, 0.005F);
    }

    public TerraBoundSlider(String name, CompoundNBT value, float pad) {
        super(name, value);
        CompoundNBT meta = value.getCompound("#" + name);
        this.pad = pad;
        this.lower = meta.getString("limit_lower");
        this.upper = meta.getString("limit_upper");
    }

    @Override
    protected void onChange(Slider slider, CompoundNBT value) {
        int i = (int) (slider.getValue() * 1000);

        float lower = getLower(value) + pad;
        float upper = getUpper(value) - pad;
        float val = NoiseUtil.clamp(i / 1000F, lower, upper);

        // update setting value
        value.putFloat(name, val);

        // update actual slider value
        setValue(val);
        updateSlider();
    }

    private float getLower(CompoundNBT value) {
        if (lower == null || lower.isEmpty()) {
            return 0F;
        }
        return value.getFloat(lower);
    }

    private float getUpper(CompoundNBT value) {
        if (upper == null || upper.isEmpty()) {
            return 1F;
        }
        return value.getFloat(upper);
    }
}
