package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.calvinmt.powerstones.LevelInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DiodeBlock;

@Mixin(DiodeBlock.class)
public abstract class DiodeBlockMixin {

    @Redirect(method = "getInputSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I"))
    private int getInputSignalGetEmittingSignal(Level level, BlockPos pos, Direction direction) {
        return ((LevelInterface) level).getMaxSignal(pos, direction);
    }

}
