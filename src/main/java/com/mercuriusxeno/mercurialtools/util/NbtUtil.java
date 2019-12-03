package com.mercuriusxeno.mercurialtools.util;

import com.mercuriusxeno.mercurialtools.reference.Constants;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Tuple;

import java.util.ArrayList;

/// set of static helpers for manipulating itemstack nbt, since they're somewhat common.
public class NbtUtil {
    public static void setIsDisabled(ItemStack itemStack, boolean b) {
        CompoundNBT tag;
        if (itemStack.hasTag()) {
            tag = itemStack.getTag();
        } else {
            tag = new CompoundNBT();
        }

        tag.putBoolean(Names.IS_DISABLED, b);
        itemStack.setTag(tag);
    }

    public static boolean getIsDisabled(ItemStack itemStack) {
        CompoundNBT tag;
        if(!itemStack.hasTag()) {
            return false;
        } else {
            tag = itemStack.getTag();
        }

        if (!tag.contains(Names.IS_DISABLED)) {
            return false;
        }

        return tag.getBoolean(Names.IS_DISABLED);
    }

    public static NonNullList<ItemStack> getItems(ItemStack itemStack) {
        CompoundNBT tag;
        if (!itemStack.hasTag()) {
            return NonNullList.withSize(1, ItemStack.EMPTY);
        } else {
            tag = itemStack.getTag();
        }

        if (!tag.contains(Names.ITEMS, 9)) {
            return NonNullList.withSize(1, ItemStack.EMPTY);
        }

        ListNBT tagItems = tag.getList("Items", 10);
        int listSize = tagItems.size();

        NonNullList<ItemStack> loadStacks = NonNullList.withSize(listSize, ItemStack.EMPTY);

        ItemStackHelper.loadAllItems(tag, loadStacks);

        return loadStacks;
    }

    public static void addItemStackToContainerItem(ItemStack containerItem, ItemStack depositedItem) {
        CompoundNBT tag;
        if (containerItem.hasTag()) {
            tag = containerItem.getTag();
        } else {
            tag = new CompoundNBT();
        }

        if (!tag.contains(Names.ITEMS, 9)) {
            tag.put(Names.ITEMS, new ListNBT());
        }

        ListNBT tagItems = tag.getList("Items", 10);
        int listSize = tagItems.size();

        // whatever the list size was, assume it will grow by one for safety's sake.
        NonNullList<ItemStack> containedStacks = NonNullList.withSize(listSize + 1, ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, containedStacks);

        int itemsInStack = depositedItem.getCount();
        for(int stackIndex = 0; stackIndex < containedStacks.size(); stackIndex++) {
            ItemStack stack = containedStacks.get(stackIndex);
            if (stack.isItemEqual(depositedItem)) {
                int actuallyDeposited = stack.getMaxStackSize() - stack.getCount();
                int newStackSize = Math.min(stack.getCount() + itemsInStack, stack.getMaxStackSize());
                itemsInStack -= actuallyDeposited;
                stack.setCount(newStackSize);
            } else if(stack == ItemStack.EMPTY) {
                containedStacks.set(stackIndex, depositedItem);
            }
        }

        ItemStackHelper.saveAllItems(tag, containedStacks);

        containerItem.setTag(tag);
    }

    public static NonNullList<ItemStack> getAllDistinctlyTaggedStacks(ItemStack container) {
        ArrayList<ItemStack> matchingStacks = new ArrayList<>();
        NonNullList<ItemStack> containedItems = NbtUtil.getItems(container);
        for(int i = 0; i < containedItems.size(); i++) {
            ItemStack stack = containedItems.get(i);
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

    public static ArrayList<Tuple<String, Integer>> getDistinctItemCountsForDisplay(ItemStack stack) {
        NonNullList<ItemStack> distinctItems = getAllDistinctlyTaggedStacks(stack);
        NonNullList<ItemStack> containedItems = NbtUtil.getItems(stack);
        ArrayList<Tuple<String, Integer>> results = new ArrayList<>();
        for(ItemStack distinctItem : distinctItems) {
            if (distinctItem == ItemStack.EMPTY) {
                continue;
            }
            int count = 0;
            for (int i = 0; i < containedItems.size(); i++) {
                ItemStack foundStack = containedItems.get(i);
                if (foundStack == ItemStack.EMPTY) {
                    continue;
                }
                if (!foundStack.isItemEqual(distinctItem)) {
                    continue;
                }
                if (!ItemStack.areItemStackTagsEqual(foundStack, distinctItem)) {
                    continue;
                }
                count += foundStack.getCount();
            }
            results.add(new Tuple<>(distinctItem.getDisplayName().getFormattedText(), count));
        }
        return results;
    }
}
