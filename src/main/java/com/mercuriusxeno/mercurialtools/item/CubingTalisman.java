package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.setup.ModSetup;
import com.mercuriusxeno.mercurialtools.transmutation.TransmutationRecipes;
import com.mercuriusxeno.mercurialtools.util.ItemPair;
import com.mercuriusxeno.mercurialtools.util.ItemUtil;
import com.mercuriusxeno.mercurialtools.util.NbtUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;

public class CubingTalisman extends Item {
    public CubingTalisman() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        this.addPropertyOverride(ItemUtil.disabledProperty, ItemUtil.disablingPropertyGetter);
        setRegistryName(Names.CUBING_TALISMAN);
    }

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

        if (NbtUtil.getIsDisabled(stack)) {
            return;
        }

        PlayerEntity player = (PlayerEntity) entityIn;

        doCubingEffect(player);
    }

    private void doCubingEffect(PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;

        for(ItemPair cubingPair : ModSetup.transmutationRecipes.CUBING_INPUT_MAPPINGS) {
            ItemStack singleItemStack = new ItemStack(cubingPair.item1);
            ItemStack blockItemStack = new ItemStack(cubingPair.item2);
            int singletonItemTotal = ItemUtil.getItemCount(singleItemStack, playerInventory.mainInventory);
            int blockItemTotal = ItemUtil.getItemCount(blockItemStack, playerInventory.mainInventory) * 9;

            int itemTotal = blockItemTotal + singletonItemTotal;

            // player has enough of the item in their inventory to justify cubing
            // scan through the list of objects which can be "cubed", and target them if their totals are over 64.
            if (itemTotal > 64 && singletonItemTotal >= 63) {
                while (singletonItemTotal >= 63) {
                    replaceNineSingletonsWithOneBlock(cubingPair, playerInventory);
                    singletonItemTotal -= 9;
                }
            }
        }
    }

    private void replaceNineSingletonsWithOneBlock(ItemPair cubingPair, PlayerInventory playerInventory) {
        int itemsToRemove = 9;
        for(int i = 0; i < playerInventory.mainInventory.size(); i++) {
            ItemStack playerItem = playerInventory.mainInventory.get(i);
            if (!isCubingItem(playerItem, cubingPair, true)) {
                continue;
            }
            int canRemove = Math.min(itemsToRemove, playerItem.getCount());
            itemsToRemove -= canRemove;
            playerInventory.decrStackSize(i, canRemove);
            if (itemsToRemove == 0) {
                break;
            }
        }
        Item itemToAdd = cubingPair.item2;
        playerInventory.addItemStackToInventory(new ItemStack(itemToAdd, 1));
    }

    private boolean isCubingItem(ItemStack playerItem, ItemPair cubingPair, boolean isCountingSingletonItemsOnly) {
        return playerItem.getItem().equals(cubingPair.item1)
                || (!isCountingSingletonItemsOnly && playerItem.getItem().equals(cubingPair.item2));
    }
}
