package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.calvinmt.powerstones.PowerColour;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;

import net.minecraft.block.BlockState;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(BlockDustParticle.class)
public class BlockDustParticleMixin {

    @Unique
    private boolean powerstones$didChannelA;

    @ModifyConstant(method = "<init>(Lnet/minecraft/client/world/ClientWorld;DDDDDDLnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)V", constant = @Constant(intValue = 0))
    private int BlockDustParticleTintIndex(int oldTintIndex, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, BlockState state, BlockPos blockPos) {
        if (!state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return oldTintIndex;
        }

        PowerPair powerPair = state.get(PowerStones.POWER_PAIR);
        PowerColour colour = this.powerstones$didChannelA ? powerPair.getColourB() : powerPair.getColourA();

        this.powerstones$didChannelA = !this.powerstones$didChannelA;

        return colour.getTintIndex();
    }

}
