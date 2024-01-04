package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockDustParticle.class)
public class BlockDustParticleMixin {

    private static boolean didRed = false;
    private static boolean didGreen = false;

    @ModifyConstant(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", constant = @Constant(intValue = 0))
    private int BlockDustParticleTintIndex(int oldTintIndex, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockPos blockPos) {
        if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            if (! didRed && state.get(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE) {
                didRed = true;
                return 0;
            }
            if (state.get(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE) {
                didRed = false;
                return 1;
            }
            if (! didGreen && state.get(PowerStones.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
                didGreen = true;
                return 2;
            }
            if (state.get(PowerStones.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
                didGreen = false;
                return 3;
            }
        }
        return oldTintIndex;
    }

}
