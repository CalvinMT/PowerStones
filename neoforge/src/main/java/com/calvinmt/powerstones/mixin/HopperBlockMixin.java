package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.LevelInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;

@Mixin(HopperBlock.class)
public class HopperBlockMixin {

    @Redirect(method = "checkPoweredState", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasNeighborSignal(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean updateEnabledIsReceivingPower(Level level, BlockPos pos) {
        return ((LevelInterface) level).isReceivingSignal(pos);
    }

}
