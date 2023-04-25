package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.PoweredBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(PoweredBlock.class)
public abstract class PoweredBlockMixin {

    public int getSignalBlue(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getSignalGreen(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getSignalYellow(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return 0;
    }

}
