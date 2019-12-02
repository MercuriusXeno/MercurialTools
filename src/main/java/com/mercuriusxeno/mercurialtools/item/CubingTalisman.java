package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;

public class CubingTalisman extends Item {
    public CubingTalisman() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
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

        // scan through the list of objects which can be "cubed", and target them if their totals are over 64.
    }
}
