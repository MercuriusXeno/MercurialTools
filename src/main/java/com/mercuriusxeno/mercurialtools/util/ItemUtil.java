package com.mercuriusxeno.mercurialtools.util;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ItemUtil {
    /*
     * Properties used by items which have a function that gets disabled via NBT, set when the player right clicks, or by some other impetus.
     * This prevents the item from being used anymore, or stops it from performing some function, depending on the context/item it's used on.
     */
    public static final IItemPropertyGetter disablingPropertyGetter =
            new IItemPropertyGetter() {
                @OnlyIn(Dist.CLIENT)
                @Override
                public float call(ItemStack itemStack, @Nullable World worldIn, LivingEntity usingEntity) {
                    return NbtUtil.getIsDisabled(itemStack) ? 1.0F : 0.0F;
                }
            };

    public static final ResourceLocation disabledProperty = new ResourceLocation(Names.MOD_ID, Names.IS_DISABLED);

    /*
     * Flips the NBT value of the disabled flag in the itemstack's tag.
     */
    public static ActionResult<ItemStack> toggleDisable(PlayerEntity playerIn, Hand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        NbtUtil.setIsDisabled(itemStack, !NbtUtil.getIsDisabled(itemStack));
        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    /*
     * Gets the number of itemstacks in a non-null list of items. Differs slightly from the inventory version, since
     * it's checking a list of items, specifically, rather than a player inventory (including the cursor "held" stack)
     */
    public static int getItemCount(ItemStack itemStackWeWant, NonNullList<ItemStack> itemList) {
        int value = 0;
        for(ItemStack itemStack : itemList) {
            if (itemStack.isEmpty()) {
                continue;
            }
            if (!itemStack.isItemEqual(itemStackWeWant)) {
                continue;
            }
            value += ItemStack.areItemStackTagsEqual(itemStackWeWant, itemStack) ? itemStack.getCount() : 0;
        }
        return value;
    }

    /*
     * For a given itemstack (nbt sensitive), count the number in the player inventory as well as the amount held in the cursor, for QOL reasons.
     * Note that the cursor count doesn't work in creative mode, which causes strange behavior. But I don't care about creative mode behaviors - items aren't consumed anyway.
     */
    public static int getItemCountInInventory(PlayerInventory playerInventory, ItemStack itemToConsume) {
        int itemCount = ItemUtil.getItemCount(itemToConsume, playerInventory.mainInventory);
        // make sure we check for the item in the cursor, so that we don't steal items from you
        // while you're moving things around!
        // note that this doesn't work in creative mode, it will eat the stack in your inventory because it can't "see" the stack you're holding.
        // It's annoying but oh well.
        boolean isCursorHeldItemStackMatching =
                playerInventory.getItemStack().isItemEqual(itemToConsume)
                && ItemStack.areItemStackTagsEqual(playerInventory.getItemStack(), itemToConsume);
        itemCount += isCursorHeldItemStackMatching ? playerInventory.getItemStack().getCount() : 0;

        return itemCount;
    }

    /*
     * From a list of items, find a particular itemstack (including NBT) and remove the last ordinal one you can find
     */
    public static ItemStack getAndRemoveOneStack(NonNullList<ItemStack> inventory, ItemStack itemToPull, int amountToGrab) {
        // read backwards because it makes for a cleaner experience.
        for(int i = inventory.size() - 1; i >= 0; i--) {
            ItemStack stack = inventory.get(i);
            if (stack == ItemStack.EMPTY) {
                continue;
            }
            if (!stack.isItemEqual(itemToPull)) {
                continue;
            }
            if (ItemStack.areItemStackTagsEqual(stack, itemToPull)) {
                if (stack.getCount() <= amountToGrab) {
                    return ItemStackHelper.getAndRemove(inventory, i);
                } else {
                    ItemStack result = stack.copy();
                    result.setCount(amountToGrab);
                    stack.setCount(stack.getCount() - amountToGrab);
                    return result;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    /*
     * Gets all the items in the player inventory that match the item being passed in, returning a list of unique tag variants it finds.
     * This is what allows calling methods to distinguish between items with differing NBTs (tipped arrows, potions, et al)
     * This is also trawling through the container itemstack's contained items to perform "return" methods against, to keep the player stocked with
     * a stack of whatever is in it, as that's what most of the container items in the mod are for.
     */
    public static NonNullList<ItemStack> getAllDistinctlyTaggedStacks(PlayerInventory playerInventory, ItemStack container, Item itemToConsume) {
        ArrayList<ItemStack> matchingStacks = new ArrayList<>();
        for(int i = 0; i < playerInventory.mainInventory.size(); i++) {
            ItemStack stack = playerInventory.mainInventory.get(i);
            if (stack == ItemStack.EMPTY) {
                continue;
            }
            // they're not the same item.
            if (!stack.getItem().equals(itemToConsume)) {
                continue;
            }
            boolean isAlreadyFound = false;
            for(ItemStack alreadyFoundStack : matchingStacks) {
                if (ItemStack.areItemStackTagsEqual(alreadyFoundStack, stack)) {
                    isAlreadyFound = true;
                }
            }
            if (!isAlreadyFound) {
                matchingStacks.add(stack);
            }
        }
        NonNullList<ItemStack> containedItems = NbtUtil.getItems(container);
        for(int i = 0; i < containedItems.size(); i++) {
            ItemStack stack = containedItems.get(i);
            // they're not the same item.
            if (!stack.getItem().equals(itemToConsume)) {
                continue;
            }
            boolean isAlreadyFound = false;
            for(ItemStack alreadyFoundStack : matchingStacks) {
                if (ItemStack.areItemStackTagsEqual(alreadyFoundStack, stack)) {
                    isAlreadyFound = true;
                }
            }
            if (!isAlreadyFound) {
                matchingStacks.add(stack);
            }
        }
        int distinctCount = matchingStacks.size();
        NonNullList<ItemStack> results = NonNullList.withSize(distinctCount, ItemStack.EMPTY);
        for(int i = 0; i < distinctCount; i++) {
            results.set(i, matchingStacks.get(i));
        }
        return results;
    }

    /*
     * From a list of itemstacks, attempt to skim (last in first out) the amount we're asking for from the stacks.
     */
    public static ItemStack siphonStacks(ItemStack distinctStack, int amountWeWouldLike, NonNullList<ItemStack> containedItems) {
        ItemStack result = ItemStack.EMPTY;
        for (int i = containedItems.size() - 1; i >= 0 && amountWeWouldLike > 0; i--) {
            ItemStack containedItem = containedItems.get(i);
            if (containedItem == ItemStack.EMPTY) {
                continue;
            }
            // they're not the same item.
            if (!containedItem.isItemEqual(distinctStack)) {
                continue;
            }
            if (!ItemStack.areItemStackTagsEqual(containedItem, distinctStack)) {
                continue;
            }
            if (getItemCount(distinctStack, containedItems) >= amountWeWouldLike) {
                result = ItemStackHelper.getAndSplit(containedItems, i, amountWeWouldLike);
                amountWeWouldLike -= result.getCount();
            }
        }

        return result;
    }

    public static boolean isInventoryFull(NonNullList<ItemStack> inventory) {
        for(ItemStack checkInventoryForSpaceStack : inventory) {
            if (checkInventoryForSpaceStack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public static void undoContainment(ItemStack container, PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;
        if (ItemUtil.isInventoryFull(playerInventory.mainInventory)) {
            return;
        }

        NonNullList<ItemStack> containerItems = NbtUtil.getItems(container);
        for(int i = containerItems.size() - 1; i >= 0; i--) {
            // after removing the stack from the util list, persist the changes.
            ItemStack takenStack = ItemStackHelper.getAndRemove(containerItems, i);
            CompoundNBT tag = container.getTag();
            ItemStackHelper.saveAllItems(tag, containerItems);
            container.setTag(tag);
            if (takenStack == ItemStack.EMPTY) {
                continue;
            }
            if (!playerInventory.addItemStackToInventory(takenStack)) {
                NbtUtil.addItemStackToContainerItem(container, takenStack);
            };
            if (ItemUtil.isInventoryFull(playerInventory.mainInventory)) {
                return;
            }
        }
    }

    public static void doContainment(ItemStack container, PlayerEntity player, ArrayList<Item> containerConsumesThese, ArrayList<Item> itemExceptions) {
        PlayerInventory playerInventory = player.inventory;

        for(Item itemToConsume : containerConsumesThese) {
            // make sure we respect distinct nbt tags, we do this by
            // getting all items in the player inventory with distint NBTs
            NonNullList<ItemStack> distinctStacks = ItemUtil.getAllDistinctlyTaggedStacks(playerInventory, container, itemToConsume);

            for(ItemStack distinctStack : distinctStacks) {
                int amountWeWouldLike = itemExceptions.contains(distinctStack.getItem()) ? 0 : distinctStack.getMaxStackSize();
                int amountWeShouldConsume = ItemUtil.getItemCountInInventory(playerInventory, distinctStack) - amountWeWouldLike;
                while (amountWeShouldConsume > 0) {
                    ItemStack stackFound = ItemUtil.getAndRemoveOneStack(playerInventory.mainInventory, distinctStack, amountWeShouldConsume);
                    amountWeShouldConsume -= stackFound.getCount();
                    NbtUtil.addItemStackToContainerItem(container, stackFound);
                }

                NonNullList<ItemStack> containedItems = NbtUtil.getItems(container);

                // item exceptions are items we don't want back. this is used by the potion belt
                // on empty bottles
                if (amountWeWouldLike <= 0) {
                    continue;
                }

                amountWeWouldLike = distinctStack.getMaxStackSize() - ItemUtil.getItemCountInInventory(playerInventory, distinctStack);
                // if we have more than we need, just stop.
                if (amountWeWouldLike <= 0) {
                    continue;
                }

                // the opposite should occur for each distinct stack that isn't full when the container item has items in it
                ItemStack siphonedStack = ItemUtil.siphonStacks(distinctStack, amountWeWouldLike, containedItems);
                if (!playerInventory.addItemStackToInventory(siphonedStack)) {
                    NbtUtil.addItemStackToContainerItem(container, siphonedStack);
                }

                // no matter what just happened, persist the Nbt updates.
                CompoundNBT tag = container.getTag();
                ItemStackHelper.saveAllItems(tag, containedItems);
                container.setTag(tag);
            }
        }
    }
}
