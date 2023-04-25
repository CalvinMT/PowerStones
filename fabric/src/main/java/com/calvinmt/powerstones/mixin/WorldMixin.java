package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.WorldInterface;
import com.calvinmt.powerstones.WorldViewInterface;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

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

    @Overwrite
    public int getReceivedStrongRedstonePower(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((WorldView)this).getStrongRedstonePower(pos.down(), Direction.DOWN))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldView)this).getStrongRedstonePower(pos.up(), Direction.UP))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldView)this).getStrongRedstonePower(pos.north(), Direction.NORTH))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldView)this).getStrongRedstonePower(pos.south(), Direction.SOUTH))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldView)this).getStrongRedstonePower(pos.west(), Direction.WEST))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldView)this).getStrongRedstonePower(pos.east(), Direction.EAST))) == 15) {
            return i;
        }
        return i;
    }

    @Overwrite
    public int getReceivedRedstonePower(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getEmittedRedstonePower(pos.offset(direction), direction);
            if (j == 15) {
                return 15;
            }
            if (j == 16 || j <= i) continue;
            i = j;
        }
        return i;
    }

    public boolean isEmittingPower(BlockPos pos, Direction direction) {
        int redstonePower = this.getEmittedRedstonePower(pos, direction);
        int bluestonePower = this.getEmittedBluestonePower(pos, direction);
        int greenstonePower = this.getEmittedGreenstonePower(pos, direction);
        int yellowstonePower = this.getEmittedYellowstonePower(pos, direction);
        return (redstonePower > 0 && redstonePower < 16)
            || (bluestonePower > 0 && bluestonePower < 16)
            || (greenstonePower > 0 && greenstonePower < 16)
            || (yellowstonePower > 0 && yellowstonePower < 16);
    }
    
    public boolean isReceivingPower(BlockPos pos) {
        return this.isReceivingRedstonePower(pos) || this.isReceivingBluestonePower(pos) || this.isReceivingGreenstonePower(pos) || this.isReceivingYellowstonePower(pos);
    }
    
    public int getMaxReceivedPower(BlockPos pos) {
        return Math.max(this.getReceivedRedstonePower(pos), Math.max(this.getReceivedBluestonePower(pos), Math.max(this.getReceivedGreenstonePower(pos), this.getReceivedYellowstonePower(pos))));
    }

    public int getReceivedStrongBluestonePower(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.down(), Direction.DOWN))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.up(), Direction.UP))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.north(), Direction.NORTH))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.south(), Direction.SOUTH))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.west(), Direction.WEST))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.east(), Direction.EAST))) == 15) {
            return i;
        }
        return i;
    }

    public int getReceivedStrongGreenstonePower(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.down(), Direction.DOWN))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.up(), Direction.UP))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.north(), Direction.NORTH))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.south(), Direction.SOUTH))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.west(), Direction.WEST))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.east(), Direction.EAST))) == 15) {
            return i;
        }
        return i;
    }

    public int getReceivedStrongYellowstonePower(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.down(), Direction.DOWN))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.up(), Direction.UP))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.north(), Direction.NORTH))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.south(), Direction.SOUTH))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.west(), Direction.WEST))) == 15) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.east(), Direction.EAST))) == 15) {
            return i;
        }
        return i;
    }

    public int getEmittedBluestonePower(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        int i = ((AbstractBlockStateInterface) blockState).getWeakBluestonePower((World)((Object) this), pos, direction);
        if (blockState.isSolidBlock((World)((Object) this), pos)) {
            return Math.max(i, this.getReceivedStrongBluestonePower(pos));
        }
        return i;
    }

    public int getEmittedGreenstonePower(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        int i = ((AbstractBlockStateInterface) blockState).getWeakGreenstonePower((World)((Object) this), pos, direction);
        if (blockState.isSolidBlock((World)((Object) this), pos)) {
            return Math.max(i, this.getReceivedStrongGreenstonePower(pos));
        }
        return i;
    }

    public int getEmittedYellowstonePower(BlockPos pos, Direction direction) {
        BlockState blockState = this.getBlockState(pos);
        int i = ((AbstractBlockStateInterface) blockState).getWeakYellowstonePower((World)((Object) this), pos, direction);
        if (blockState.isSolidBlock((World)((Object) this), pos)) {
            return Math.max(i, this.getReceivedStrongYellowstonePower(pos));
        }
        return i;
    }

    @Override
    public boolean isReceivingBluestonePower(BlockPos pos) { 
        if (this.getEmittedBluestonePower(pos.down(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getEmittedBluestonePower(pos.up(), Direction.UP) > 0) {
            return true;
        }
        if (this.getEmittedBluestonePower(pos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getEmittedBluestonePower(pos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getEmittedBluestonePower(pos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getEmittedBluestonePower(pos.east(), Direction.EAST) > 0;
    }

    @Override
    public boolean isReceivingGreenstonePower(BlockPos pos) { 
        if (this.getEmittedGreenstonePower(pos.down(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getEmittedGreenstonePower(pos.up(), Direction.UP) > 0) {
            return true;
        }
        if (this.getEmittedGreenstonePower(pos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getEmittedGreenstonePower(pos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getEmittedGreenstonePower(pos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getEmittedGreenstonePower(pos.east(), Direction.EAST) > 0;
    }

    @Override
    public boolean isReceivingYellowstonePower(BlockPos pos) { 
        if (this.getEmittedYellowstonePower(pos.down(), Direction.DOWN) > 0) {
            return true;
        }
        if (this.getEmittedYellowstonePower(pos.up(), Direction.UP) > 0) {
            return true;
        }
        if (this.getEmittedYellowstonePower(pos.north(), Direction.NORTH) > 0) {
            return true;
        }
        if (this.getEmittedYellowstonePower(pos.south(), Direction.SOUTH) > 0) {
            return true;
        }
        if (this.getEmittedYellowstonePower(pos.west(), Direction.WEST) > 0) {
            return true;
        }
        return this.getEmittedYellowstonePower(pos.east(), Direction.EAST) > 0;
    }

    @Override
    public int getReceivedBluestonePower(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getEmittedBluestonePower(pos.offset(direction), direction);
            if (j == 15) {
                return 15;
            }
            if (j == 16 || j <= i) continue;
            i = j;
        }
        return i;
    }

    @Override
    public int getReceivedGreenstonePower(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getEmittedGreenstonePower(pos.offset(direction), direction);
            if (j == 15) {
                return 15;
            }
            if (j == 16 || j <= i) continue;
            i = j;
        }
        return i;
    }

    @Override
    public int getReceivedYellowstonePower(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getEmittedYellowstonePower(pos.offset(direction), direction);
            if (j == 15) {
                return 15;
            }
            if (j == 16 || j <= i) continue;
            i = j;
        }
        return i;
    }

}
