package com.mercuriusxeno.mercurialtools.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

/// set of static helpers for manipulating itemstack nbt, since they're somewhat common.
public interface NbtUtil {
    public static void setIsDisabled(ItemStack itemStack, boolean b) {
        CompoundNBT tag;
        if (itemStack.hasTag()) {
            tag = itemStack.getTag();
        } else {
            tag = new CompoundNBT();
        }

        if (tag.contains("is_disabled")) {
            tag.putBoolean("is_disabled", b);
        } else {
            tag.putBoolean("is_disabled", false);
        }
        itemStack.setTag(tag);
    }

    public static boolean getIsDisabled(ItemStack itemStack) {
        CompoundNBT tag;
        if(!itemStack.hasTag()) {
            return false;
        } else {
            tag = itemStack.getTag();
        }

        if (!tag.contains("is_disabled")) {
            return false;
        }

        return tag.getBoolean("is_disabled");
    }
}
