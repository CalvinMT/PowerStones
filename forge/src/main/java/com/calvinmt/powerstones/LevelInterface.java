package com.calvinmt.powerstones;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public interface LevelInterface {

    default boolean isEmittingSignal(BlockPos pos, Direction direction) { return false; }

    default boolean isReceivingSignal(BlockPos pos) { return false; }
    
    default int getMaxSignal(BlockPos pos, Direction direction) { return 0; }

    default boolean hasNeighborSignalBlue(BlockPos pos) { return false; }

    default boolean hasNeighborSignalGreen(BlockPos pos) { return false; }

    default boolean hasNeighborSignalYellow(BlockPos pos) { return false; }
    
    default int getBestNeighborSignalBlue(BlockPos pos) { return 0; }
    
    default int getBestNeighborSignalGreen(BlockPos pos) { return 0; }
    
    default int getBestNeighborSignalYellow(BlockPos pos) { return 0; }

}
