package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import com.calvinmt.powerstones.BlockStateBaseInterface;
import com.calvinmt.powerstones.LevelInterface;
import com.calvinmt.powerstones.LevelReaderInterface;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelInterface {

    @Shadow
    private static final Direction[] DIRECTIONS = Direction.values();

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);
    @Shadow
    public abstract int getSignal(BlockPos pos, Direction direction);
    @Shadow
    public abstract boolean hasNeighborSignal(BlockPos pos);
    @Shadow
    public abstract int getBestNeighborSignal(BlockPos pos);

    @FunctionalInterface
    private interface BlockStateSignalFunction {
        int apply(BlockStateBaseInterface blockState, Level level, BlockPos position, Direction direction);
    }

    @FunctionalInterface
    private interface SignalFunction {
        int apply(BlockPos position, Direction direction);
    }

    @FunctionalInterface
    private interface DirectSignalFunction {
        int apply(LevelReaderInterface level, BlockPos position, Direction direction);
    }

    @FunctionalInterface
    private interface DirectSignalToFunction {
        int apply(BlockPos position);
    }

    @Override
    public boolean isEmittingSignal(BlockPos pos, Direction direction) {
        int redstonePower = this.getSignal(pos, direction);
        int bluestonePower = this.getSignalBlue(pos, direction);
        int greenstonePower = this.getSignalGreen(pos, direction);
        int yellowstonePower = this.getSignalYellow(pos, direction);
        return redstonePower > 0 || bluestonePower > 0 || greenstonePower > 0 || yellowstonePower > 0;
    }

    @Override
    public boolean isReceivingSignal(BlockPos pos) {
        return this.hasNeighborSignal(pos) || this.hasNeighborSignalBlue(pos) || this.hasNeighborSignalGreen(pos) || this.hasNeighborSignalYellow(pos);
    }

    @Override
    public int getMaxSignal(BlockPos pos, Direction direction) {
        return Math.max(this.getSignal(pos, direction), Math.max(this.getSignalBlue(pos, direction), Math.max(this.getSignalGreen(pos, direction), this.getSignalYellow(pos, direction))));
    }

    private int getDirectSignalColouredTo(BlockPos pos, DirectSignalFunction directSignalFunction) {
        int i = 0;
        for(Direction direction : DIRECTIONS) {
            i = Math.max(i, directSignalFunction.apply((LevelReaderInterface) this, pos, direction));
            if (i >= 15) {
                break;
            }
        }
        return i;
    }

    public int getDirectSignalBlueTo(BlockPos pos) {
        return this.getDirectSignalColouredTo(pos, (level, position, direction) -> level.getDirectSignalBlue(position, direction));
    }

    public int getDirectSignalGreenTo(BlockPos pos) {
        return this.getDirectSignalColouredTo(pos, (level, position, direction) -> level.getDirectSignalGreen(position, direction));
    }

    public int getDirectSignalYellowTo(BlockPos pos) {
        return this.getDirectSignalColouredTo(pos, (level, position, direction) -> level.getDirectSignalYellow(position, direction));
    }

    private int getSignalColoured(BlockPos pos, Direction direction, BlockStateSignalFunction blockStateSignalFunction, DirectSignalToFunction directSignalToFunction) {
        BlockState blockState = this.getBlockState(pos);
        int i = blockStateSignalFunction.apply(((BlockStateBaseInterface) blockState), (Level)(Object)this, pos, direction);
        return blockState.shouldCheckWeakPower((Level)(Object)this, pos, direction) ? Math.max(i, directSignalToFunction.apply(pos)) : i;
    }

    public int getSignalBlue(BlockPos pos, Direction direction) {
        return this.getSignalColoured(pos, direction, (blockState, level, position, dir) -> blockState.getSignalBlue(level, position, dir), (position) -> this.getDirectSignalBlueTo(position));
    }

    public int getSignalGreen(BlockPos pos, Direction direction) {
        return this.getSignalColoured(pos, direction, (blockState, level, position, dir) -> blockState.getSignalGreen(level, position, dir), (position) -> this.getDirectSignalGreenTo(position));
    }

    public int getSignalYellow(BlockPos pos, Direction direction) {
        return this.getSignalColoured(pos, direction, (blockState, level, position, dir) -> blockState.getSignalYellow(level, position, dir), (position) -> this.getDirectSignalYellowTo(position));
    }

    private boolean hasNeighborSignalColoured(BlockPos pos, SignalFunction signalFunction) {
        boolean result = false;
        for(Direction direction : DIRECTIONS) {
            if (signalFunction.apply(pos.relative(direction), direction) > 0) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public boolean hasNeighborSignalBlue(BlockPos pos) {
        return this.hasNeighborSignalColoured(pos, (position, direction) -> this.getSignalBlue(position, direction));
    }

    @Override
    public boolean hasNeighborSignalGreen(BlockPos pos) {
        return this.hasNeighborSignalColoured(pos, (position, direction) -> this.getSignalGreen(position, direction));
    }

    @Override
    public boolean hasNeighborSignalYellow(BlockPos pos) {
        return this.hasNeighborSignalColoured(pos, (position, direction) -> this.getSignalYellow(position, direction));
    }

    private int getBestNeighborSignalColoured(BlockPos pos, SignalFunction signalFunction) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = signalFunction.apply(pos.relative(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    @Override
    public int getBestNeighborSignalBlue(BlockPos pos) {
        return this.getBestNeighborSignalColoured(pos, (position, direction) -> this.getSignalBlue(position, direction));
    }

    @Override
    public int getBestNeighborSignalGreen(BlockPos pos) {
        return this.getBestNeighborSignalColoured(pos, (position, direction) -> this.getSignalGreen(position, direction));
    }

    @Override
    public int getBestNeighborSignalYellow(BlockPos pos) {
        return this.getBestNeighborSignalColoured(pos, (position, direction) -> this.getSignalYellow(position, direction));
    }

}
