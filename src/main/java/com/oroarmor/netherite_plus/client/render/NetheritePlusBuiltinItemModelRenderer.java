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

package com.oroarmor.netherite_plus.client.render;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.mojang.datafixers.util.Pair;
import com.oroarmor.netherite_plus.block.NetheritePlusBlocks;
import com.oroarmor.netherite_plus.block.NetheriteShulkerBoxBlock;
import com.oroarmor.netherite_plus.block.entity.NetheriteShulkerBoxBlockEntity;
import com.oroarmor.netherite_plus.client.NetheritePlusTextures;
import com.oroarmor.netherite_plus.item.NetheritePlusItems;
import net.minecraft.client.render.VertexConsumer;

import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.ShieldEntityModel;
import net.minecraft.client.render.entity.model.TridentEntityModel;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class NetheritePlusBuiltinItemModelRenderer implements SynchronousResourceReloader {

    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private final EntityModelLoader entityModelLoader;

    public NetheritePlusBuiltinItemModelRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher, EntityModelLoader entityModelLoader) {
        this.blockEntityRenderDispatcher = blockEntityRenderDispatcher;
        this.entityModelLoader = entityModelLoader;
    }

    private static final NetheriteShulkerBoxBlockEntity RENDER_NETHERITE_SHULKER_BOX = new NetheriteShulkerBoxBlockEntity(BlockPos.ORIGIN, NetheritePlusBlocks.NETHERITE_SHULKER_BOX.getDefaultState());
    private static final NetheriteShulkerBoxBlockEntity[] RENDER_NETHERITE_SHULKER_BOX_DYED = Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map(dyeColor -> new NetheriteShulkerBoxBlockEntity(dyeColor, BlockPos.ORIGIN, NetheritePlusBlocks.NETHERITE_SHULKER_BOX.getDefaultState())).toArray(NetheriteShulkerBoxBlockEntity[]::new);

    private ShieldEntityModel modelShield;
    private TridentEntityModel modelTrident;

    @Override
    public void reload(ResourceManager manager) {
//        this.modelShield = new ShieldEntityModel(this.entityModelLoader.getModelPart(EntityModelLayers.SHIELD));
//        this.modelTrident = new TridentEntityModel(this.entityModelLoader.getModelPart(EntityModelLayers.TRIDENT));
        modelShield = null;
        modelTrident = null;
    }

    public void render(ItemStack itemStack, ModelTransformationMode transformType, MatrixStack matrices, VertexConsumerProvider vertices, int light, int overlay) {
        if(modelShield == null) modelShield = new ShieldEntityModel(this.entityModelLoader.getModelPart(EntityModelLayers.SHIELD));
        if(modelTrident == null) modelTrident = new TridentEntityModel(this.entityModelLoader.getModelPart(EntityModelLayers.TRIDENT));
        if (itemStack.isOf(NetheritePlusItems.NETHERITE_TRIDENT)) {
            renderTrident(modelTrident, itemStack, transformType, matrices, vertices, light, overlay);
        } else if (itemStack.isOf(NetheritePlusItems.NETHERITE_SHIELD)) {
            renderShield(modelShield, itemStack, transformType, matrices, vertices, light, overlay);
        } else if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof NetheriteShulkerBoxBlock block) {
            NetheriteShulkerBoxBlockEntity entity;
            DyeColor dyecolor = block.getColor();
            if (dyecolor == null) {
                entity = RENDER_NETHERITE_SHULKER_BOX;
            } else {
                entity = RENDER_NETHERITE_SHULKER_BOX_DYED[dyecolor.getId()];
            }
            blockEntityRenderDispatcher.renderEntity(entity, matrices, vertices, light, overlay);
        }
    }

    public void renderShield(ShieldEntityModel model, ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        boolean bl = stack.getSubNbt("BlockEntityTag") != null;
        matrices.push();
        matrices.scale(1.0F, -1.0F, -1.0F);
        Identifier id = bl ? NetheritePlusTextures.NETHERITE_SHIELD_TEXTURE : NetheritePlusTextures.NETHERITE_SHIELD_TEXTURE_NOPATTERN;
        VertexConsumer vertexConsumer = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, model.getLayer(id), true, stack.hasGlint());
        model.getHandle().render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        if (bl) {
            List<Pair<RegistryEntry<BannerPattern>, DyeColor>> list = BannerBlockEntity.getPatternsFromNbt(ShieldItem.getColor(stack), BannerBlockEntity.getPatternListNbt(stack));
            BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, light, overlay, model.getPlate(), NetheritePlusTextures.NETHERITE_SHIELD_BASE, false, list, stack.hasGlint());
        } else {
            model.getPlate().render(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        }

        matrices.pop();
    }

    public static void renderTrident(TridentEntityModel model, ItemStack stack, ModelTransformationMode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();
        matrices.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer vertexConsumer2 = ItemRenderer.getDirectItemGlintConsumer(vertexConsumers, model.getLayer(NetheritePlusTextures.NETHERITE_TRIDENT_TEXTURE), false, stack.hasGlint());
        model.render(matrices, vertexConsumer2, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
        matrices.pop();
    }

//    @Override
//    public Identifier getQuiltId() {
//        return id("netherite_plus_builtin_item_model_reloader");
//    }
}