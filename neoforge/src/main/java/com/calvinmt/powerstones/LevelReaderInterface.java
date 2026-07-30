package com.calvinmt.powerstones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface LevelReaderInterface {

    default public int getDirectSignalBlue(BlockPos pos, Direction direction) { return 0; }

    default public int getDirectSignalGreen(BlockPos pos, Direction direction) { return 0; }

    default public int getDirectSignalYellow(BlockPos pos, Direction direction) { return 0; }

}
