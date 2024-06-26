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

package com.oroarmor.netherite_plus.screen;

import java.util.Map;

import com.mojang.logging.LogUtils;
import com.oroarmor.netherite_plus.NetheritePlusMod;
import com.oroarmor.netherite_plus.block.NetheritePlusBlocks;
import net.minecraft.screen.slot.ForgingSlotsManager;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.text.Text;
import net.minecraft.world.WorldEvents;

public class NetheriteAnvilScreenHandler extends ForgingScreenHandler {
    public static final int INGREDIENT_SLOT = 0;
    public static final int ADDITIONAL_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final boolean DEBUG_COST = false;
    public static final int MAX_NAME_LENGTH = 50;
    private int repairItemUsage;
    @Nullable
    private String newItemName;
    private final Property levelCost = Property.create();
    private static final int FAIL_COST = 0;
    private static final int BASE_COST = 1;
    private static final int ADDED_BASE_COST = 1;
    private static final int MATERIAL_REPAIR_COST = 1;
    private static final int SACRIFICE_REPAIR_COST = 2;
    private static final int INCOMPATIBLE_PENALTY_COST = 1;
    private static final int RENAME_COST = 1;
    private static final int INGREDIENT_SLOT_X = 27;
    private static final int ADDITIONAL_SLOT_X = 76;
    private static final int RESULT_SLOT_X = 134;
    private static final int SLOT_Y = 47;

    public NetheriteAnvilScreenHandler(int syncId, PlayerInventory inventory) {
        this(syncId, inventory, ScreenHandlerContext.EMPTY);
    }

    public NetheriteAnvilScreenHandler(int syncId, PlayerInventory inventory, ScreenHandlerContext context) {
        super(NetheritePlusScreenHandlers.NETHERITE_ANVIL, syncId, inventory, context);
        this.addProperty(levelCost);
    }

    @Override
    protected ForgingSlotsManager getForgingSlotsManager() {
        return ForgingSlotsManager.create()
                .input(0, 27, 47, stack -> true)
                .input(1, 76, 47, stack -> true)
                .output(2, 134, 47)
                .build();
    }

    public static int getNextCost(int cost) {
        return cost * 2 + 1;
    }

    @Override
    protected boolean canTakeOutput(PlayerEntity player, boolean present) {
        return (player.getAbilities().creativeMode || player.experienceLevel >= levelCost.get()) && levelCost.get() > 0;
    }

    @Override
    protected boolean canUse(BlockState state) {
        return state.isOf(NetheritePlusBlocks.NETHERITE_ANVIL_BLOCK);
    }

    public int getLevelCost() {
        return levelCost.get();
    }

    @Override
    protected void onTakeOutput(PlayerEntity player, ItemStack stack) {
        if (!player.getAbilities().creativeMode) {
            player.addExperienceLevels(-this.levelCost.get());
        }

        this.input.setStack(INGREDIENT_SLOT, ItemStack.EMPTY);
        if (repairItemUsage > 0) {
            ItemStack additionStack = this.input.getStack(ADDITIONAL_SLOT);
            if (!additionStack.isEmpty() && additionStack.getCount() > repairItemUsage) {
                additionStack.decrement(repairItemUsage);
                this.input.setStack(ADDITIONAL_SLOT, additionStack);
            } else {
                this.input.setStack(ADDITIONAL_SLOT, ItemStack.EMPTY);
            }
        } else {
            this.input.setStack(ADDITIONAL_SLOT, ItemStack.EMPTY);
        }

        levelCost.set(0);
        context.run((world, blockPos) -> world.syncWorldEvent(WorldEvents.ANVIL_USED, blockPos, 0));
    }

    public void setNewItemName(String string) {
        newItemName = string;
        if (getSlot(RESULT_SLOT).hasStack()) {
            ItemStack itemStack = getSlot(RESULT_SLOT).getStack();
            if (StringUtils.isBlank(string)) {
                itemStack.removeCustomName();
            } else {
                itemStack.setCustomName(Text.literal(newItemName));
            }
        }

        updateResult();
    }

