package com.mercuriusxeno.mercurialtools.util;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class ItemUtil {
    public static final IItemPropertyGetter disablingPropertyGetter =
            new IItemPropertyGetter() {
                @OnlyIn(Dist.CLIENT)
                @Override
                public float call(ItemStack itemStack, @Nullable World worldIn, LivingEntity usingEntity) {
                    return NbtUtil.getIsDisabled(itemStack) ? 1.0F : 0.0F;
                }
            };

    public static final ResourceLocation disabledProperty = new ResourceLocation(Names.MOD_ID, Names.IS_DISABLED);

    public static ActionResult<ItemStack> toggleDisable(PlayerEntity playerIn, Hand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        NbtUtil.setIsDisabled(itemStack, !NbtUtil.getIsDisabled(itemStack));
        return new ActionResult<>(ActionResultType.PASS, itemStack);
    }

    public static int getItemCount(Item item, NonNullList<ItemStack> itemList) {
        int value = 0;
        for(ItemStack itemStack : itemList) {
            if (itemStack.isEmpty()) {
                continue;
            }
            value += itemStack.getItem().equals(item) ? itemStack.getCount() : 0;
        }
        return value;
    }

    public static ItemStack createStackFromItem(Item itemToConsume) {
        return new ItemStack(itemToConsume, 1);
    }

    public static int getItemCountInInventory(PlayerInventory playerInventory, Item itemToConsume) {
        int itemCount = ItemUtil.getItemCount(itemToConsume, playerInventory.mainInventory);
        // make sure we check for the item in the cursor, so that we don't steal items from you
        // while you're moving things around!
        itemCount += playerInventory.getItemStack().getItem().equals(itemToConsume) ? playerInventory.getItemStack().getCount() : 0;
        return itemCount;
    }

    public static ItemStack getAndRemoveOneStack(NonNullList<ItemStack> inventory, Item itemToPull) {
        // read backwards because it makes for a cleaner experience.
        for(int i = inventory.size() - 1; i >= 0; i--) {
            ItemStack stack = inventory.get(i);
            if (stack == ItemStack.EMPTY) {
                continue;
            }
            if (stack.getItem().equals(itemToPull)) {
                return ItemStackHelper.getAndRemove(inventory, i);
            }
        }
        return ItemStack.EMPTY;
    }
}
