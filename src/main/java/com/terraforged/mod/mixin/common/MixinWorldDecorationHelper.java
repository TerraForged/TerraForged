package com.terraforged.mod.mixin.common;

import com.terraforged.api.feature.decorator.DecorationHelper;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldDecoratingHelper.class)
public class MixinWorldDecorationHelper implements DecorationHelper {

    @Final
    @Shadow
    private ISeedReader field_242889_a;
    @Final
    @Shadow
    private ChunkGenerator field_242890_b;

    @Override
    public ISeedReader getRegion() {
        return field_242889_a;
    }

    @Override
    public ChunkGenerator getGenerator() {
        return field_242890_b;
    }
}
