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

package com.oroarmor.netherite_plus.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.*;

import net.fabricmc.fabric.api.entity.event.v1.FabricElytraItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;

import java.util.UUID;

public class NetheriteElytraItem extends ElytraItem implements FabricElytraItem {

    private final ArmorMaterial material;
    private final int protection;
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public NetheriteElytraItem(Settings settings) {
        super(settings);
        material = NetheriteElytraArmorMaterials.NETHERITE_ELYTRA_MATERIAL;
        protection = material.getProtection(ArmorItem.ArmorSlot.CHESTPLATE);
        float toughness = material.getToughness();
        float knockbackResistance = material.getKnockbackResistance();
        if(protection > 0) {
            ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
            UUID uUID = UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E");
            builder.put(EntityAttributes.GENERIC_ARMOR, new EntityAttributeModifier(uUID, "Armor modifier", protection, EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_ARMOR_TOUGHNESS, new EntityAttributeModifier(uUID, "Armor toughness", toughness, EntityAttributeModifier.Operation.ADDITION));
            builder.put(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, new EntityAttributeModifier(uUID, "Armor knockback resistance", knockbackResistance, EntityAttributeModifier.Operation.ADDITION));
            this.attributeModifiers = builder.build();
        } else {
            attributeModifiers = null;
        }
    }

    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE;
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == ArmorItem.ArmorSlot.CHESTPLATE.getEquipmentSlot() && attributeModifiers != null
                ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    public int getEnchantability() {
        return protection > 0 ? this.material.getEnchantability() : 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return protection > 0;
    }
}