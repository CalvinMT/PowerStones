package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockDustParticle.class)
public class BlockDustParticleMixin {

    private static boolean didRed = false;
    private static boolean didGreen = false;

    @ModifyConstant(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", constant = @Constant(intValue = 0))
    private int BlockDustParticleTintIndex(int oldTintIndex, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockPos blockPos) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) {
            if (! didRed && state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE && state.get(RedstoneWireBlock.POWER) < 16) {
                if (state.get(PowerstoneWireBlock.POWER_B) < 16) {
                    didRed = true;
                }
                return 0;
            }
            if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE && state.get(PowerstoneWireBlock.POWER_B) < 16) {
                didRed = false;
                return 1;
            }
            if (! didGreen && state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW && state.get(RedstoneWireBlock.POWER) < 16) {
                if (state.get(PowerstoneWireBlock.POWER_B) < 16) {
                    didGreen = true;
                }
                return 2;
            }
            if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW && state.get(PowerstoneWireBlock.POWER_B) < 16) {
                didGreen = false;
                return 3;
            }
        }
        return oldTintIndex;
    }

}
