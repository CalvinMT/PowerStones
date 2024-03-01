package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.WorldInterface;
import com.calvinmt.powerstones.WorldViewInterface;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(World.class)
public abstract class WorldMixin implements WorldInterface {

    @Shadow
    private static final Direction[] DIRECTIONS = Direction.values();

    @Shadow
    public abstract BlockState getBlockState(BlockPos pos);
    @Shadow
    public abstract int getEmittedRedstonePower(BlockPos pos, Direction direction);
    @Shadow
    public abstract boolean isReceivingRedstonePower(BlockPos pos);
    @Shadow
    public abstract int getReceivedRedstonePower(BlockPos pos);

    @FunctionalInterface
    private interface BlockStatePowerFunction {
        int apply(AbstractBlockStateInterface blockState, World world, BlockPos position, Direction direction);
    }

    @FunctionalInterface
    private interface PowerFunction {
        int apply(BlockPos position, Direction direction);
    }

    @FunctionalInterface
    private interface StrongPowerFunction {
        int apply(WorldViewInterface world, BlockPos position, Direction direction);
    }

    @FunctionalInterface
    private interface StrongPowerToFunction {
        int apply(BlockPos position);
    }

    @Override
    public boolean isEmittingPower(BlockPos pos, Direction direction) {
        int redstonePower = this.getEmittedRedstonePower(pos, direction);
        int bluestonePower = this.getEmittedBluestonePower(pos, direction);
        int greenstonePower = this.getEmittedGreenstonePower(pos, direction);
        int yellowstonePower = this.getEmittedYellowstonePower(pos, direction);
        return redstonePower > 0 || bluestonePower > 0 || greenstonePower > 0 || yellowstonePower > 0;
    }

    @Override
    public boolean isReceivingPower(BlockPos pos) {
        return this.isReceivingRedstonePower(pos) || this.isReceivingBluestonePower(pos) || this.isReceivingGreenstonePower(pos) || this.isReceivingYellowstonePower(pos);
    }

    @Override
    public int getMaxPower(BlockPos pos, Direction direction) {
        return Math.max(this.getEmittedRedstonePower(pos, direction), Math.max(this.getEmittedBluestonePower(pos, direction), Math.max(this.getEmittedGreenstonePower(pos, direction), this.getEmittedYellowstonePower(pos, direction))));
    }

    private int getReceivedStrongColouredTo(BlockPos pos, StrongPowerFunction strongPowerFunction) {
        int i = 0;
        for(Direction direction : DIRECTIONS) {
            i = Math.max(i, strongPowerFunction.apply((WorldViewInterface) this, pos.offset(direction), direction));
            if (i >= 15) {
                break;
            }
        }
        return i;
    }

    public int getReceivedStrongBluestonePower(BlockPos pos) {
        return this.getReceivedStrongColouredTo(pos, (level, position, direction) -> level.getStrongBluestonePower(position, direction));
    }

    public int getReceivedStrongGreenstonePower(BlockPos pos) {
        return this.getReceivedStrongColouredTo(pos, (level, position, direction) -> level.getStrongGreenstonePower(position, direction));
    }

    public int getReceivedStrongYellowstonePower(BlockPos pos) {
        return this.getReceivedStrongColouredTo(pos, (level, position, direction) -> level.getStrongYellowstonePower(position, direction));
    }

    private int getEmittedPowerColoured(BlockPos pos, Direction direction, BlockStatePowerFunction blockStatePowerFunction, StrongPowerToFunction strongPowerToFunction) {
        BlockState blockState = this.getBlockState(pos);
        int i = blockStatePowerFunction.apply(((AbstractBlockStateInterface) blockState), (World)(Object)this, pos, direction);
        return blockState.isSolidBlock((World)(Object)this, pos) ? Math.max(i, strongPowerToFunction.apply(pos)) : i;
    }

    public int getEmittedBluestonePower(BlockPos pos, Direction direction) {
        return this.getEmittedPowerColoured(pos, direction, (blockState, level, position, dir) -> blockState.getWeakBluestonePower(level, position, dir), (position) -> this.getReceivedStrongBluestonePower(position));
    }

    public int getEmittedGreenstonePower(BlockPos pos, Direction direction) {
        return this.getEmittedPowerColoured(pos, direction, (blockState, level, position, dir) -> blockState.getWeakGreenstonePower(level, position, dir), (position) -> this.getReceivedStrongGreenstonePower(position));
    }

    public int getEmittedYellowstonePower(BlockPos pos, Direction direction) {
        return this.getEmittedPowerColoured(pos, direction, (blockState, level, position, dir) -> blockState.getWeakYellowstonePower(level, position, dir), (position) -> this.getReceivedStrongYellowstonePower(position));
    }

    private boolean isReceivingPowerColoured(BlockPos pos, PowerFunction powerFunction) {
        boolean result = false;
        for(Direction direction : DIRECTIONS) {
            if (powerFunction.apply(pos.offset(direction), direction) > 0) {
                result = true;
                break;
            }
        }
        return result;
    }

    @Override
    public boolean isReceivingBluestonePower(BlockPos pos) {
        return this.isReceivingPowerColoured(pos, (position, direction) -> this.getEmittedBluestonePower(position, direction));
    }

    @Override
    public boolean isReceivingGreenstonePower(BlockPos pos) {
        return this.isReceivingPowerColoured(pos, (position, direction) -> this.getEmittedGreenstonePower(position, direction));
    }

    @Override
    public boolean isReceivingYellowstonePower(BlockPos pos) {
        return this.isReceivingPowerColoured(pos, (position, direction) -> this.getEmittedYellowstonePower(position, direction));
    }

    private int getReceivedPowerColoured(BlockPos pos, PowerFunction powerFunction) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = powerFunction.apply(pos.offset(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    @Override
    public int getReceivedBluestonePower(BlockPos pos) {
        return this.getReceivedPowerColoured(pos, (position, direction) -> this.getEmittedBluestonePower(position, direction));
    }

    @Override
    public int getReceivedGreenstonePower(BlockPos pos) {
        return this.getReceivedPowerColoured(pos, (position, direction) -> this.getEmittedGreenstonePower(position, direction));
    }

    @Override
    public int getReceivedYellowstonePower(BlockPos pos) {
        return this.getReceivedPowerColoured(pos, (position, direction) -> this.getEmittedYellowstonePower(position, direction));
    }

}
