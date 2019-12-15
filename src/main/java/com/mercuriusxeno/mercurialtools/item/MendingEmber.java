package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.setup.ModSetup;
import com.mercuriusxeno.mercurialtools.util.ItemPair;
import com.mercuriusxeno.mercurialtools.util.ItemUtil;
import com.mercuriusxeno.mercurialtools.util.NbtUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MendingEmber extends Item {
    public MendingEmber() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        this.addPropertyOverride(ItemUtil.disabledProperty, ItemUtil.disablingPropertyGetter);
        setRegistryName(Names.MENDING_EMBER);
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

        doMending(player);
    }

    private void doMending(PlayerEntity player) {
        doMending(player, player.inventory.mainInventory);
        doMending(player, player.inventory.armorInventory);
    }

    /**
     *
     * @param player the player we're deducting experience from
     * @param inventorySector which sector of the inventory we're scanning with this op
     */
    private void doMending(PlayerEntity player, NonNullList<ItemStack> inventorySector) {
        int experienceRemaining = player.experienceTotal;
        for(ItemStack stack : inventorySector) {
            // these are all reasons we won't be repairing this item.
            if (!stack.isEnchanted() || !stack.isDamaged()) {
                continue;
            }
            if (!isMending(stack)) {
                continue;
            }

            int itemDamage = stack.getDamage();

            // figure out how much experience is needed to repair a point of damage for this item
            float experienceNeededForOneDurability = 1f / stack.getXpRepairRatio();

            float experienceNeededForCompleteMend = experienceNeededForOneDurability * (float)itemDamage;
            int flatExperienceNeededForCompleteMend = MathHelper.ceil(experienceNeededForCompleteMend);

            // try to be efficient - if the amount of experience we're draining is less than 99% efficient to the ratio, don't do it.
            if ((float)flatExperienceNeededForCompleteMend * 0.99F > experienceNeededForCompleteMend) {
                continue;
            }

           if (experienceRemaining >= flatExperienceNeededForCompleteMend) {
               stack.setDamage(0);
               player.giveExperiencePoints(-flatExperienceNeededForCompleteMend);
           }
        }
    }

    private boolean isMending(ItemStack stack) {
        ListNBT enchantmentTags = stack.getEnchantmentTagList();
        for(int i = 0; i < enchantmentTags.size(); i++) {
            CompoundNBT enchantTag = enchantmentTags.getCompound(i);
            if (enchantTag.getString("id").equals("minecraft:mending")) {
                return true;
            }
        }
        return false;
    }
}
