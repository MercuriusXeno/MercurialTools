package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.block.ModBlocks;
import com.mercuriusxeno.mercurialtools.reference.Constants;
import com.mercuriusxeno.mercurialtools.reference.Names;
import com.mercuriusxeno.mercurialtools.util.EntityUtil;
import com.mercuriusxeno.mercurialtools.util.ItemUtil;
import com.mercuriusxeno.mercurialtools.util.NbtUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.UseAction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.tileentity.MobSpawnerTileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

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
     * returns the action that specifies what animation to play when the items is being used
     */
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {@link #onItemUse}.
     */
    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }

    private boolean isLatched(ItemStack stack) {
        CompoundNBT tag;
        if (!stack.hasTag()) {
            return false;
        } else {
            tag = stack.getTag();
        }
        return tag.contains(Names.ENTITY_TRANSLATION_KEY) && tag.contains(Names.SOUL_TOME_PROGRESS);
    }

    private boolean isSoulCaptured(ItemStack stack) {
        CompoundNBT tag;
        if (!stack.hasTag()) {
            return false;
        } else {
            tag = stack.getTag();
        }

        return tag.contains(Names.ENTITY_TRANSLATION_KEY)
                && tag.getInt(Names.SOUL_TOME_PROGRESS) == Constants.SOUL_TOME_PROGRESS_LIMIT;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 300;
    }

    private void latchOntoSoul(World worldIn, PlayerEntity playerIn, ItemStack stack) {
        if (worldIn.isRemote()) {
            return;
        }
        CompoundNBT tag;
        if (!stack.hasTag()) {
            tag = new CompoundNBT();
        } else {
            tag = stack.getTag();
        }

        // look around for a nearby entity
        LivingEntity entity = EntityUtil.getClosestEntityToPlayer(worldIn, playerIn, Constants.SOUL_TOME_RANGE);
        // there's nobody around, you tool
        if (entity == null) {
            return;
        }

        // nice try fucko.
        if (!entity.isNonBoss()) {
            return;
        }

        CompoundNBT entityTag = entity.serializeNBT();
        tag.putString(Names.ENTITY_TRANSLATION_KEY, entity.getType().getTranslationKey());
        tag.putInt(Names.ENTITY_ID, entity.getEntityId());
        tag.putString(Names.ENTITY_RESOURCE_LOCATION, entityTag.getString("id"));
        tag.putInt(Names.SOUL_TOME_PROGRESS, 0);
        stack.setTag(tag);
        return;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !oldStack.isItemEqual(newStack) || slotChanged;
    }

    /**
     * Called when the player stops using an Item (stops holding the right mouse button).
     */
    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (worldIn.isRemote()) {
            return;
        }
        if (isSoulCaptured(stack)) {
            return;
        }

        // wipe the tag if the player failed to obtain a soul.
        stack.setDamage(0);
        stack.setTag(new CompoundNBT());
    }

    @Override
    public void onUsingTick(ItemStack stack, LivingEntity livingEntity, int count) {
        if (livingEntity.world.isRemote()) {
            return;
        }

        if (!(livingEntity instanceof PlayerEntity)) {
            return;
        }

        PlayerEntity player = (PlayerEntity)livingEntity;

        // you already have a soul in the book - don't recapture ever again.
        // this right click function is permanently disabled now, the book is bound.
        if (isSoulCaptured(stack)) {
            return;
        }

        // find the closest soul-stealable entity and latch onto it using NBT
        if (!isLatched(stack)) {
            // it doesn't matter if this succeeds or fails.
            latchOntoSoul(livingEntity.world, player, stack);
        }

        // get the entity, we're doing science/effects on it
        LivingEntity entity = getContainedEntity(livingEntity.world, stack);
        if (entity == null) {
            return;
        }

        entity.addPotionEffect(new EffectInstance(Effects.SLOWNESS, 3));

        drainEntityEffect(stack, livingEntity.world, player, entity);

        // increment the progress by 1 tick.
        // At Constants.SOUL_TOME_PROGRESS_LIMIT, the soul is "done"
        increaseSoulProgress(stack);
    }

    private void drainEntityEffect(ItemStack stack, World world, PlayerEntity player, LivingEntity entity) {
        // you shouldn't be here if you don't have a tag.
        if (!stack.hasTag()) {
            return;
        }

        CompoundNBT tag = stack.getTag();
        if (!tag.contains(Names.SOUL_TOME_PROGRESS)) {
            return;
        }

        int soulProgress = Math.min(Constants.SOUL_TOME_PROGRESS_LIMIT, tag.getInt(Names.SOUL_TOME_PROGRESS) + 1);

        float healthRatio = (((float)Constants.SOUL_TOME_PROGRESS_LIMIT - (float)soulProgress) / (float)Constants.SOUL_TOME_PROGRESS_LIMIT);
        float newHealth = (float)Math.floor(entity.getMaxHealth() * healthRatio);
        if (healthRatio == 0) {
            entity.attackEntityFrom(DamageSource.causePlayerDamage(player), entity.getMaxHealth());
        } else {
            float breakpoint = entity.getMaxHealth() / 5;
            if (newHealth <= entity.getHealth() - breakpoint) {
                entity.attackEntityFrom(DamageSource.causePlayerDamage(player), entity.getHealth() - newHealth);
                world.addEntity(new ExperienceOrbEntity(world, entity.posX, entity.posY, entity.posZ, 1));
                world.addOptionalParticle(ParticleTypes.WITCH, entity.posX, entity.posY, entity.posZ,
                        (player.posX - entity.posX) / 10F, (player.posY - entity.posY) / 10F, (player.posZ - entity.posZ) / 10F);
            }
        }

    }

    private void increaseSoulProgress(ItemStack stack) {
        // you shouldn't be here if you don't have a tag.
        if (!stack.hasTag()) {
            return;
        }

        CompoundNBT tag = stack.getTag();
        if (!tag.contains(Names.SOUL_TOME_PROGRESS)) {
            return;
        }

        int soulProgress = Math.min(Constants.SOUL_TOME_PROGRESS_LIMIT, tag.getInt(Names.SOUL_TOME_PROGRESS) + 1);
        if (soulProgress > 0) {
            stack.setDamage(stack.getMaxDamage() - soulProgress);
        }
        tag.putInt(Names.SOUL_TOME_PROGRESS, soulProgress);
        stack.setTag(tag);
        // this is permanent!
        if (soulProgress == 100) {
            NbtUtil.setIsDisabled(stack, true);
        }
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
        EntityType<?> entityType = getEntityTypeFromResourceLocation(stack).orElse(null);
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

    private Optional<EntityType<?>> getEntityTypeFromResourceLocation(ItemStack stack) {
        ResourceLocation resourceLocation = getContainedEntityResourceLocation(stack);
        return EntityType.byKey(resourceLocation.toString()); // attempting the fully qualified string to avoid mod mismatches.
    }

    private LivingEntity getContainedEntity(World worldIn, ItemStack stack) {
        CompoundNBT tag;
        if (!stack.hasTag()) {
            return null;
        } else {
            tag = stack.getTag();
        }

        if (!tag.contains(Names.ENTITY_ID)) {
            return null;
        }

        int entityId = tag.getInt(Names.ENTITY_ID);
        Entity capturedEntity = worldIn.getEntityByID(entityId);
        if (capturedEntity instanceof LivingEntity) {
            return (LivingEntity) capturedEntity;
        }
        return null;
    }
}
