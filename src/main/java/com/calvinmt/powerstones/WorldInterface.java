package com.calvinmt.powerstones;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public interface WorldInterface {

    default boolean isEmittingPower(BlockPos pos, Direction direction) { return false; }

    default boolean isReceivingPower(BlockPos pos) { return false; }

    default boolean isReceivingBluestonePower(BlockPos pos) { return false; }

    default boolean isReceivingGreenstonePower(BlockPos pos) { return false; }

    default boolean isReceivingYellowstonePower(BlockPos pos) { return false; }
    
    default int getReceivedBluestonePower(BlockPos pos) { return 0; }
    
    default int getReceivedGreenstonePower(BlockPos pos) { return 0; }
    
    default int getReceivedYellowstonePower(BlockPos pos) { return 0; }

}
