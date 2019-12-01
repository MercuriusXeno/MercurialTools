package com.mercuriusxeno.mercurialtools.block;

import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class SpawnerTemplate extends Block {
    public SpawnerTemplate() {
        super(Block.Properties
                .create(Material.IRON)
                .sound(SoundType.METAL)
                .hardnessAndResistance(4.0f)
                .lightValue(0));
        setRegistryName(Names.SPAWNER_TEMPLATE);
    }

    /**
     * Gets the render layer this block will render on. SOLID for solid blocks, CUTOUT or CUTOUT_MIPPED for on-off
     * transparency (glass, reeds), TRANSLUCENT for fully blended transparency (stained glass)
     */
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    /*
    * There's a goofy mojang deprecation here, but I've read this override is fine and the deprecation isn't being used correctly.
    * Blockstate onActivate (whatever it's called) calls this when it fires.
     */
    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        // only fire this server side.
        if (worldIn.isRemote) {
            return false;
        }

        if (player == null) {
            return false;
        }

        // TODO
        // figure out if the player is holding a soul tome. If they are (in either hand)
        // replace this block in the world silently with a spawner of that type.
        // Then set its compound to be the right entity type, based on the book used.

        return false;
    }


}
