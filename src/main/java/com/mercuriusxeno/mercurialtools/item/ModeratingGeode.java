package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.ItemUtil;
import com.mercuriusxeno.mercurialtools.util.NbtUtil;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

public class ModeratingGeode extends Item {
    public ModeratingGeode() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        this.addPropertyOverride(ItemUtil.disabledProperty, ItemUtil.disablingPropertyGetter);
        setRegistryName(Names.MODERATING_GEODE);
    }

    public static final ArrayList<Item> GEODE_CONSUMES_THESE = new ArrayList<>(
            Arrays.asList(
                    Blocks.COBBLESTONE.asItem(),
                    Blocks.STONE.asItem(),
                    Blocks.DIRT.asItem(),
                    Blocks.GRAVEL.asItem(),
                    Items.FLINT.asItem(),
                    Blocks.ANDESITE.asItem(),
                    Blocks.GRANITE.asItem(),
                    Blocks.DIORITE.asItem(),
                    Blocks.IRON_ORE.asItem(),
                    Blocks.GOLD_ORE.asItem(),
                    Blocks.NETHERRACK.asItem(),
                    Blocks.END_STONE.asItem()
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
            undoContainment(stack, player);
        } else {
            doContainment(stack, player);
        }
    }

    private static void undoContainment(ItemStack geodeStack, PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;

        NonNullList<ItemStack> geodeItems = NbtUtil.getItems(geodeStack);
        for(int i = 0; i < geodeItems.size(); i++) {
            // after removing the stack from the util list, persist the changes.
            ItemStack takenStack = ItemStackHelper.getAndRemove(geodeItems, i);
            CompoundNBT tag = geodeStack.getTag();
            ItemStackHelper.saveAllItems(tag, geodeItems);
            geodeStack.setTag(tag);

            if (takenStack == ItemStack.EMPTY) {
                continue;
            }
            if (!playerInventory.addItemStackToInventory(takenStack)) {
                NbtUtil.addItemStackToContainerItem(geodeStack, takenStack);
                break;
            }
        }
    }

    private static void doContainment(ItemStack container, PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;

        for(Item itemToConsume : GEODE_CONSUMES_THESE) {
            // make sure we respect distinct nbt tags, we do this by
            // getting all items in the player inventory with distint NBTs
            NonNullList<ItemStack> distinctStacks = ItemUtil.getAllDistinctlyTaggedStacks(playerInventory, container, itemToConsume);

            for(ItemStack distinctStack : distinctStacks) {
                while (ItemUtil.getItemCountInInventory(playerInventory, distinctStack) > distinctStack.getMaxStackSize()) {
                    ItemStack stackFound = ItemUtil.getAndRemoveOneStack(playerInventory.mainInventory, distinctStack);
                    NbtUtil.addItemStackToContainerItem(container, stackFound);
                }

                NonNullList<ItemStack> containedItems = NbtUtil.getItems(container);
                int amountWeWouldLike = distinctStack.getMaxStackSize() - ItemUtil.getItemCountInInventory(playerInventory, distinctStack);
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
