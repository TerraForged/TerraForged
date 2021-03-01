/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.mixin.common;

import com.terraforged.mod.feature.TagConfigFixer;
import net.minecraft.block.Block;
import net.minecraft.tags.ITag;
import net.minecraft.world.gen.feature.template.TagMatchRuleTest;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Non-essential.
 * Fixes references to the base_stone_overworld block-tag. This is to work around a vanilla
 * bug where the active tag-manager instance changes between creation of the TagMatchRuleTest
 * and serialization of it causing tags to become orphaned from the active tag-manager and so
 * produces the 'unknown tag Tag@#####' message when trying to encode the TagMatchRuleTest.
 */
@Mixin(TagMatchRuleTest.class)
public class MixinTagMatchRuleTest {

    @Final
    @Shadow
    @Mutable
    private ITag<Block> tag;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(ITag<Block> tagIn, CallbackInfo info) {
        tag = TagConfigFixer.getFixedBlockTag(tagIn);
    }
}
