package com.calvinmt.powerstones;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public interface AbstractBlockStateInterface {

    default int getWeakBluestonePower(BlockView world, BlockPos pos, Direction direction) { return 0; }

    default int getWeakGreenstonePower(BlockView world, BlockPos pos, Direction direction) { return 0; }

    default int getWeakYellowstonePower(BlockView world, BlockPos pos, Direction direction) { return 0; }

    default int getStrongBluestonePower(BlockView world, BlockPos pos, Direction direction) { return 0; }

    default int getStrongGreenstonePower(BlockView world, BlockPos pos, Direction direction) { return 0; }

    default int getStrongYellowstonePower(BlockView world, BlockPos pos, Direction direction) { return 0; }

}
