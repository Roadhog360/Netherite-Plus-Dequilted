/*
 * MIT License
 *
 * Copyright (c) 2021-2023 OroArmor (Eli Orona)
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

package com.oroarmor.netherite_plus.mixin.render;

import com.oroarmor.netherite_plus.NetheritePlusMod;
import com.oroarmor.netherite_plus.client.NetheritePlusTextures;
import net.minecraft.client.render.model.SpriteAtlasManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.HashMap;
import java.util.Map;

@Mixin(SpriteAtlasManager.class)
public class TexturedRenderLayersMixin {
    @ModifyVariable(at = @At("HEAD"), method = "<init>", argsOnly = true)
    private static Map<Identifier, Identifier> onAddDefaultTextures(Map<Identifier, Identifier> atlasIds) {
        atlasIds = new HashMap<>(atlasIds);
        atlasIds.put(NetheritePlusTextures.NETHERITE_SHULKER_BOXES_ATLAS_TEXTURE, NetheritePlusMod.id("netherite_shulker_boxes"));
        atlasIds.put(NetheritePlusTextures.NETHERITE_SHIELD_PATTERNS_ATLAS_TEXTURE, NetheritePlusMod.id("netherite_shield_patterns"));
        return atlasIds;
    }
}