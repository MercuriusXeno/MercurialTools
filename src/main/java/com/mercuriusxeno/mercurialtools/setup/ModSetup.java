package com.mercuriusxeno.mercurialtools.setup;

import com.mercuriusxeno.mercurialtools.block.ModBlocks;
import com.mercuriusxeno.mercurialtools.transmutation.TransmutationRecipes;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class ModSetup {

    public static TransmutationRecipes transmutationRecipes;

    public ItemGroup itemGroup = new ItemGroup("mercurialtools") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlocks.INTERLOPER);
        }
    };

    public void init() {
        transmutationRecipes = new TransmutationRecipes();
    }
}
