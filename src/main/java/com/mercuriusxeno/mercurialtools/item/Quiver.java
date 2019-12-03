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

public class Quiver extends Item {
    public Quiver() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        this.addPropertyOverride(ItemUtil.disabledProperty, ItemUtil.disablingPropertyGetter);
        setRegistryName(Names.QUIVER);
    }

    public static final ArrayList<Item> QUIVER_CONSUMES_THESE = new ArrayList<>(
            Arrays.asList(
                    Items.ARROW,
                    Items.SPECTRAL_ARROW,
                    Items.TIPPED_ARROW
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
            undoQuiverThing(stack, player);
        } else {
            doQuiverThing(stack, player);
        }
    }

    private static void undoQuiverThing(ItemStack quiverStack, PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;

        NonNullList<ItemStack> quiverItems = NbtUtil.getItems(quiverStack);
        for(int i = 0; i < quiverItems.size(); i++) {
            // after removing the stack from the util list, persist the changes.
            ItemStack takenStack = ItemStackHelper.getAndRemove(quiverItems, i);
            CompoundNBT tag = quiverStack.getTag();
            ItemStackHelper.saveAllItems(tag, quiverItems);
            quiverStack.setTag(tag);

            if (takenStack == ItemStack.EMPTY) {
                continue;
            }
            if (!playerInventory.addItemStackToInventory(takenStack)) {
                NbtUtil.addItemStackToContainerItem(quiverStack, takenStack);
                break;
            }
        }
    }

    private static void doQuiverThing(ItemStack quiverStack, PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;

        for(Item itemToConsume : QUIVER_CONSUMES_THESE) {
            // make sure we respect distinct nbt tags, we do this by
            // getting all items in the player inventory with distint NBTs
            NonNullList<ItemStack> distinctStacks = ItemUtil.getAllDistinctlyTaggedStacks(playerInventory, itemToConsume);

            for(ItemStack distinctStack : distinctStacks) {
                while (ItemUtil.getItemCountInInventory(playerInventory, distinctStack) > distinctStack.getMaxStackSize()) {
                    ItemStack stackFound = ItemUtil.getAndRemoveOneStack(playerInventory.mainInventory, distinctStack);
                    NbtUtil.addItemStackToContainerItem(quiverStack, stackFound);
                }
            }
        }
    }
}
