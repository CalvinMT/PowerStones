package com.calvinmt.powerstones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockBehaviourInterface {

    default int getSignalBlue(BlockState state, BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getSignalGreen(BlockState state, BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getSignalYellow(BlockState state, BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getDirectSignalBlue(BlockState state, BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getDirectSignalGreen(BlockState state, BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getDirectSignalYellow(BlockState state, BlockGetter world, BlockPos pos, Direction direction) { return 0; }

}
