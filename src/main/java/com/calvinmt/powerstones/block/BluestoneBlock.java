package com.calvinmt.powerstones.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class BluestoneBlock
extends Block {
    public BluestoneBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 15;
    }

}
