package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@Mixin(RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin {

    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

}
