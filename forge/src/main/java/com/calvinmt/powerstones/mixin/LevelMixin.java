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

    public int getDirectSignalBlueTo(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalBlue(pos.below(), Direction.DOWN))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalBlue(pos.above(), Direction.UP))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalBlue(pos.north(), Direction.NORTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalBlue(pos.south(), Direction.SOUTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalBlue(pos.west(), Direction.WEST))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalBlue(pos.east(), Direction.EAST))) >= 15) {
            return i;
        }
        return i;
    }

    public int getDirectSignalGreenTo(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalGreen(pos.below(), Direction.DOWN))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalGreen(pos.above(), Direction.UP))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalGreen(pos.north(), Direction.NORTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalGreen(pos.south(), Direction.SOUTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalGreen(pos.west(), Direction.WEST))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalGreen(pos.east(), Direction.EAST))) >= 15) {
            return i;
        }
        return i;
    }

    public int getDirectSignalYellowTo(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalYellow(pos.below(), Direction.DOWN))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalYellow(pos.above(), Direction.UP))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalYellow(pos.north(), Direction.NORTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalYellow(pos.south(), Direction.SOUTH))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalYellow(pos.west(), Direction.WEST))) >= 15) {
            return i;
        }
        if ((i = Math.max(i, ((LevelReaderInterface) this).getDirectSignalYellow(pos.east(), Direction.EAST))) >= 15) {
            return i;
        }
        return i;
    }

    public int getSignalBlue(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        int i = ((BlockStateBaseInterface) blockState).getSignalBlue((Level)(Object)this, pos, direction);
        return blockState.shouldCheckWeakPower((Level)(Object)this, pos, direction) ? Math.max(i, this.getDirectSignalBlueTo(pos)) : i;
    }

    public int getSignalGreen(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        int i = ((BlockStateBaseInterface) blockState).getSignalGreen((Level)(Object)this, pos, direction);
        return blockState.shouldCheckWeakPower((Level)(Object)this, pos, direction) ? Math.max(i, this.getDirectSignalGreenTo(pos)) : i;
    }

    public int getSignalYellow(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        int i = ((BlockStateBaseInterface) blockState).getSignalYellow((Level)(Object)this, pos, direction);
        return blockState.shouldCheckWeakPower((Level)(Object)this, pos, direction) ? Math.max(i, this.getDirectSignalYellowTo(pos)) : i;
    }

    @Override
    public boolean hasNeighborSignalBlue(BlockPos pos) { 
        if (this.getSignalBlue(pos.below(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getSignalBlue(pos.above(), Direction.UP) > 0) {
            return true;
        }
        if (this.getSignalBlue(pos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getSignalBlue(pos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getSignalBlue(pos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getSignalBlue(pos.east(), Direction.EAST) > 0;
    }

    @Override
    public boolean hasNeighborSignalGreen(BlockPos pos) { 
        if (this.getSignalGreen(pos.below(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getSignalGreen(pos.above(), Direction.UP) > 0) {
            return true;
        }
        if (this.getSignalGreen(pos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getSignalGreen(pos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getSignalGreen(pos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getSignalGreen(pos.east(), Direction.EAST) > 0;
    }

    @Override
    public boolean hasNeighborSignalYellow(BlockPos pos) { 
        if (this.getSignalYellow(pos.below(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getSignalYellow(pos.above(), Direction.UP) > 0) {
            return true;
        }
        if (this.getSignalYellow(pos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getSignalYellow(pos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getSignalYellow(pos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getSignalYellow(pos.east(), Direction.EAST) > 0;
    }

    @Override
    public int getBestNeighborSignalBlue(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getSignalBlue(pos.relative(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    @Override
    public int getBestNeighborSignalGreen(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getSignalGreen(pos.relative(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    @Override
    public int getBestNeighborSignalYellow(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getSignalYellow(pos.relative(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

}
