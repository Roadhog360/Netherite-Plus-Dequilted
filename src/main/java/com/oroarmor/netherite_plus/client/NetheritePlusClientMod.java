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

package com.oroarmor.netherite_plus.client;

import java.util.LinkedList;
import java.util.Queue;

import com.oroarmor.netherite_plus.NetheritePlusMod;
import com.oroarmor.netherite_plus.block.NetheritePlusBlocks;
import com.oroarmor.netherite_plus.client.render.NetheriteBeaconBlockEntityRenderer;
import com.oroarmor.netherite_plus.client.render.NetheritePlusBuiltinItemModelRenderer;
import com.oroarmor.netherite_plus.client.render.NetheriteShulkerBoxBlockEntityRenderer;
import com.oroarmor.netherite_plus.item.NetheritePlusItems;
import com.oroarmor.netherite_plus.network.LavaVisionUpdatePacket;
import com.oroarmor.netherite_plus.screen.NetheritePlusScreenHandlers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;

import static com.oroarmor.netherite_plus.NetheritePlusMod.id;

public class NetheritePlusClientMod implements ClientModInitializer {
    public static final Queue<Integer> TRIDENT_QUEUE = new LinkedList<>();
    public static double LAVA_VISION_DISTANCE = NetheritePlusMod.CONFIG.graphics.lava_vision_distance.value();

    public void onInitializeClient() {
        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            ClientPlayNetworking.registerReceiver(LavaVisionUpdatePacket.ID, (minecraft, listener, buf, responseSender) -> {
                LAVA_VISION_DISTANCE = buf.readDouble();
            });

            ClientPlayNetworking.registerReceiver(id("lava_vision_update"), (minecraft, listener, buf, sender) -> {
                NetheritePlusClientMod.LAVA_VISION_DISTANCE = buf.getDouble(0);
            });

            ClientPlayNetworking.registerReceiver(id("netherite_trident"), (minecraft, listener, buf, responseSender) -> TRIDENT_QUEUE.add(buf.readInt()));
        });

//        NetheritePlusTextures.register();

        BlockEntityRendererRegistry.register(NetheritePlusBlocks.NETHERITE_SHULKER_BOX_ENTITY, NetheriteShulkerBoxBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(NetheritePlusBlocks.NETHERITE_BEACON_BLOCK_ENTITY, NetheriteBeaconBlockEntityRenderer::new);

        NetheritePlusModelProvider.registerItemsWithModelProvider();
        NetheritePlusScreenHandlers.initializeClient();

        if (NetheritePlusMod.CONFIG.enabled.beacon.value()) {
            BlockRenderLayerMap.INSTANCE.putBlock(NetheritePlusBlocks.NETHERITE_BEACON, RenderLayer.getCutout());
        }
    }

    public static void registerBuiltinItemRenderers(MinecraftClient client) {
        NetheritePlusBuiltinItemModelRenderer builtinItemModelRenderer = new NetheritePlusBuiltinItemModelRenderer(client.getBlockEntityRenderDispatcher(), client.getEntityModelLoader());

        builtinItemModelRenderer.reload(client.getResourceManager());

        BuiltinItemRendererRegistry.DynamicItemRenderer dynamicItemRenderer = builtinItemModelRenderer::render;
        if (NetheritePlusMod.CONFIG.enabled.shulker_boxes.value()) {
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_WHITE_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_ORANGE_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_MAGENTA_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_LIGHT_BLUE_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_YELLOW_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_LIME_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_PINK_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_GRAY_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_LIGHT_GRAY_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_CYAN_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_PURPLE_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_BLUE_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_BROWN_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_GREEN_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_RED_SHULKER_BOX, dynamicItemRenderer);
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_BLACK_SHULKER_BOX, dynamicItemRenderer);
        }

        if (NetheritePlusMod.CONFIG.enabled.shields.value()) {
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_SHIELD, dynamicItemRenderer);
        }
        if (NetheritePlusMod.CONFIG.enabled.trident.value()) {
            BuiltinItemRendererRegistry.INSTANCE.register(NetheritePlusItems.NETHERITE_TRIDENT, dynamicItemRenderer);
        }
    }
}