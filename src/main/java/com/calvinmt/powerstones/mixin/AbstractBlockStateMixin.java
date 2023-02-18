package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.calvinmt.powerstones.AbstractBlockInterface;
import com.calvinmt.powerstones.AbstractBlockStateInterface;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@Mixin(targets = "net/minecraft/block/AbstractBlock$AbstractBlockState")
public abstract class AbstractBlockStateMixin implements AbstractBlockStateInterface {

    @Shadow
    public Block getBlock() { return null; }
    @Shadow
    protected abstract BlockState asBlockState();

    @Override
    public int getWeakBluestonePower(BlockView world, BlockPos pos, Direction direction) {
        return ((AbstractBlockInterface) this.getBlock()).getWeakBluestonePower(this.asBlockState(), world, pos, direction);
    }

    @Override
    public int getWeakGreenstonePower(BlockView world, BlockPos pos, Direction direction) {
        return ((AbstractBlockInterface) this.getBlock()).getWeakGreenstonePower(this.asBlockState(), world, pos, direction);
    }

    @Override
    public int getWeakYellowstonePower(BlockView world, BlockPos pos, Direction direction) {
        return ((AbstractBlockInterface) this.getBlock()).getWeakYellowstonePower(this.asBlockState(), world, pos, direction);
    }

    @Override
    public int getStrongBluestonePower(BlockView world, BlockPos pos, Direction direction) {
        return ((AbstractBlockInterface) this.getBlock()).getStrongBluestonePower(this.asBlockState(), world, pos, direction);
    }

    @Override
    public int getStrongGreenstonePower(BlockView world, BlockPos pos, Direction direction) {
        return ((AbstractBlockInterface) this.getBlock()).getStrongGreenstonePower(this.asBlockState(), world, pos, direction);
    }

    @Override
    public int getStrongYellowstonePower(BlockView world, BlockPos pos, Direction direction) {
        return ((AbstractBlockInterface) this.getBlock()).getStrongYellowstonePower(this.asBlockState(), world, pos, direction);
    }

}
