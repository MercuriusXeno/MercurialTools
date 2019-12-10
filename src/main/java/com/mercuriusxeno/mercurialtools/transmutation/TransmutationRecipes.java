package com.mercuriusxeno.mercurialtools.transmutation;

import com.mercuriusxeno.mercurialtools.reference.Constants;
import net.minecraft.block.Block;
import net.minecraft.block.CropsBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/*
 * Transmutation references, for condensing and expanding hoppers to reference.
 */
public class TransmutationRecipes {
    public ArrayList<TransmutationRecipe> transmutationRecipes;

    public TransmutationRecipes () {
        // initialize the transmutation recipe list
        transmutationRecipes = new ArrayList<>();

        // bidirectional transmutes, "traditional"
        addBidirectionalTransmutation(Items.SAND, Items.DIRT);
        addBidirectionalTransmutation(Items.DIRT, Items.GRAVEL);
        addBidirectionalTransmutation(Items.GRAVEL, Items.COBBLESTONE);
        addBidirectionalTransmutation(Items.COBBLESTONE, 4, Items.COAL);
        addBidirectionalTransmutation(Items.COAL_BLOCK, 1, Items.IRON_INGOT, 2);
        addBidirectionalTransmutation(Items.IRON_BLOCK, 1, Items.GOLD_INGOT, 2);
        addBidirectionalTransmutation(Items.GLOWSTONE_DUST, 4, Items.GOLD_NUGGET); // this is the median between both trees
        addBidirectionalTransmutation(Items.NETHERRACK, 4, Items.REDSTONE);
        addBidirectionalTransmutation(Items.REDSTONE_BLOCK, 1, Items.GLOWSTONE_DUST, 2);
        addBidirectionalTransmutation(Items.GLOWSTONE, 4, Items.QUARTZ);
        addBidirectionalTransmutation(Items.QUARTZ, 4, Items.QUARTZ_BLOCK);
        addBidirectionalTransmutation(Items.QUARTZ_BLOCK, Items.LAPIS_LAZULI);
        addBidirectionalTransmutation(Items.LAPIS_BLOCK, 1, Items.DIAMOND, 2);

        // unidirectional transmutes, macerating
        addUnidirectionalTransmutation(Items.SANDSTONE, Items.SAND, TransmutationDirection.EXPANDING);
        addUnidirectionalTransmutation(Items.FLINT, Items.GRAVEL, TransmutationDirection.EXPANDING);

        // stone devolution
        addUnidirectionalTransmutation(Items.STONE, Items.COBBLESTONE, TransmutationDirection.EXPANDING);
        addUnidirectionalTransmutation(Items.SMOOTH_STONE, Items.STONE, TransmutationDirection.EXPANDING);

        // quartz restoration
        addUnidirectionalTransmutation(Items.QUARTZ_PILLAR, Items.QUARTZ_BLOCK, TransmutationDirection.EXPANDING);
        addUnidirectionalTransmutation(Items.SMOOTH_QUARTZ, Items.QUARTZ_BLOCK, TransmutationDirection.EXPANDING);
        addUnidirectionalTransmutation(Items.PURPUR_PILLAR, Items.PURPUR_BLOCK, TransmutationDirection.EXPANDING);

        // stair condensing
        initStairMappings();
        addStairCondensingTransmutations();

        // slab condensing
        initSlabMappings();
        addSlabCondensingTransmutations();

        // mulching transmutes (aka renewable dirt)
        initMulchMappings();
        addMulchingTransmutations();

        if (!validateTransmutationInputsAreUniqueBothWays()) {
            System.out.println("Transmutation recipes with non-unique inputs were found in Mercurial Tools. Blame Xeno!");
        }
    }

    private class ItemPair {
        public Item item1;

        public Item item2;

        public ItemPair(Item item1, Item item2) {
            this.item1 = item1;
            this.item2 = item2;
        }
    }

