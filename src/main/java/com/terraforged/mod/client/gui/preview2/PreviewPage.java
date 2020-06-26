package com.terraforged.mod.client.gui.preview2;

import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.client.gui.OverlayScreen;
import com.terraforged.mod.client.gui.element.TerraButton;
import com.terraforged.mod.client.gui.page.UpdatablePage;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Consumer;

public class PreviewPage extends UpdatablePage {

    private final Preview preview;
    private final CompoundNBT settings;
    private final TerraSettings genSettings = new TerraSettings();

    public PreviewPage(TerraSettings settings, int seed) {
        this.preview = new Preview(settings, seed);
        this.settings = NBTHelper.serialize("preview", new PreviewSettings());
    }

    public int getSeed() {
        return preview.getSeed();
    }

    @Override
    public void apply(Consumer<TerraSettings> consumer) {
        consumer.accept(genSettings);
        update();
    }

    @Override
    public void init(OverlayScreen parent) {
        Column right = getColumn(1);
        preview.x = 0;
        preview.y = 0;
        preview.setWidth(com.terraforged.mod.client.gui.preview.Preview.WIDTH);
        preview.setHeight(com.terraforged.mod.client.gui.preview.Preview.HEIGHT);

        addElements(right.left, right.top, right, settings, right.scrollPane::addButton, this::update);

        right.scrollPane.addButton(new TerraButton("New Seed") {
            @Override
            public void onPress() {
                preview.nextSeed();
                update();
            }
        });

        right.scrollPane.addButton(preview);

        // used to pad the scroll-pane out so that the preview legend scrolls on larger gui scales
        TerraButton spacer = createSpacer();
        for (int i = 0; i < 7; i++) {
            right.scrollPane.addButton(spacer);
        }

//        update();
    }

    @Override
    public void update() {
        PreviewSettings settings = new PreviewSettings();
        NBTHelper.deserialize(this.settings, settings);
        preview.update(settings, genSettings);
    }

    private static TerraButton createSpacer() {
        return new TerraButton("") {
            @Override
            public void render(int x, int y, float tick) { }
        };
    }
}
