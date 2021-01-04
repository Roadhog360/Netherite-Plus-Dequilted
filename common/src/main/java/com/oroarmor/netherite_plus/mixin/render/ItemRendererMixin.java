package com.oroarmor.netherite_plus.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.oroarmor.netherite_plus.client.render.item.NetheriteTridentItemRenderer;
import com.oroarmor.netherite_plus.item.NetheritePlusItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;

import static com.oroarmor.netherite_plus.NetheritePlusMod.MOD_ID;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {
    @Inject(method = "render", at = @At(value = "HEAD"), cancellable = true)
    public void renderItem(ItemStack stack, ItemTransforms.TransformType renderMode, boolean leftHanded, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay, BakedModel model, CallbackInfo info) {
        if (!stack.isEmpty() && stack.getItem() == NetheritePlusItems.NETHERITE_TRIDENT.get()) {
            matrices.pushPose();
            boolean bl = renderMode == ItemTransforms.TransformType.GUI || renderMode == ItemTransforms.TransformType.GROUND || renderMode == ItemTransforms.TransformType.FIXED;
            if (stack.getItem() == NetheritePlusItems.NETHERITE_TRIDENT.get() && bl) {
                model = ((ItemRendererAccessor) this).getItemModelShaper().getModelManager().getModel(new ModelResourceLocation(MOD_ID + ":netherite_trident#inventory"));
            }

            model.getTransforms().getTransform(renderMode).apply(leftHanded, matrices);
            matrices.translate(-0.5D, -0.5D, -0.5D);
            if (model.isCustomRenderer() || stack.getItem() == NetheritePlusItems.NETHERITE_TRIDENT.get() && !bl) {
                NetheriteTridentItemRenderer.render(stack, renderMode, matrices, vertexConsumers, light, overlay);
            } else {
                RenderType renderLayer = ItemBlockRenderTypes.getRenderType(stack, true);
                VertexConsumer vertexConsumer4;
                vertexConsumer4 = ItemRenderer.getFoilBufferDirect(vertexConsumers, renderLayer, true, stack.hasFoil());

                ((ItemRendererAccessor) this).renderModelLists(model, stack, light, overlay, matrices, vertexConsumer4);
            }

            matrices.popPose();
            info.cancel();
        }
    }
}
