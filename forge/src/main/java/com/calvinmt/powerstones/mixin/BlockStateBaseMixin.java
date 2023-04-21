package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.calvinmt.powerstones.BlockBehaviourInterface;
import com.calvinmt.powerstones.BlockStateBaseInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(targets = "net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase")
public abstract class BlockStateBaseMixin implements BlockStateBaseInterface {

    @Shadow
    public Block getBlock() { return null; }
    @Shadow
    protected abstract BlockState asState();

    @Override
    public int getSignalBlue(BlockGetter world, BlockPos pos, Direction direction) {
        return ((BlockBehaviourInterface) this.getBlock()).getSignalBlue(this.asState(), world, pos, direction);
    }

    @Override
    public int getSignalGreen(BlockGetter world, BlockPos pos, Direction direction) {
        return ((BlockBehaviourInterface) this.getBlock()).getSignalGreen(this.asState(), world, pos, direction);
    }

    @Override
    public int getSignalYellow(BlockGetter world, BlockPos pos, Direction direction) {
        return ((BlockBehaviourInterface) this.getBlock()).getSignalYellow(this.asState(), world, pos, direction);
    }

    @Override
    public int getDirectSignalBlue(BlockGetter world, BlockPos pos, Direction direction) {
        return ((BlockBehaviourInterface) this.getBlock()).getDirectSignalBlue(this.asState(), world, pos, direction);
    }

    @Override
    public int getDirectSignalGreen(BlockGetter world, BlockPos pos, Direction direction) {
        return ((BlockBehaviourInterface) this.getBlock()).getDirectSignalGreen(this.asState(), world, pos, direction);
    }

    @Override
    public int getDirectSignalYellow(BlockGetter world, BlockPos pos, Direction direction) {
        return ((BlockBehaviourInterface) this.getBlock()).getDirectSignalYellow(this.asState(), world, pos, direction);
    }

}
