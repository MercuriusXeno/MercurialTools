package com.mercuriusxeno.mercurialtools.item;

import com.google.common.collect.Lists;
import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.ItemUtil;
import com.mercuriusxeno.mercurialtools.util.NbtUtil;
import net.minecraft.client.util.ITooltipFlag;
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
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
                    Items.SPLASH_POTION,
                    Items.GLASS_BOTTLE
            )
    );

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        List<ITextComponent> tooltipList = Lists.newArrayList();
        ArrayList<Tuple<String, Integer>> distinctItemCounts = NbtUtil.getDistinctItemCountsForDisplay(stack);
        for(int i = 0; i < distinctItemCounts.size(); i++) {
            Tuple<String, Integer> itemTuple = distinctItemCounts.get(i);
            String displayTextLine = String.format("%s: %d", itemTuple.getA(), itemTuple.getB());
            tooltipList.add(new StringTextComponent(displayTextLine));
        }
        tooltip.addAll(tooltipList);
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

        PlayerEntity player = (PlayerEntity) entityIn;

        if (NbtUtil.getIsDisabled(stack)) {
            undoContainment(stack, player);
        } else {
            doContainment(stack, player);
        }
    }

    private static void undoContainment(ItemStack beltStack, PlayerEntity player) {
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

    private static void doContainment(ItemStack container, PlayerEntity player) {
        PlayerInventory playerInventory = player.inventory;

        for(Item itemToConsume : BELT_CONSUMES_THESE) {
            // make sure we respect distinct nbt tags, we do this by
            // getting all items in the player inventory with distint NBTs
            NonNullList<ItemStack> distinctStacks = ItemUtil.getAllDistinctlyTaggedStacks(playerInventory, container, itemToConsume);

            for(ItemStack distinctStack : distinctStacks) {
                // this item has a special consideration for glass bottles
                // we don't want a glass bottle left in every potion consumed slot.
                // this causes the belt to consume all bottles, and leave none.
                int howManyDoWeWant = getHowManyWeWant(distinctStack);
                while (ItemUtil.getItemCountInInventory(playerInventory, distinctStack) > howManyDoWeWant) {
                    ItemStack stackFound = ItemUtil.getAndRemoveOneStack(playerInventory.mainInventory, distinctStack);
                    NbtUtil.addItemStackToContainerItem(container, stackFound);
                }

                NonNullList<ItemStack> containedItems = NbtUtil.getItems(container);
                int amountWeWouldLike = howManyDoWeWant - ItemUtil.getItemCountInInventory(playerInventory, distinctStack);
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

    private static int getHowManyWeWant(ItemStack distinctStack) {
        return distinctStack.getItem().equals(Items.GLASS_BOTTLE) ? 0 : distinctStack.getMaxStackSize();
    }
}
