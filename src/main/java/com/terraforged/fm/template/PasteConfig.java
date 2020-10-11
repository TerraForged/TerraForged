/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.fm.template;

import com.google.gson.JsonObject;
import com.terraforged.fm.util.Json;

public class PasteConfig {

    public static final PasteConfig DEFAULT = new PasteConfig(0, false, false, false, false);

    public final int baseDepth;
    public final boolean pasteAir;
    public final boolean checkBounds;
    public final boolean replaceSolid;
    public final boolean updatePostPaste;

    public PasteConfig(int baseDepth, boolean pasteAir, boolean checkBounds, boolean replaceSolid, boolean updatePostPaste) {
        this.checkBounds = checkBounds;
        this.replaceSolid = replaceSolid;
        this.baseDepth = baseDepth;
        this.pasteAir = pasteAir;
        this.updatePostPaste = updatePostPaste;
    }

    @Override
    public String toString() {
        return "PasteConfig{" +
                "baseDepth=" + baseDepth +
                ", pasteAir=" + pasteAir +
                ", checkBounds=" + checkBounds +
                ", replaceSolid=" + replaceSolid +
                ", updatePostPaste=" + updatePostPaste +
                '}';
    }

    public static PasteConfig parse(JsonObject config) {
        if (config != null) {
            int baseDepth = Json.getInt("extend_base", config, 0);
            boolean pasteAir = Json.getBool("paste_air", config, false);
            boolean checkBounds = Json.getBool("check_bounds", config, false);
            boolean replaceSolid = Json.getBool("replace_solid", config, false);
            boolean updatePostPaste = Json.getBool("update_post_paste", config, false);
            return new PasteConfig(baseDepth, pasteAir, checkBounds, replaceSolid, updatePostPaste);
        }
        return PasteConfig.DEFAULT;
    }
}
