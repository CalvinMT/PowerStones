package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin {

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
