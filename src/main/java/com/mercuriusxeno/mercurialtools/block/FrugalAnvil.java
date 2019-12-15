package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.container.FrugalAnvilContainer;
import com.mercuriusxeno.mercurialtools.container.MercurialGrindstoneContainer;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.GrindstoneBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.Hand;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class FrugalAnvil extends AnvilBlock {

    public FrugalAnvil() {
        super(Properties
                .create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(3.0f)
                .lightValue(0));
        setRegistryName(Names.FRUGAL_ANVIL);;
    }

    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        return new SimpleNamedContainerProvider(
                (someInteger, playerInventory, playerEntity) ->
                        new FrugalAnvilContainer(someInteger, playerInventory, IWorldPosCallable.of(worldIn, pos))
                , new TranslationTextComponent(Names.FRUGAL_ANVIL));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        player.openContainer(state.getContainer(worldIn, pos));
        return true;
    }
}
