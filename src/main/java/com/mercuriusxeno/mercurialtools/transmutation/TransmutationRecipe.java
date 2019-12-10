package com.mercuriusxeno.mercurialtools.transmutation;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TransmutationRecipe {
    public final ItemStack input;
    public final ItemStack output;
    public final TransmutationDirection direction;

    public TransmutationRecipe(ItemStack input, ItemStack output, TransmutationDirection direction) {
        this.input = input;
        this.output = output;
        this.direction = direction;
    }

    public TransmutationRecipe(Item inputItem, int inputAmount, Item outputItem, int outputAmount, TransmutationDirection direction) {
        this(new ItemStack(inputItem, inputAmount), new ItemStack(outputItem, outputAmount), direction);
    }
}
