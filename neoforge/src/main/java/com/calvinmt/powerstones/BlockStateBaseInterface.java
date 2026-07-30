package com.calvinmt.powerstones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;

public interface BlockStateBaseInterface {

    default int getSignalBlue(BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getSignalGreen(BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getSignalYellow(BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getDirectSignalBlue(BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getDirectSignalGreen(BlockGetter world, BlockPos pos, Direction direction) { return 0; }

    default int getDirectSignalYellow(BlockGetter world, BlockPos pos, Direction direction) { return 0; }

}
