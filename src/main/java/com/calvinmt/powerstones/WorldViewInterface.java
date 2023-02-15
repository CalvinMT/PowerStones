package com.calvinmt.powerstones;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface WorldViewInterface {

    default public int getStrongBluestonePower(BlockPos pos, Direction direction) { return 0; }

    default public int getStrongGreenstonePower(BlockPos pos, Direction direction) { return 0; }

    default public int getStrongYellowstonePower(BlockPos pos, Direction direction) { return 0; }

}
