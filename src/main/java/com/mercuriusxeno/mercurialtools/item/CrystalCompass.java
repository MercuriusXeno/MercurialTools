package com.mercuriusxeno.mercurialtools.item;

import com.mercuriusxeno.mercurialtools.MercurialTools;
import com.mercuriusxeno.mercurialtools.reference.Constants;
import com.mercuriusxeno.mercurialtools.reference.Names;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrystalCompass extends Item {

    private static final List<String> overworldCrystals = new ArrayList<>(Arrays.asList(
        "diamond_ore",
        "lapis_ore",
        "emerald_ore")
    );

    private static final List<String> underworldCrystals = new ArrayList<>(Arrays.asList(
            "nether_quartz_ore")
    );

    public CrystalCompass() {
        super(new Properties()
                .maxStackSize(1)
                .group(MercurialTools.setup.itemGroup));
        setRegistryName(Names.CRYSTAL_COMPASS);

        this.addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter() {
            @OnlyIn(Dist.CLIENT)
            private double rotation;
            @OnlyIn(Dist.CLIENT)
            private double rota;
            @OnlyIn(Dist.CLIENT)
            private long lastUpdateTick;
            // updates are more stressful on the client than a normal compass
            // so we throttle the calls by about 90%, and pass the same rotation back between runs.
            @OnlyIn(Dist.CLIENT)
            private float previousRotation = 0.0F;
            // more caching to prevent unnecessary update calls unless the player significantly moves.
            private BlockPos latestDiscoveredBlock = null;
            private int lastFlooredX = 0;
            private int lastFlooredY = 0;
            private int lastFlooredZ = 0;

            @OnlyIn(Dist.CLIENT)
            public float call(ItemStack itemStack, @Nullable World worldIn, @Nullable LivingEntity usingEntity) {
                if (usingEntity == null && !itemStack.isOnItemFrame()) {
                    return 0.0F;
                }
                boolean isEntityNotNull = usingEntity != null;
                Entity entity = (isEntityNotNull ? usingEntity : itemStack.getItemFrame());

                int entityX = (int)Math.floor(entity.posX);
                int entityY = (int)Math.floor(entity.posY);
                int entityZ = (int)Math.floor(entity.posZ);

                if (worldIn == null) {
                    worldIn = entity.world;
                }

                // throttle the number of calls by 50% by returning when the modulo of 10 is greater than 0
                if (worldIn.getGameTime() % 2 > 0) {
                    return previousRotation;
                }

                // attempt to bypass the scan algorithm if the player hasn't moved far enough
                // from the last polling position to prevent unnecessary seeking
                double resultAngle;


                    List<String> crystalsToSeek = worldIn.dimension.isSurfaceWorld() ? overworldCrystals :
                            (worldIn.dimension.isNether() ? underworldCrystals : null);
                    if (crystalsToSeek == null) {
                        resultAngle = Math.random();
                    } else {
                        double initialAngle = isEntityNotNull ? (double)entity.rotationYaw : this.getFrameRotation((ItemFrameEntity)entity);
                        initialAngle = MathHelper.positiveModulo(initialAngle / 360.0D, 1.0D);
                        BlockPos newCrystalPosition = null;
                        if (lastFlooredX != entityX || lastFlooredY != entityY || lastFlooredZ != entityZ) {
                            newCrystalPosition = getClosestCrystalOre(crystalsToSeek, worldIn, usingEntity, entityX, entityY, entityZ);
                            // capture the last "found" states so that we can stop firing this until the player has moved a bit.
                            lastFlooredX = entityX;
                            lastFlooredY = entityY;
                            lastFlooredZ = entityZ;
                            latestDiscoveredBlock = newCrystalPosition;
                        }
                        BlockPos effectiveCrystalPosition = newCrystalPosition == null ? latestDiscoveredBlock : newCrystalPosition;
                        if (effectiveCrystalPosition == null) {
                            resultAngle = Math.random();
                        } else {
                            double crystalAngle = this.getNearestCrystalAngle(effectiveCrystalPosition, usingEntity) / (double) ((float) Math.PI * 2F);
                            resultAngle = 0.5D - (initialAngle - 0.25D - crystalAngle);
                        }
                    }

                if (isEntityNotNull) {
                    resultAngle = this.wobble(worldIn, resultAngle);
                }

                previousRotation = MathHelper.positiveModulo((float)resultAngle, 1.0F);
                return previousRotation;
            }

            @OnlyIn(Dist.CLIENT)
            private double wobble(World worldIn, double passedAngle) {
                if (worldIn.getGameTime() != this.lastUpdateTick) {
                    this.lastUpdateTick = worldIn.getGameTime();
                    double resultAngle = passedAngle - this.rotation;
                    resultAngle = MathHelper.positiveModulo(resultAngle + 0.5D, 1.0D) - 0.5D;
                    this.rota += resultAngle * 0.1D;
                    this.rota *= 0.8D;
                    this.rotation = MathHelper.positiveModulo(this.rotation + this.rota, 1.0D);
                }

                return this.rotation;
            }

            @OnlyIn(Dist.CLIENT)
            private double getFrameRotation(ItemFrameEntity itemEntity) {
                return MathHelper.wrapDegrees(180 + itemEntity.getHorizontalFacing().getHorizontalIndex() * 90);
            }

            @OnlyIn(Dist.CLIENT)
            private double getNearestCrystalAngle(BlockPos blockPosition, Entity usingEntity) {

                if (blockPosition == null) {
                    return Math.random();
                }
                return Math.atan2((double)blockPosition.getZ() + 0.5D - usingEntity.posZ, (double)blockPosition.getX() + 0.5D - usingEntity.posX);
            }

            @OnlyIn(Dist.CLIENT)
            private BlockPos getClosestCrystalOre(List<String> crystalNames, IWorld worldIn, Entity usingEntity, int posX, int posY, int posZ) {
                BlockPos closestBlockPos = null;
                double closestDistanceSq = Double.MAX_VALUE;

                for(int x = 0; x <= Constants.CRYSTAL_COMPASS_RANGE; x++) {
                    for(int y = 0; y <= Constants.CRYSTAL_COMPASS_RANGE; y++) {
                        for (int z = 0; z <= Constants.CRYSTAL_COMPASS_RANGE; z++) {
                            double distance = usingEntity.getDistanceSq(posX + x, posY + y, posZ + z);
                            if (distance > closestDistanceSq) {
                                continue;
                            }
                            if (distance < closestDistanceSq) {
                                BlockPos[] blockPositions = new BlockPos[8];

                                // middle out style search pattern, starts close to the player and moves out
                                // sort of simultaneously
                                blockPositions[0] = new BlockPos(posX + x, posY + y, posZ + z);
                                if (x > 0) {
                                    blockPositions[1] = new BlockPos(posX - x, posY + y, posZ + z);
                                }
                                if (y > 0) {
                                    blockPositions[2] = new BlockPos(posX + x, posY - y, posZ + z);
                                }
                                if (z > 0) {
                                    blockPositions[3] = new BlockPos(posX + x, posY + y, posZ - z);
                                }
                                if (x > 0 && y > 0) {
                                    blockPositions[4] = new BlockPos(posX - x, posY - y, posZ + z);
                                }
                                if (x > 0 && z > 0) {
                                    blockPositions[5] = new BlockPos(posX - x, posY + y, posZ - z);
                                }
                                if (y > 0 && z > 0) {
                                    blockPositions[6] = new BlockPos(posX + x, posY - y, posZ - z);
                                }
                                if (x > 0 && y > 0 && z > 0) {
                                    blockPositions[7] = new BlockPos(posX - x, posY - y, posZ - z);
                                }
                                for (BlockPos pos : blockPositions) {
                                    if (pos == null) {
                                        continue;
                                    }
                                    BlockState state = worldIn.getBlockState(pos);
                                    if (crystalNames.contains(state.getBlock().getRegistryName().getPath())) {
                                        closestDistanceSq = distance;
                                        closestBlockPos = pos;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                return closestBlockPos;
            }
        });
    }
}
