package com.mercuriusxeno.mercurialtools.container;

import com.mercuriusxeno.mercurialtools.block.ModBlocks;
import com.mercuriusxeno.mercurialtools.item.ModItems;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Map;

public class MercurialGrindstoneContainer extends Container {
    private final IInventory craftResultInventory = new CraftResultInventory();
    private final IInventory inventory = new Inventory(2) {
        /**
         * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think
         * it hasn't changed and skip it.
         */
        public void markDirty() {
            super.markDirty();
            MercurialGrindstoneContainer.this.onCraftMatrixChanged(this);
        }
    };
    private final IWorldPosCallable worldPosCallable;

    public MercurialGrindstoneContainer(int windowId, PlayerInventory playerInventory) {
        this(windowId, playerInventory, IWorldPosCallable.DUMMY);
    }

    public MercurialGrindstoneContainer(int windowId, PlayerInventory playerInventory, IWorldPosCallable worldPosCallable) {
        super(ModBlocks.MERCURIAL_GRINDSTONE_CONTAINER, windowId);
        this.worldPosCallable = worldPosCallable;
        this.addSlot(new Slot(this.inventory, 0, 49, 19) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            // in this case, the item in the first slot MUST be an enchantable or damageable item.
            // it's either an enchanted item we're stripping of enchantments with a book
            // or it's a tool we're "restoring" with mercurial blend.
            public boolean isItemValid(ItemStack stack) {
                return (stack.isDamageable() && stack.isRepairable()) || stack.isEnchanted();
            }
        });
        this.addSlot(new Slot(this.inventory, 1, 49, 40) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            // only mercurial blend and books are allowed here.
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() == Items.BOOK || stack.getItem() == ModItems.MERCURIAL_BLEND;
            }
        });

        this.addSlot(new Slot(this.craftResultInventory, 2, 129, 34) {
            /**
             * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
             */
            public boolean isItemValid(ItemStack stack) {
                return false;
            }

            public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
                consumeOrModifyCraftingInputs();
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
        if (inventoryIn == this.inventory) {
            this.determineCraftResult();
        }
    }

    private void consumeOrModifyCraftingInputs() {
        ItemStack itemstack = this.inventory.getStackInSlot(0);
        ItemStack itemstack1 = this.inventory.getStackInSlot(1);

        // unlike the vanilla grindstone, you can't do anything without both slots full
        boolean isBothSlotsFull = !itemstack.isEmpty() && !itemstack1.isEmpty();
        if (isBothSlotsFull) {
            boolean isStrippingRecipe = itemstack.isEnchanted() && itemstack1.getItem() == Items.BOOK;
            boolean isRestoringRecipe = itemstack.isDamageable() && itemstack.isRepairable() && itemstack1.getItem() == ModItems.MERCURIAL_BLEND;
            if (isStrippingRecipe) {
                // return a result that is a book with the first enchantment it finds
                this.inventory.setInventorySlotContents(0, getItemWithStrippedFirstEnchantment(itemstack));
                this.inventory.decrStackSize(1, 1);
                this.detectAndSendChanges();
            } else if (isRestoringRecipe) {
                this.inventory.setInventorySlotContents(0, ItemStack.EMPTY);
                this.inventory.decrStackSize(1, 1);
                this.detectAndSendChanges();
            }
        }
    }

    private void determineCraftResult() {
        ItemStack itemstack = this.inventory.getStackInSlot(0);
        ItemStack itemstack1 = this.inventory.getStackInSlot(1);
        // unlike the vanilla grindstone, you can't do anything without both slots full
        boolean isBothSlotsFull = !itemstack.isEmpty() && !itemstack1.isEmpty();
        if (!isBothSlotsFull) {
            // no valid results if neither slot has anything in it, straightforward
            this.craftResultInventory.setInventorySlotContents(0, ItemStack.EMPTY);
            this.detectAndSendChanges();
        } else {
            boolean isStrippingRecipe = itemstack.isEnchanted() && itemstack1.getItem() == Items.BOOK;
            boolean isRestoringRecipe = itemstack.isDamageable() && itemstack.isRepairable() && itemstack1.getItem() == ModItems.MERCURIAL_BLEND;
            if (isStrippingRecipe) {
                // return a result that is a book with the first enchantment it finds
                this.craftResultInventory.setInventorySlotContents(0, getBookFromStrippedEnchantment(itemstack));
                this.detectAndSendChanges();
            } else if (isRestoringRecipe) {
                // return a stack that has a drastically reduced repair cost
                this.craftResultInventory.setInventorySlotContents(0, getRestoredItem(itemstack));
                this.detectAndSendChanges();
            } else {
                // invalid recipe, no results
                this.craftResultInventory.setInventorySlotContents(0, ItemStack.EMPTY);
                this.detectAndSendChanges();
            }
        }
    }

    private ItemStack getRestoredItem(ItemStack itemstack) {
        ItemStack result = itemstack.copy();
        int newRepairCost = MathHelper.floor(Math.sqrt(result.getRepairCost()));
        result.setRepairCost(newRepairCost);
        return result;
    }

    private ItemStack getItemWithStrippedFirstEnchantment(ItemStack itemstack) {
        Map<Enchantment, Integer> enchantmentMap = EnchantmentHelper.getEnchantments(itemstack);
        for(Enchantment e : enchantmentMap.keySet()) {
            // we are arbitrarily grabbing the first in the list.
            ItemStack result = itemstack.copy();
            ListNBT enchantmentList = result.getEnchantmentTagList();
            ResourceLocation enchantmentId = e.getRegistryName();
            int tagToRemove = -1;
            for(int i = 0; i < enchantmentList.size(); i++) {
                CompoundNBT enchantmentTag = enchantmentList.getCompound(i);
                if (enchantmentTag.getString("id").equals(enchantmentId.toString())) {
                    // do stuff
                    tagToRemove = i;
                }
            }
            if (tagToRemove > -1) {
                enchantmentList.remove(tagToRemove);
            }
            CompoundNBT tag = result.getTag();
            tag.put("Enchantments", enchantmentList);
            result.setTag(tag);
            return result;
        }
        // this would happen if there's no enchantments, which doesn't make much sense.
        return itemstack;
    }

    private ItemStack getBookFromStrippedEnchantment(ItemStack itemstack) {
        ItemStack resultBook = new ItemStack(Items.ENCHANTED_BOOK);
        Map<Enchantment, Integer> enchantmentMap = EnchantmentHelper.getEnchantments(itemstack);
        for(Enchantment e : enchantmentMap.keySet()) {
            // we are arbitrarily grabbing the first in the list.
            EnchantedBookItem.addEnchantment(resultBook, new EnchantmentData(e, enchantmentMap.get(e)));
            // we leave immediately, this for loop is just a formality/null check.
            break;
        }
        return resultBook;
    }

    /**
     * Called when the container is closed.
     */
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.clearContainer(playerIn, playerIn.world, this.inventory);
    }

    /**
     * Determines whether supplied player can use this container
     */
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(worldPosCallable, playerIn, ModBlocks.MERCURIAL_GRINDSTONE);
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
            ItemStack itemstack2 = this.inventory.getStackInSlot(0);
            ItemStack itemstack3 = this.inventory.getStackInSlot(1);
            if (index == 2) {
                if (!this.mergeItemStack(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onSlotChange(itemstack1, itemstack);
            } else if (index != 0 && index != 1) {
                if (!itemstack2.isEmpty() && !itemstack3.isEmpty()) {
                    if (index >= 3 && index < 30) {
                        if (!this.mergeItemStack(itemstack1, 30, 39, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (index >= 30 && index < 39 && !this.mergeItemStack(itemstack1, 3, 30, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.mergeItemStack(itemstack1, 0, 2, false)) {
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
}
