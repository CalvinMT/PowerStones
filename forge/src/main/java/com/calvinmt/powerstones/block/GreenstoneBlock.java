package com.calvinmt.powerstones.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class GreenstoneBlock extends Block {

    public GreenstoneBlock(Properties p_49795_) {
        super(p_49795_);
    }

    @Override
    public boolean isSignalSource(BlockState p_55213_) {
       return true;
    }
 
    public int getSignalGreen(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
       return 15;
    }

}
