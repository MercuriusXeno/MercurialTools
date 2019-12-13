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

public class Quiver extends Item {
    public Quiver() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        this.addPropertyOverride(ItemUtil.disabledProperty, ItemUtil.disablingPropertyGetter);
        setRegistryName(Names.QUIVER);
    }

    public static final ArrayList<Item> ITEM_EXCEPTIONS = new ArrayList<>();

    public static final ArrayList<Item> QUIVER_CONSUMES_THESE = new ArrayList<>(
            Arrays.asList(
                    Items.ARROW,
                    Items.SPECTRAL_ARROW,
                    Items.TIPPED_ARROW
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
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (NbtUtil.getIsDisabled(oldStack) != NbtUtil.getIsDisabled(newStack)) {
            return true;
        }
        return slotChanged;
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
            ItemUtil.undoContainment(stack, player);
        } else {
            ItemUtil.doContainment(stack, player, QUIVER_CONSUMES_THESE, ITEM_EXCEPTIONS);
        }
    }
}
