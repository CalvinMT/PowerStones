package com.calvinmt.powerstones;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface RedstoneWireBlockInterface {

    default void updateAll(BlockState state, Level level, BlockPos pos) {}

    default void setShouldSignal(boolean shouldSignal) {}

}
