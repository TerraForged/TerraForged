package com.terraforged.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TerraTextInput extends TextFieldWidget implements Element, Consumer<String> {

    private final String name;
    private final CompoundNBT value;
    private final List<String> tooltip;
    private Predicate<String> validator = s -> true;
    private Consumer<TerraTextInput> callback = t -> {};

    public TerraTextInput(String name, CompoundNBT value) {
        super(Minecraft.getInstance().fontRenderer, 0, 0, 100, 20, Element.getDisplayName(name, value) + ": ");
        this.name = name;
        this.value = value;
        this.tooltip = Element.getToolTip(name, value);
        setText(value.getString(name));
        setResponder(this);
    }

    public void setColorValidator(Predicate<String> validator) {
        this.validator = validator;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        return super.keyPressed(i, j, k);
    }

    @Override
    public boolean charTyped(char c, int code) {
        return super.charTyped(c, code);
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public void accept(String text) {
        value.put(name, StringNBT.valueOf(text));
        callback.accept(this);
        if (validator.test(text)) {
            setTextColor(14737632);
        } else {
            setTextColor(0xffff3f30);
        }
    }
}