    @Override
    public void updateResult() {
        ItemStack inputStack = this.input.getStack(INGREDIENT_SLOT);
        levelCost.set(BASE_COST);
        if (inputStack.isEmpty()) {
            this.output.setStack(0, ItemStack.EMPTY);
            levelCost.set(FAIL_COST);
        } else {
            ItemStack copiedInput = inputStack.copy();
            ItemStack addition = this.input.getStack(ADDITIONAL_SLOT);
            Map<Enchantment, Integer> currentEnchantments = EnchantmentHelper.get(copiedInput);
            int repairCost = inputStack.getRepairCost() + (addition.isEmpty() ? FAIL_COST : addition.getRepairCost());
            this.repairItemUsage = 0;
            int uses = 0;
            int isRename = 0;
            if (!addition.isEmpty()) {
                boolean addingEnchantmentBook = addition.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantmentNbt(addition).isEmpty();
                if (copiedInput.isDamageable() && copiedInput.getItem().canRepair(inputStack, addition)) {
                    int additionRepairAmount = Math.min(copiedInput.getDamage(), copiedInput.getMaxDamage() / 4);
                    if (additionRepairAmount <= 0) {
                        this.output.setStack(0, ItemStack.EMPTY);
                        levelCost.set(FAIL_COST);
                        return;
                    }

                    int repairs = 0;
                    for (; additionRepairAmount > 0 && repairs < addition.getCount(); ++repairs) {
                        int newDamage = copiedInput.getDamage() - additionRepairAmount;
                        copiedInput.setDamage(newDamage);
                        ++uses;
                        additionRepairAmount = Math.min(copiedInput.getDamage(), copiedInput.getMaxDamage() / 4);
                    }

                    repairItemUsage = repairs;
                } else {
                    if (!addingEnchantmentBook && (copiedInput.getItem() != addition.getItem() || !copiedInput.isDamageable())) {
                        this.output.setStack(0, ItemStack.EMPTY);
                        levelCost.set(0);
                        return;
                    }

                    if (copiedInput.isDamageable() && !addingEnchantmentBook) {
                        int inputDamage = inputStack.getMaxDamage() - inputStack.getDamage();
                        int additionDamage = addition.getMaxDamage() - addition.getDamage();
                        int addedDamage = additionDamage + copiedInput.getMaxDamage() * 12 / 100;
                        int combinedDamage = inputDamage + addedDamage;
                        int newDamage = copiedInput.getMaxDamage() - combinedDamage;
                        if (newDamage < 0) {
                            newDamage = 0;
                        }

                        if (newDamage < copiedInput.getDamage()) {
                            copiedInput.setDamage(newDamage);
                            uses += 2;
                        }
                    }

                    Map<Enchantment, Integer> addedEnchantments = EnchantmentHelper.get(addition);
                    boolean addedAnyEnchantment = false;
                    boolean failedEnchantmentAdded = false;

                    for(Enchantment addedEnchantment : addedEnchantments.keySet()) {
                        if (addedEnchantment != null) {
                            int currentLevel = currentEnchantments.getOrDefault(addedEnchantment, 0);
                            int addedLevel = addedEnchantments.get(addedEnchantment);
                            addedLevel = currentLevel == addedLevel ? addedLevel + 1 : Math.max(addedLevel, currentLevel);
                            boolean canAddEnchantment = addedEnchantment.isAcceptableItem(inputStack);
                            if (this.player.getAbilities().creativeMode || inputStack.isOf(Items.ENCHANTED_BOOK)) {
                                canAddEnchantment = true;
                            }

                            for(Enchantment currentEnchantment : currentEnchantments.keySet()) {
                                if (currentEnchantment != addedEnchantment && !currentEnchantment.canCombine(addedEnchantment)) {
                                    canAddEnchantment = false;
                                    ++uses;
                                }
                            }

                            if (!canAddEnchantment) {
                                failedEnchantmentAdded = true;
                            } else {
                                addedAnyEnchantment = true;
                                if (addedLevel > addedEnchantment.getMaxLevel()) {
                                    addedLevel = addedEnchantment.getMaxLevel();
                                }

                                currentEnchantments.put(addedEnchantment, addedLevel);
                                int rarityCost = switch (addedEnchantment.getRarity()) {
                                    case COMMON -> 1;
                                    case UNCOMMON -> 2;
                                    case RARE -> 4;
                                    case VERY_RARE -> 8;
                                };

                                rarityCost = Math.max(1, rarityCost / 2);

                                uses += rarityCost * addedLevel;
                                if (inputStack.getCount() > 1) {
                                    uses = 40;
                                }
                            }
                        }
                    }

                    if (failedEnchantmentAdded && !addedAnyEnchantment) {
                        this.output.setStack(0, ItemStack.EMPTY);
                        this.levelCost.set(0);
                        return;
                    }
                }
            }

            if (StringUtils.isBlank(newItemName)) {
                if (inputStack.hasCustomName()) {
                    isRename = 1;
                    uses += isRename;
                    copiedInput.removeCustomName();
                }
            } else if (!newItemName.equals(inputStack.getName().getString())) {
                isRename = 1;
                uses += isRename;
                copiedInput.setCustomName(Text.literal(newItemName));
            }

            // this is the important line that changes things
            double cost = (1d - NetheritePlusMod.CONFIG.anvil.xp_reduction.value()) * (repairCost + uses);

            levelCost.set(cost < BASE_COST ? BASE_COST : (int) cost);
            if (uses <= 0) {
                copiedInput = ItemStack.EMPTY;
            }

            if (isRename == uses && isRename > 0 && levelCost.get() >= 40) {
                levelCost.set(39);
            }

            if (levelCost.get() >= 40 && !player.getAbilities().creativeMode) {
                copiedInput = ItemStack.EMPTY;
            }

            if (!copiedInput.isEmpty()) {
                int copiedRepairCost = copiedInput.getRepairCost();
                if (!addition.isEmpty() && copiedRepairCost < addition.getRepairCost()) {
                    copiedRepairCost = addition.getRepairCost();
                }

                if (isRename != uses || isRename == 0) {
                    copiedRepairCost = getNextCost(copiedRepairCost);
                }

                copiedInput.setRepairCost(copiedRepairCost);
                EnchantmentHelper.set(currentEnchantments, copiedInput);
            }

            this.output.setStack(0, copiedInput);
            sendContentUpdates();
        }
    }
}