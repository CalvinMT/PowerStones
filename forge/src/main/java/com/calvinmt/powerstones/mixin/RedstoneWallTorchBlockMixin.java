package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.LevelInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedstoneWallTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(RedstoneWallTorchBlock.class)
public class RedstoneWallTorchBlockMixin {

    @Redirect(method = "hasNeighborSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean shouldUnpowerIsEmittingSignal(Level level, BlockPos pos, Direction direction) {
        return ((LevelInterface)level).isEmittingSignal(pos, direction);
    }

    public int getSignalBlue(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getSignalGreen(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getSignalYellow(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getDirectSignalBlue(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getDirectSignalGreen(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getDirectSignalYellow(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

}
