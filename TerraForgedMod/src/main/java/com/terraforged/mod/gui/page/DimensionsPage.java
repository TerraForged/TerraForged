package com.terraforged.mod.gui.page;

import com.terraforged.mod.TerraWorld;
import com.terraforged.mod.gui.OverlayScreen;
import com.terraforged.mod.gui.element.TerraTextInput;
import com.terraforged.mod.settings.TerraSettings;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraftforge.registries.ForgeRegistries;

public class DimensionsPage extends BasePage {

    private final TerraSettings settings;
    private final CompoundNBT dimensionSettings;

    public DimensionsPage(TerraSettings settings) {
        this.settings = settings;
        this.dimensionSettings = NBTHelper.serialize(settings.dimensions);

        CompoundNBT generators = dimensionSettings.getCompound("dimensionGenerators").getCompound("value");
        for (String name : generators.keySet()) {
            CompoundNBT setting = generators.getCompound(name);
            setting.put("#options", getWorldTypes());
        }
    }

    @Override
    public String getTitle() {
        return "Dimension Settings";
    }

    @Override
    public void save() {
        NBTHelper.deserialize(dimensionSettings, settings.dimensions);
    }

    @Override
    public void init(OverlayScreen parent) {
        Column left = getColumn(0);
        addElements(left.left, left.top, left, dimensionSettings, true, left.scrollPane::addButton, this::update);
    }

    @Override
    public void onAddWidget(Widget widget) {
        if (widget instanceof TerraTextInput) {
            TerraTextInput input = (TerraTextInput) widget;
            input.setColorValidator(string -> ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(string)));
        }
    }

    private static ListNBT getWorldTypes() {
        ListNBT options = new ListNBT();
        for (WorldType type : WorldType.WORLD_TYPES) {
            if (type == null || (type.getId() >= 1 && type.getId() <= 6) || type.getId() == 8) {
                continue;
            }
            if (!TerraWorld.isTerraType(type)) {
                options.add(StringNBT.valueOf(type.getName()));
            }
        }
        return options;
    }
}
