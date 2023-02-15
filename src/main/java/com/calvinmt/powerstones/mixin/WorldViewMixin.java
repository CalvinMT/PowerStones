package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.WorldViewInterface;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.WorldView;

@Mixin(WorldView.class)
public interface WorldViewMixin extends BlockRenderView, WorldViewInterface {

    @Override
    default public int getStrongBluestonePower(BlockPos pos, Direction direction) {
        return ((AbstractBlockStateInterface) this.getBlockState(pos)).getStrongBluestonePower(this, pos, direction);
    }

    @Override
    default public int getStrongGreenstonePower(BlockPos pos, Direction direction) {
        return ((AbstractBlockStateInterface) this.getBlockState(pos)).getStrongBluestonePower(this, pos, direction);
    }

    @Override
    default public int getStrongYellowstonePower(BlockPos pos, Direction direction) {
        return ((AbstractBlockStateInterface) this.getBlockState(pos)).getStrongBluestonePower(this, pos, direction);
    }

}
