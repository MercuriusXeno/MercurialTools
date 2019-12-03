package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.ItemUtil;
import com.mercuriusxeno.mercurialtools.util.NbtUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

public class PotionBelt extends Item {
    public PotionBelt() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        this.addPropertyOverride(ItemUtil.disabledProperty, ItemUtil.disablingPropertyGetter);
        setRegistryName(Names.POTION_BELT);
    }

    public static final ArrayList<Item> BELT_CONSUMES_THESE = new ArrayList<>(
            Arrays.asList(
                    Items.POTION,
                    Items.LINGERING_POTION,
                    Items.SPLASH_POTION
            )
    );

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        return ItemUtil.toggleDisable(playerIn, handIn);
    }

    /**
     * Called each tick as long the item is on a player inventory. Uses by maps to check if is on a player hand and
     * update it's contents.
     */
    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (worldIn.isRemote()) {
            return;
        }

        if (!(entityIn instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) entityIn;

        if (NbtUtil.getIsDisabled(stack)) {
            undoBeltThing(stack, player);
        } else {
            doBeltThing(stack, player);
        }
    }

    private static void undoBeltThing(ItemStack beltStack, PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;

        NonNullList<ItemStack> beltItems = NbtUtil.getItems(beltStack);
        for(int i = 0; i < beltItems.size(); i++) {
            // after removing the stack from the util list, persist the changes.
            ItemStack takenStack = ItemStackHelper.getAndRemove(beltItems, i);
            CompoundNBT tag = beltStack.getTag();
            ItemStackHelper.saveAllItems(tag, beltItems);
            beltStack.setTag(tag);

            if (takenStack == ItemStack.EMPTY) {
                continue;
            }
            if (!playerInventory.addItemStackToInventory(takenStack)) {
                NbtUtil.addItemStackToContainerItem(beltStack, takenStack);
                break;
            }
        }
    }

    private static void doBeltThing(ItemStack beltStack, PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;

        for(Item itemToConsume : BELT_CONSUMES_THESE) {
            // make sure we respect distinct nbt tags, we do this by
            // getting all items in the player inventory with distint NBTs
            NonNullList<ItemStack> distinctStacks = ItemUtil.getAllDistinctlyTaggedStacks(playerInventory, itemToConsume);

            for(ItemStack distinctStack : distinctStacks) {
                while (ItemUtil.getItemCountInInventory(playerInventory, distinctStack) > distinctStack.getMaxStackSize()) {
                    ItemStack stackFound = ItemUtil.getAndRemoveOneStack(playerInventory.mainInventory, distinctStack);
                    NbtUtil.addItemStackToContainerItem(beltStack, stackFound);
                }
            }
        }
    }
}
