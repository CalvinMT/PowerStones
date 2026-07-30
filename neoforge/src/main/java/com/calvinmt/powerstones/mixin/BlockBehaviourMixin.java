package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.calvinmt.powerstones.BlockBehaviourInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin implements BlockBehaviourInterface {

    @Shadow
    public abstract int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction);
    @Shadow
    public abstract int getDirectSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction);

    @Override
    public int getSignalBlue(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getSignal(state, world, pos, direction);
    }

    @Override
    public int getSignalGreen(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getSignal(state, world, pos, direction);
    }

    @Override
    public int getSignalYellow(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getSignal(state, world, pos, direction);
    }

    @Override
    public int getDirectSignalBlue(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getDirectSignal(state, world, pos, direction);
    }

    @Override
    public int getDirectSignalGreen(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getDirectSignal(state, world, pos, direction);
    }

    @Override
    public int getDirectSignalYellow(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        return getDirectSignal(state, world, pos, direction);
    }

}
