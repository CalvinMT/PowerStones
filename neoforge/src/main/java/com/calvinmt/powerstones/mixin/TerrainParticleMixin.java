package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(TerrainParticle.class)
public class TerrainParticleMixin {

    private static boolean didRed = false;
    private static boolean didGreen = false;

    @ModifyConstant(method = "<init>(Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDDLnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", constant = @Constant(intValue = 0))
    private int BlockDustParticleTintIndex(int oldTintIndex, ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockPos pos) {
        if (state.is(PowerStones.MULTIPLE_WIRES.get())) {
            if (! didRed && state.getValue(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE) {
				didRed = true;
                return 0;
            }
            if (state.getValue(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE) {
                didRed = false;
                return 1;
            }
            if (! didGreen && state.getValue(PowerStones.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
				didGreen = true;
                return 2;
            }
            if (state.getValue(PowerStones.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
                didGreen = false;
                return 3;
            }
        }
        return oldTintIndex;
    }

}
