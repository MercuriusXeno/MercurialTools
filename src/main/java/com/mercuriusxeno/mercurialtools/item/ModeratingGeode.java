package com.mercuriusxeno.mercurialtools.item;

import com.google.common.collect.Lists;
import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.ItemUtil;
import com.mercuriusxeno.mercurialtools.util.NbtUtil;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModeratingGeode extends Item {
    public ModeratingGeode() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        this.addPropertyOverride(ItemUtil.disabledProperty, ItemUtil.disablingPropertyGetter);
        setRegistryName(Names.MODERATING_GEODE);
    }

    public static final ArrayList<Item> ITEM_EXCEPTIONS = new ArrayList<>();

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
            ItemUtil.doContainment(stack, player, GEODE_CONSUMES_THESE, ITEM_EXCEPTIONS);
        }
    }
}
