package com.mercuriusxeno.mercurialtools.container;

import com.mercuriusxeno.mercurialtools.block.ModBlocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class FrugalAnvilContainer extends Container {
    private static final Logger LOGGER = LogManager.getLogger();
    private final IInventory outputSlot = new CraftResultInventory();
    private final IInventory inputSlots = new Inventory(2) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void markDirty() {
            super.markDirty();
            FrugalAnvilContainer.this.onCraftMatrixChanged(this);
        }
    };
    private final IntReferenceHolder maximumCost = IntReferenceHolder.single();
    private final IWorldPosCallable worldPosCallable;
    public int materialCost;
    private String repairedItemName;
    private final PlayerEntity player;

    public FrugalAnvilContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, IWorldPosCallable.DUMMY);
    }

    public FrugalAnvilContainer(int windowId, PlayerInventory playerInventory, final IWorldPosCallable worldPosCallable) {
        super(ModBlocks.FRUGAL_ANVIL_CONTAINER, windowId);
        this.worldPosCallable = worldPosCallable;
        this.player = playerInventory.player;
        this.trackInt(this.maximumCost);
        this.addSlot(new Slot(this.inputSlots, 0, 27, 47));
        this.addSlot(new Slot(this.inputSlots, 1, 76, 47));
        this.addSlot(new Slot(this.outputSlot, 2, 134, 47) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            /**
             * Return whether this slot's stack can be taken from this slot.
             */
            public boolean canTakeStack(PlayerEntity playerIn) {
                return (playerIn.abilities.isCreativeMode || playerIn.experienceLevel >= FrugalAnvilContainer.this.maximumCost.get())
                        && FrugalAnvilContainer.this.maximumCost.get() > 0 && this.getHasStack();
            }

            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
                if (!thePlayer.abilities.isCreativeMode) {
                    thePlayer.addExperienceLevel(-FrugalAnvilContainer.this.maximumCost.get());
                }

                FrugalAnvilContainer.this.inputSlots.setInventorySlotContents(0, ItemStack.EMPTY);
                if (FrugalAnvilContainer.this.materialCost > 0) {
                    ItemStack itemstack = FrugalAnvilContainer.this.inputSlots.getStackInSlot(1);
                    if (!itemstack.isEmpty() && itemstack.getCount() > FrugalAnvilContainer.this.materialCost) {
                        itemstack.shrink(FrugalAnvilContainer.this.materialCost);
                        FrugalAnvilContainer.this.inputSlots.setInventorySlotContents(1, itemstack);
                    } else {
                        FrugalAnvilContainer.this.inputSlots.setInventorySlotContents(1, ItemStack.EMPTY);
                    }
                } else {
                    FrugalAnvilContainer.this.inputSlots.setInventorySlotContents(1, ItemStack.EMPTY);
                }

                FrugalAnvilContainer.this.maximumCost.set(0);
                return stack;
            }
        });

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142));
        }

    }

    /**
     * Callback for when the crafting matrix is changed.
     */
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        super.onCraftMatrixChanged(inventoryIn);
        if (inventoryIn == this.inputSlots) {
            this.updateRepairOutput();
        }

    }

    /**
     * called when the Anvil Input Slot changes, calculates the new result and puts it in the output slot
     */
    public void updateRepairOutput() {
        ItemStack itemstack = this.inputSlots.getStackInSlot(0);
        this.maximumCost.set(1);
        int experienceCost = 0;
        int repairCost = 0;
        int itemNameAdditionalCost = 0;
        if (itemstack.isEmpty()) {
            this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
            this.maximumCost.set(0);
        } else {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = this.inputSlots.getStackInSlot(1);
            Map<Enchantment, Integer> firstItemEnchantments = EnchantmentHelper.getEnchantments(itemstack1);
            repairCost = repairCost + itemstack.getRepairCost() + (itemstack2.isEmpty() ? 0 : itemstack2.getRepairCost());
            this.materialCost = 0;
            boolean isEnchantingWithBook = false;

            if (!itemstack2.isEmpty()) {
                isEnchantingWithBook = itemstack2.getItem() == Items.ENCHANTED_BOOK && !EnchantedBookItem.getEnchantments(itemstack2).isEmpty();
                if (itemstack1.isDamageable() && itemstack1.getItem().getIsRepairable(itemstack, itemstack2)) {
                    int repairFactor = Math.min(itemstack1.getDamage(), itemstack1.getMaxDamage() / 4);
                    if (repairFactor <= 0) {
                        this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                        this.maximumCost.set(0);
                        return;
                    }

                    int materialsUsed;
                    for(materialsUsed = 0; repairFactor > 0 && materialsUsed < itemstack2.getCount(); ++materialsUsed) {
                        int newItemDamage = itemstack1.getDamage() - repairFactor;
                        itemstack1.setDamage(newItemDamage);
                        ++experienceCost;
                        repairFactor = Math.min(itemstack1.getDamage(), itemstack1.getMaxDamage() / 4);
                    }

                    this.materialCost = materialsUsed;
                } else {
                    if (!isEnchantingWithBook && (itemstack1.getItem() != itemstack2.getItem() || !itemstack1.isDamageable())) {
                        this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                        this.maximumCost.set(0);
                        return;
                    }

                    if (itemstack1.isDamageable() && !isEnchantingWithBook) {
                        int firstItemDurability = itemstack.getMaxDamage() - itemstack.getDamage();
                        int secondItemDurability = itemstack2.getMaxDamage() - itemstack2.getDamage();
                        int repairAmountBasedOnSecondItem = secondItemDurability + itemstack1.getMaxDamage() * 12 / 100;
                        int resultDurability = firstItemDurability + repairAmountBasedOnSecondItem;
                        int resultDurabilityConvertedToDamage = itemstack1.getMaxDamage() - resultDurability;
                        if (resultDurabilityConvertedToDamage < 0) {
                            resultDurabilityConvertedToDamage = 0;
                        }

                        if (resultDurabilityConvertedToDamage < itemstack1.getDamage()) {
                            itemstack1.setDamage(resultDurabilityConvertedToDamage);
                            experienceCost += 2;
                        }
                    }

                    Map<Enchantment, Integer> secondItemEnchantments = EnchantmentHelper.getEnchantments(itemstack2);
                    boolean isAbleToEnchant = false;
                    boolean isAbortingEnchantment = false;

                    for(Enchantment secondItemEnchantment : secondItemEnchantments.keySet()) {
                        if (secondItemEnchantment != null) {
                            int existingFirstItemEnchantmentLevel = firstItemEnchantments.containsKey(secondItemEnchantment) ? firstItemEnchantments.get(secondItemEnchantment) : 0;
                            int secondItemEnchantmentLevel = secondItemEnchantments.get(secondItemEnchantment);
                            secondItemEnchantmentLevel = existingFirstItemEnchantmentLevel == secondItemEnchantmentLevel ? secondItemEnchantmentLevel + 1 : Math.max(secondItemEnchantmentLevel, existingFirstItemEnchantmentLevel);
                            boolean isEnchantmentValid = secondItemEnchantment.canApply(itemstack);
                            if (this.player.abilities.isCreativeMode || itemstack.getItem() == Items.ENCHANTED_BOOK) {
                                isEnchantmentValid = true;
                            }

                            for(Enchantment enchantment : firstItemEnchantments.keySet()) {
                                if (enchantment != secondItemEnchantment && !secondItemEnchantment.isCompatibleWith(enchantment)) {
                                    isEnchantmentValid = false;
                                    ++experienceCost;
                                }
                            }

                            if (!isEnchantmentValid) {
                                isAbortingEnchantment = true;
                            } else {
                                isAbleToEnchant = true;
                                if (secondItemEnchantmentLevel > secondItemEnchantment.getMaxLevel()) {
                                    secondItemEnchantmentLevel = secondItemEnchantment.getMaxLevel();
                                }

                                firstItemEnchantments.put(secondItemEnchantment, secondItemEnchantmentLevel);
                                int rarityLevel = 0;
                                switch(secondItemEnchantment.getRarity()) {
                                    case COMMON:
                                        rarityLevel = 1;
                                        break;
                                    case UNCOMMON:
                                        rarityLevel = 2;
                                        break;
                                    case RARE:
                                        rarityLevel = 4;
                                        break;
                                    case VERY_RARE:
                                        rarityLevel = 8;
                                }

                                if (isEnchantingWithBook) {
                                    rarityLevel = Math.max(1, rarityLevel / 2);
                                }

                                experienceCost += rarityLevel * secondItemEnchantmentLevel;
                                if (itemstack.getCount() > 1) {
                                    experienceCost = 40;
                                }
                            }
                        }
                    }

                    if (isAbortingEnchantment && !isAbleToEnchant) {
                        this.outputSlot.setInventorySlotContents(0, ItemStack.EMPTY);
                        this.maximumCost.set(0);
                        return;
                    }
                }
            }

            if (StringUtils.isBlank(this.repairedItemName)) {
                if (itemstack.hasDisplayName()) {
                    itemNameAdditionalCost = 1;
                    experienceCost += itemNameAdditionalCost;
                    itemstack1.clearCustomName();
                }
            } else if (!this.repairedItemName.equals(itemstack.getDisplayName().getString())) {
                itemNameAdditionalCost = 1;
                experienceCost += itemNameAdditionalCost;
                itemstack1.setDisplayName(new StringTextComponent(this.repairedItemName));
            }
            if (isEnchantingWithBook && !itemstack1.isBookEnchantable(itemstack2)) itemstack1 = ItemStack.EMPTY;

            // basically the whole point of this class is right here - when setting the experience cost of an op, it's drastically lower than normal.
            this.maximumCost.set(MathHelper.ceil((repairCost + experienceCost) / 3F ));
            if (experienceCost <= 0) {
                itemstack1 = ItemStack.EMPTY;
            }

            if (itemNameAdditionalCost == experienceCost && itemNameAdditionalCost > 0 && this.maximumCost.get() >= 40) {
                this.maximumCost.set(39);
            }

            if (this.maximumCost.get() >= 40 && !this.player.abilities.isCreativeMode) {
                itemstack1 = ItemStack.EMPTY;
            }

            if (!itemstack1.isEmpty()) {
                int firstItemRepairCost = itemstack1.getRepairCost();
                if (!itemstack2.isEmpty() && firstItemRepairCost < itemstack2.getRepairCost()) {
                    firstItemRepairCost = itemstack2.getRepairCost();
                }

                if (itemNameAdditionalCost != experienceCost || itemNameAdditionalCost == 0) {
                    firstItemRepairCost = kindaDoubleRepairCost(firstItemRepairCost);
                }

                itemstack1.setRepairCost(firstItemRepairCost);
                EnchantmentHelper.setEnchantments(firstItemEnchantments, itemstack1);
            }

            this.outputSlot.setInventorySlotContents(0, itemstack1);
            this.detectAndSendChanges();
        }
    }

    public static int kindaDoubleRepairCost(int repairCost) {
        return repairCost * 2 + 1;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.worldPosCallable.consume((p_216973_2_, p_216973_3_) -> {
            this.clearContainer(playerIn, p_216973_2_, this.inputSlots);
        });
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.worldPosCallable.applyOrElse((world, blockPos) -> {
            return playerIn.getDistanceSq((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 0.5D, (double)blockPos.getZ() + 0.5D) <= 64.0D;
        }, true);
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index == 2) {
                if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 0 && index != 1) {
                if (index >= 3 && index < 39 && !this.mergeItemStack(itemstack1, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }

    /**
     * used by the Anvil GUI to update the Item Name being typed by the player
     */
    public void updateItemName(String newName) {
        this.repairedItemName = newName;
        if (this.getSlot(2).getHasStack()) {
            ItemStack itemstack = this.getSlot(2).getStack();
            if (StringUtils.isBlank(newName)) {
                itemstack.clearCustomName();
            } else {
                itemstack.setDisplayName(new StringTextComponent(this.repairedItemName));
            }
        }

        this.updateRepairOutput();
    }

    @OnlyIn(Dist.CLIENT)
    public int clientOnlyGetRepairCost() {
        return this.maximumCost.get();
    }

    public void setMaximumCost(int value) {
        this.maximumCost.set(value);
    }
}
