package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.NbtUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

public class CubingTalisman extends Item {
    public IItemPropertyGetter disablingPropertyGetter = (itemStack, worldIn, usingEntity) -> NbtUtil.getIsDisabled(itemStack) ? 1.0F : 0.0F;

    public CubingTalisman() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        this.addPropertyOverride(new ResourceLocation("is_disabled"), disablingPropertyGetter);
        setRegistryName(Names.CUBING_TALISMAN);
    }

    public static final ArrayList<Tuple<String, String>> CUBING_OBJECT_NAME_PAIRS = new ArrayList<>(
      Arrays.asList(
              new Tuple<>("lapis_lazuli", "lapis_block"),
              new Tuple<>("redstone", "redstone_block"),
              new Tuple<>("coal", "coal_block"),
              new Tuple<>("diamond", "diamond_block"),
              new Tuple<>("emerald", "emerald_block"),
              new Tuple<>("iron", "iron_block"),
              new Tuple<>("gold", "gold_block"),
              new Tuple<>("dried_kelp", "dried_kelp_block"),
              new Tuple<>("wheat", "hay_block"),
              new Tuple<>("slime_ball", "slime_block"),
              new Tuple<>("nether_wart", "nether_wart_block"),
              new Tuple<>("bone_meal", "bone_block")
      )
    );

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemStack = playerIn.getHeldItem(handIn);
        NbtUtil.setIsDisabled(itemStack, !NbtUtil.getIsDisabled(itemStack));
        return new ActionResult<>(ActionResultType.PASS, itemStack);
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

        PlayerInventory playerInventory = player.inventory;

        for(Tuple<String, String> cubingPair : CUBING_OBJECT_NAME_PAIRS) {
            int itemTotal = getTotalOfItemInInventory(cubingPair, playerInventory, false);
            int singletonItemTotal = getTotalOfItemInInventory(cubingPair, playerInventory, true);
            // player has enough of the item in their inventory to justify

            // scan through the list of objects which can be "cubed", and target them if their totals are over 64.
            if (itemTotal > 64 && singletonItemTotal >= 9) {
              while (singletonItemTotal >= 9) {
                  replaceNineSingletonsWithOneBlock(cubingPair, playerInventory);
                  singletonItemTotal -= 9;
              }
            }
        }

    }

    private void replaceNineSingletonsWithOneBlock(Tuple<String, String> cubingPair, PlayerInventory playerInventory) {
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
        Item itemToAdd = ForgeRegistries.ITEMS.getValue(ResourceLocation.create(String.format("minecraft:%s", cubingPair.getB()), ':')).getItem();
        playerInventory.addItemStackToInventory(new ItemStack(itemToAdd, 1));
    }

    private int getTotalOfItemInInventory(Tuple<String, String> cubingPair, PlayerInventory playerInventory, boolean isCountingSingleItemsOnly) {
        int value = 0;
        for(ItemStack playerItem : playerInventory.mainInventory) {
            if (playerItem.isEmpty()) {
                continue;
            }
            if (!isCubingItem(playerItem, cubingPair, false)) {
                continue;
            }
            value += cubingItemValue(playerItem, cubingPair, isCountingSingleItemsOnly);
        }
        return value;
    }

    private int cubingItemValue(ItemStack playerItem, Tuple<String, String> cubingPair, boolean isCountingSingleItemsOnly) {
        if (playerItem.getItem().getRegistryName().getPath().equals(cubingPair.getB())) {
            return isCountingSingleItemsOnly ? 0 : 9 * playerItem.getCount();
        }

        return 1 * playerItem.getCount();
    }

    private boolean isCubingItem(ItemStack playerItem, Tuple<String, String> cubingPair, boolean isCountingSingletonItemsOnly) {
        return playerItem.getItem().getRegistryName().getPath().equals(cubingPair.getA())
                || (!isCountingSingletonItemsOnly && playerItem.getItem().getRegistryName().getPath().equals(cubingPair.getB()));
    }
}
