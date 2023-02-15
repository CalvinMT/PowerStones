package com.calvinmt.powerstones;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public interface AbstractBlockInterface {

    default int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) { return 0; }

    default int getStrongBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) { return 0; }

}
