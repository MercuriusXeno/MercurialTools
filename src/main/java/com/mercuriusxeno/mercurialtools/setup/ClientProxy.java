package com.mercuriusxeno.mercurialtools.setup;

import com.mercuriusxeno.mercurialtools.block.EnderVacuumTile;
import com.mercuriusxeno.mercurialtools.block.ModBlocks;
import com.mercuriusxeno.mercurialtools.client.render.entity.EnderVacuumModel;
import com.mercuriusxeno.mercurialtools.client.render.tileentity.EnderVacuumTileEntityRenderer;
import com.mercuriusxeno.mercurialtools.container.FrugalAnvilScreen;
import com.mercuriusxeno.mercurialtools.container.MercurialGrindstoneScreen;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy implements IProxy {
    @Override
    public void init() {
        ClientRegistry.bindTileEntitySpecialRenderer(EnderVacuumTile.class, new EnderVacuumTileEntityRenderer(new EnderVacuumModel()));
        ScreenManager.registerFactory(ModBlocks.MERCURIAL_GRINDSTONE_CONTAINER,
                MercurialGrindstoneScreen::new);
        ScreenManager.registerFactory(ModBlocks.FRUGAL_ANVIL_CONTAINER,
                FrugalAnvilScreen::new);
    }

    @Override
    public World getClientWorld() {
        return Minecraft.getInstance().world;
    }
}
