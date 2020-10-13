package com.terraforged.mixin.client;

import com.terraforged.api.level.client.ClientLevelManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(WorldOptionsScreen.class)
public class MixinWorldOptionsScreen {

    @Shadow
    private Optional<BiomeGeneratorTypeScreens> field_239040_n_;
    @Shadow
    private DimensionGeneratorSettings field_239039_m_;

    @Inject(method = "func_239048_a_", at = @At("HEAD"))
    public void setup(CreateWorldScreen screen, Minecraft minecraft, FontRenderer fontRenderer, CallbackInfo ci) {
        Optional<BiomeGeneratorTypeScreens> option = ClientLevelManager.getDefault();
        if (option.isPresent()) {
            DimensionGeneratorSettings settings = field_239039_m_;
            long seed = settings.getSeed();
            boolean chest = settings.hasBonusChest();
            boolean structures = settings.doesGenerateFeatures();
            DynamicRegistries.Impl registries = screen.field_238934_c_.func_239055_b_();
            field_239040_n_ = option;
            field_239039_m_ = option.get().func_241220_a_(registries, seed, structures, chest);
        }
    }
}
