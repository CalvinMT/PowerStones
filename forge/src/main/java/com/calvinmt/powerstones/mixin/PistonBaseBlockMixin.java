package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.LevelInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {

    @Redirect(method = "getNeighborSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", ordinal = 0))
    private boolean getNeighborSignalIsEmittingPower0(Level level, BlockPos pos, Direction direction) {
        return ((LevelInterface) level).isEmittingSignal(pos, direction);
    }

    @Redirect(method = "getNeighborSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", ordinal = 1))
    private boolean getNeighborSignalIsEmittingPower1(Level level, BlockPos pos, Direction direction) {
        return ((LevelInterface) level).isEmittingSignal(pos, direction);
    }

    @Redirect(method = "getNeighborSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z", ordinal = 2))
    private boolean getNeighborSignalIsEmittingPower2(Level level, BlockPos pos, Direction direction) {
        return ((LevelInterface) level).isEmittingSignal(pos, direction);
    }

}
