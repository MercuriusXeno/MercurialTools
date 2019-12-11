package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.block.ModBlocks;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.ItemUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class SoulTome extends Item {
    public SoulTome() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup)
                .maxDamage(100));
        this.addPropertyOverride(ItemUtil.disabledProperty, ItemUtil.disablingPropertyGetter);
        setRegistryName(Names.SOUL_TOME);
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (!isSoulCaptured(stack)) {
            return;
        }
        String entityName = getEntityLocalizedName(stack);
        tooltip.add(new StringTextComponent(entityName));
    }

    @Override
    public boolean hasEffect(ItemStack stack) {
        return isSoulCaptured(stack);
    }

    /**
     * Current implementations of this method in child classes do not use the entry argument beside ev. They just raise
     * the damage on the stack.
     */
    @Override
    public boolean hitEntity(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (target.getHealth() >= 1.0F) {
            return true;
        }
        if (isSoulCaptured(stack)) {
            return true;
        }
        latchOntoSoul(target, stack);
        return true;
    }

    private boolean isSoulCaptured(ItemStack stack) {
        CompoundNBT tag;
        if (!stack.hasTag()) {
            return false;
        } else {
            tag = stack.getTag();
        }

        return tag.contains(Names.ENTITY_TRANSLATION_KEY);
    }

    private void latchOntoSoul(LivingEntity entity, ItemStack stack) {
        if(entity.world.isRemote()) {
            return;
        }
        CompoundNBT tag;
        if (!stack.hasTag()) {
            tag = new CompoundNBT();
        } else {
            tag = stack.getTag();
        }

        // there's nobody around, you tool
        if (entity == null) {
            return;
        }

        // nice try fucko.
        if (!entity.isNonBoss()) {
            return;
        }

        CompoundNBT entityTag = entity.serializeNBT();
        tag.putString(Names.ENTITY_RESOURCE_LOCATION, entityTag.getString("id"));
        tag.putString(Names.ENTITY_TRANSLATION_KEY, entity.getType().getTranslationKey());
        tag.putInt(Names.ENTITY_ID, entity.getEntityId());
        stack.setTag(tag);
        entity.world.addParticle(ParticleTypes.SMOKE, entity.posX + entity.getWidth() / 2, entity.posY + entity.getHeight(), entity.posZ + entity.getWidth() / 2, 0D, 1.0D, 0D);
        entity.remove();
        return;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.isItemEqual(newStack) || slotChanged;
    }

    private ResourceLocation getContainedEntityResourceLocation(ItemStack stack) {
        // you shouldn't be here if you don't have a tag.
        if (!stack.hasTag()) {
            return null;
        }

        CompoundNBT tag = stack.getTag();
        if (!tag.contains(Names.ENTITY_TRANSLATION_KEY)) {
            return null;
        }

        return new ResourceLocation(tag.getString(Names.ENTITY_RESOURCE_LOCATION));
    }

    private String getContainedEntityTranslationKey(ItemStack stack) {
        // you shouldn't be here if you don't have a tag.
        if (!stack.hasTag()) {
            return "";
        }

        CompoundNBT tag = stack.getTag();
        if (!tag.contains(Names.ENTITY_TRANSLATION_KEY)) {
            return "";
        }

        String resourceName = tag.getString(Names.ENTITY_TRANSLATION_KEY);

        return resourceName;
    }

    private String getEntityLocalizedName(ItemStack stack) {
        String resourceName = getContainedEntityTranslationKey(stack);

        TranslationTextComponent textComponent = new TranslationTextComponent(resourceName);

        return textComponent.getString();
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        if (world.isRemote()) {
            return ActionResultType.PASS;
        }

        ItemStack stack = context.getItem();
        if (!isSoulCaptured(stack)) {
            return ActionResultType.PASS;
        }
        EntityType<?> entityType = getEntityTypeFromResourceLocation(stack);
        if (entityType == null) {
            return ActionResultType.PASS;
        }
        BlockPos blockPos = context.getPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (blockState.getBlock().equals(ModBlocks.SPAWNER_TEMPLATE)) {
            world.setBlockState(blockPos, Blocks.SPAWNER.getDefaultState(), 0);
            if (Blocks.SPAWNER.getDefaultState().hasTileEntity()) {
                Blocks.SPAWNER.getDefaultState().createTileEntity(world).setPos(blockPos);
                MobSpawnerTileEntity tileEntity = (MobSpawnerTileEntity)world.getTileEntity(blockPos);
                tileEntity.getSpawnerBaseLogic().setEntityType(entityType);
                world.notifyBlockUpdate(blockPos, blockState, world.getBlockState(blockPos), 0);
            }
        }
        // destroy the item, it's used up.
        context.getItem().setDamage(context.getItem().getMaxDamage() - 1);
        context.getItem().damageItem(1, context.getPlayer(), (entityPlayer) -> {
            entityPlayer.sendBreakAnimation(context.getHand());
        });
        return ActionResultType.SUCCESS;
    }

    private EntityType<?> getEntityTypeFromResourceLocation(ItemStack stack) {
        ResourceLocation resourceLocation = getContainedEntityResourceLocation(stack);
        // return EntityType.byKey(resourceLocation.toString()); // attempting the fully qualified string to avoid mod mismatches.
        // apparently this one is better
        EntityType<?> entityType = ForgeRegistries.ENTITIES.getValue(resourceLocation);
        return entityType;
    }
}
