package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.LevelInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.piston.PistonBaseBlock;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    @Redirect(method = "getNeighborSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/SignalGetter;hasSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", ordinal = 0))
    private boolean getNeighborSignalIsEmittingPower0(SignalGetter level, BlockPos pos, Direction direction) {
        return ((LevelInterface) level).isEmittingSignal(pos, direction);
    }

    @Redirect(method = "getNeighborSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/SignalGetter;hasSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", ordinal = 1))
    private boolean getNeighborSignalIsEmittingPower1(SignalGetter level, BlockPos pos, Direction direction) {
        return ((LevelInterface) level).isEmittingSignal(pos, direction);
    }

    @Redirect(method = "getNeighborSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/SignalGetter;hasSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", ordinal = 2))
    private boolean getNeighborSignalIsEmittingPower2(SignalGetter level, BlockPos pos, Direction direction) {
        return ((LevelInterface) level).isEmittingSignal(pos, direction);
    }

}