    public ArrayList<ItemPair> STAIR_TO_NON_STAIR_MAPPINGS = new ArrayList<>();
    public void initStairMappings() {
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.PURPUR_STAIRS, Items.PURPUR_BLOCK));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.OAK_STAIRS, Items.OAK_PLANKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.COBBLESTONE_STAIRS, Items.COBBLESTONE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.BRICK_STAIRS, Items.BRICK));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.STONE_BRICK_STAIRS, Items.STONE_BRICKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.NETHER_BRICK_STAIRS, Items.NETHER_BRICK));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.SANDSTONE_STAIRS, Items.SANDSTONE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.SPRUCE_STAIRS, Items.SPRUCE_PLANKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.BIRCH_STAIRS, Items.BIRCH_PLANKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.JUNGLE_STAIRS, Items.JUNGLE_PLANKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.QUARTZ_STAIRS, Items.QUARTZ));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.ACACIA_STAIRS, Items.ACACIA_PLANKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.DARK_OAK_STAIRS, Items.DARK_OAK_PLANKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.PRISMARINE_STAIRS, Items.PRISMARINE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.PRISMARINE_BRICK_STAIRS, Items.PRISMARINE_BRICKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.DARK_PRISMARINE_STAIRS, Items.DARK_PRISMARINE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.RED_SANDSTONE_STAIRS, Items.RED_SANDSTONE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.POLISHED_GRANITE_STAIRS, Items.POLISHED_GRANITE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.SMOOTH_RED_SANDSTONE_STAIRS, Items.SMOOTH_RED_SANDSTONE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.MOSSY_STONE_BRICK_STAIRS, Items.MOSSY_STONE_BRICKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.POLISHED_DIORITE_STAIRS, Items.POLISHED_DIORITE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.MOSSY_COBBLESTONE_STAIRS, Items.MOSSY_COBBLESTONE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.END_STONE_BRICK_STAIRS, Items.END_STONE_BRICKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.STONE_STAIRS, Items.STONE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.SMOOTH_SANDSTONE_STAIRS, Items.SMOOTH_SANDSTONE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.SMOOTH_QUARTZ_STAIRS, Items.SMOOTH_QUARTZ));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.GRANITE_STAIRS, Items.GRANITE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.ANDESITE_STAIRS, Items.ANDESITE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.RED_NETHER_BRICK_STAIRS, Items.RED_NETHER_BRICKS));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.POLISHED_ANDESITE_STAIRS, Items.POLISHED_ANDESITE));
        STAIR_TO_NON_STAIR_MAPPINGS.add(new ItemPair(Items.DIORITE_STAIRS, Items.DIORITE));
    }

    public ArrayList<ItemPair> SLAB_TO_NON_SLAB_MAPPINGS = new ArrayList<>();
    public void initSlabMappings() {
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.OAK_SLAB, Items.OAK_PLANKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.SPRUCE_SLAB, Items.SPRUCE_PLANKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.BIRCH_SLAB, Items.BIRCH_PLANKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.JUNGLE_SLAB, Items.JUNGLE_PLANKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.ACACIA_SLAB, Items.ACACIA_PLANKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.DARK_OAK_SLAB, Items.DARK_OAK_PLANKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.STONE_SLAB, Items.STONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.SMOOTH_STONE_SLAB, Items.SMOOTH_STONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.SANDSTONE_SLAB, Items.SANDSTONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.CUT_SANDSTONE_SLAB, Items.CUT_SANDSTONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.COBBLESTONE_SLAB, Items.COBBLESTONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.BRICK_SLAB, Items.BRICK));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.STONE_BRICK_SLAB, Items.STONE_BRICKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.NETHER_BRICK_SLAB, Items.NETHER_BRICK));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.QUARTZ_SLAB, Items.QUARTZ));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.RED_SANDSTONE_SLAB, Items.RED_SANDSTONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.CUT_RED_SANDSTONE_SLAB, Items.CUT_RED_SANDSTONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.PURPUR_SLAB, Items.PURPUR_BLOCK));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.PRISMARINE_SLAB, Items.PRISMARINE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.PRISMARINE_BRICK_SLAB, Items.PRISMARINE_BRICKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.DARK_PRISMARINE_SLAB, Items.DARK_PRISMARINE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.POLISHED_GRANITE_SLAB, Items.POLISHED_GRANITE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.SMOOTH_RED_SANDSTONE_SLAB, Items.SMOOTH_RED_SANDSTONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.MOSSY_STONE_BRICK_SLAB, Items.MOSSY_STONE_BRICKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.POLISHED_DIORITE_SLAB, Items.POLISHED_DIORITE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.MOSSY_COBBLESTONE_SLAB, Items.MOSSY_COBBLESTONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.END_STONE_BRICK_SLAB, Items.END_STONE_BRICKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.SMOOTH_SANDSTONE_SLAB, Items.SMOOTH_SANDSTONE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.SMOOTH_QUARTZ_SLAB, Items.SMOOTH_QUARTZ));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.GRANITE_SLAB, Items.GRANITE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.ANDESITE_SLAB, Items.ANDESITE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.RED_NETHER_BRICK_SLAB, Items.RED_NETHER_BRICKS));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.POLISHED_ANDESITE_SLAB, Items.POLISHED_ANDESITE));
        SLAB_TO_NON_SLAB_MAPPINGS.add(new ItemPair(Items.DIORITE_SLAB, Items.DIORITE));
    };

    public ArrayList<ItemPair> MULCH_INPUT_MAPPINGS = new ArrayList<>();
    public void initMulchMappings() {
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.GRASS_BLOCK, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.OAK_SAPLING, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SPRUCE_SAPLING, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BIRCH_SAPLING, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.JUNGLE_SAPLING, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ACACIA_SAPLING, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.DARK_OAK_SAPLING, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.OAK_LEAVES, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SPRUCE_LEAVES, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BIRCH_LEAVES, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.JUNGLE_LEAVES, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ACACIA_LEAVES, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.DARK_OAK_LEAVES, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.GRASS, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.FERN, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.DEAD_BUSH, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SEAGRASS, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SEA_PICKLE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.WHITE_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ORANGE_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.MAGENTA_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LIGHT_BLUE_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.YELLOW_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LIME_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PINK_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.GRAY_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LIGHT_GRAY_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CYAN_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PURPLE_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BLUE_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BROWN_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.GREEN_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.RED_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BLACK_WOOL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.DANDELION, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.POPPY, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BLUE_ORCHID, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ALLIUM, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.AZURE_BLUET, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.RED_TULIP, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ORANGE_TULIP, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.WHITE_TULIP, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PINK_TULIP, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.OXEYE_DAISY, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CORNFLOWER, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LILY_OF_THE_VALLEY, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.WITHER_ROSE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BROWN_MUSHROOM, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.RED_MUSHROOM, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CHORUS_PLANT, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CHORUS_FLOWER, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CACTUS, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PUMPKIN, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.MELON, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.VINE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LILY_PAD, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SUNFLOWER, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LILAC, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ROSE_BUSH, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PEONY, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.TALL_GRASS, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LARGE_FERN, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.APPLE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.STICK, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.STRING, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.FEATHER, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.GUNPOWDER, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.WHEAT_SEEDS, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.WHEAT, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BREAD, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PORKCHOP, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COOKED_PORKCHOP, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SUGAR_CANE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.KELP, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.DRIED_KELP_BLOCK, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BAMBOO, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PAPER, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SLIME_BALL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.EGG, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COD, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SALMON, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.TROPICAL_FISH, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PUFFERFISH, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COOKED_COD, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COOKED_SALMON, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.INK_SAC, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.RED_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.GREEN_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COCOA_BEANS, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PURPLE_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CYAN_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LIGHT_GRAY_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.GRAY_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PINK_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LIME_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.YELLOW_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.LIGHT_BLUE_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.MAGENTA_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ORANGE_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BONE_MEAL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BLUE_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BROWN_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BLACK_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.WHITE_DYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BONE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SUGAR, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COOKIE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.MELON_SLICE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.DRIED_KELP, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.PUMPKIN_SEEDS, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.MELON_SEEDS, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BEEF, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COOKED_BEEF, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CHICKEN, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COOKED_CHICKEN, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ROTTEN_FLESH, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.ENDER_PEARL, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BLAZE_ROD, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.GHAST_TEAR, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.NETHER_WART, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.SPIDER_EYE, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.MAGMA_CREAM, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CARROT, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.POTATO, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BAKED_POTATO, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.POISONOUS_POTATO, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.RABBIT, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COOKED_RABBIT, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.MUTTON, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.COOKED_MUTTON, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.CHORUS_FRUIT, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.POPPED_CHORUS_FRUIT, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BEETROOT, Items.DIRT));
        MULCH_INPUT_MAPPINGS.add(new ItemPair(Items.BEETROOT_SEEDS, Items.DIRT));
    }

    private boolean validateTransmutationInputsAreUniqueBothWays() {
        ArrayList<Item> distinctCondensingInputs = new ArrayList<>();
        ArrayList<Item> distinctExpandingInputs = new ArrayList<>();
        for(TransmutationRecipe recipe : transmutationRecipes) {
            if (recipe.direction == TransmutationDirection.CONDENSING) {
                if (distinctCondensingInputs.contains(recipe.input.getItem())) {
                    return false;
                }
                distinctCondensingInputs.add(recipe.input.getItem());
            } else {
                if (distinctExpandingInputs.contains(recipe.input.getItem())) {
                    return false;
                }
                distinctExpandingInputs.add(recipe.input.getItem());
            }
        }
        return true;
    }

    private void addMulchingTransmutations() {
        MULCH_INPUT_MAPPINGS.forEach(r -> addMulchingTransmutation(r.item1, r.item2));
    }

    private void addMulchingTransmutation(Item item, Item item1) {
        addUnidirectionalTransmutation(item, item1, TransmutationDirection.EXPANDING);
    }

    private void addStairCondensingTransmutations() {
        STAIR_TO_NON_STAIR_MAPPINGS.forEach(r -> addStairCondensingTransmutation(r.item1, r.item2));
    }

    private void addStairCondensingTransmutation(Item stair, Item fullBlock) {
        addUnidirectionalTransmutation(stair, fullBlock, TransmutationDirection.CONDENSING);
    }

    private void addSlabCondensingTransmutations() {
        SLAB_TO_NON_SLAB_MAPPINGS.forEach(r -> addSlabCondensingTransmutation(r.item1, r.item2));
    }

    private void addSlabCondensingTransmutation(Item slab, Item fullBlock) {
        addUnidirectionalTransmutation(slab, 2, fullBlock, TransmutationDirection.CONDENSING);
    }

    public void addUnidirectionalTransmutation(Item inputItem, Item outputItem, TransmutationDirection direction) {
        addUnidirectionalTransmutation(inputItem, 1, outputItem, 1, direction);
    }

    public void addUnidirectionalTransmutation(Item inputItem, int inputAmount, Item outputItem, TransmutationDirection direction) {
        addUnidirectionalTransmutation(inputItem, inputAmount, outputItem, 1, direction);
    }

    public void addUnidirectionalTransmutation(Item inputItem, int inputAmount, Item outputItem, int outputAmount, TransmutationDirection direction) {
        addTransmutation(inputItem, inputAmount, outputItem, outputAmount, direction, false);
    }

    public void addBidirectionalTransmutation(Item inputItem, int inputAmount, Item outputItem, int outputAmount) {
        addTransmutation(inputItem, inputAmount, outputItem, outputAmount, TransmutationDirection.CONDENSING, true);
    }

    public void addBidirectionalTransmutation(Item inputItem, int inputAmount, Item outputItem) {
        addBidirectionalTransmutation(inputItem, inputAmount, outputItem, 1);
    }

    public void addBidirectionalTransmutation(Item inputItem, Item outputItem) {
        addBidirectionalTransmutation(inputItem, 1, outputItem);
    }

    public void addTransmutation(Item inputItem, int inputAmount, Item outputItem, int outputAmount, TransmutationDirection direction, boolean isBidirectional) {
        transmutationRecipes.add(new TransmutationRecipe(inputItem, inputAmount, outputItem, outputAmount, direction));
        if (isBidirectional) {
            transmutationRecipes.add(new TransmutationRecipe(outputItem, outputAmount, inputItem, inputAmount, direction == TransmutationDirection.EXPANDING ? TransmutationDirection.CONDENSING : TransmutationDirection.EXPANDING));
        }
    }

    public boolean isInput(ItemStack stackInSlot, TransmutationDirection direction) {
        boolean result = false;
        for(TransmutationRecipe r : transmutationRecipes) {
            if (r.direction != direction) {
                continue;
            }
            if (r.input.isItemEqual(stackInSlot) && r.input.getCount() <= stackInSlot.getCount() && ItemStack.areItemStackTagsEqual(r.input, stackInSlot)) {
                result = true;
            }
        }
        return result;
    }

    public TransmutationRecipe getRecipe(ItemStack stackInSlot, TransmutationDirection direction) {
        TransmutationRecipe result = null;
        for(TransmutationRecipe r : transmutationRecipes) {
            if (r.direction != direction) {
                continue;
            }
            if (r.input.isItemEqual(stackInSlot) && r.input.getCount() <= stackInSlot.getCount() && ItemStack.areItemStackTagsEqual(r.input, stackInSlot)) {
                result = r;
            }
        }
        return result;
    }
}
