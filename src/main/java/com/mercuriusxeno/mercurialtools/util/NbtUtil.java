package com.mercuriusxeno.mercurialtools.util;

import com.mercuriusxeno.mercurialtools.reference.Constants;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;

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
}
