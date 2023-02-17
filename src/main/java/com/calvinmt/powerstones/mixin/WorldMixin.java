package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant.Condition;

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

    @ModifyConstant(method = "getReceivedStrongRedstonePower(Lnet/minecraft/util/math/BlockPos;)I", constant = @Constant(intValue = 15))
    private int getReceivedStrongRedstonePowerMaxPower(int oldMaxPower) {
        return 16;
    }

    @ModifyConstant(method = "isEmittingRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", constant = @Constant(expandZeroConditions = Condition.GREATER_THAN_ZERO))
    private int isEmittingRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z", constant = @Constant(expandZeroConditions = Condition.GREATER_THAN_ZERO))
    private int isReceivingRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "getReceivedRedstonePower(Lnet/minecraft/util/math/BlockPos;)I", constant = @Constant(intValue = 15))
    private int getReceivedRedstonePowerMaxPower(int oldMaxPower) {
        return 16;
    }

    public boolean isEmittingPower(BlockPos pos, Direction direction) {
        return this.getEmittedRedstonePower(pos, direction) > 1 || this.getEmittedBluestonePower(pos, direction) > 1;
    }
    
    public boolean isReceivingPower(BlockPos pos) {
        return this.isReceivingRedstonePower(pos) || this.isReceivingBluestonePower(pos);
    }
    
    public int getMaxReceivedPower(BlockPos pos) {
        return Math.max(this.getReceivedRedstonePower(pos), this.getReceivedBluestonePower(pos));
    }

    public int getReceivedStrongBluestonePower(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.down(), Direction.DOWN))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.up(), Direction.UP))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.north(), Direction.NORTH))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.south(), Direction.SOUTH))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.west(), Direction.WEST))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongBluestonePower(pos.east(), Direction.EAST))) >= 16) {
            return i;
        }
        return i;
    }

    public int getReceivedStrongGreenstonePower(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.down(), Direction.DOWN))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.up(), Direction.UP))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.north(), Direction.NORTH))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.south(), Direction.SOUTH))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.west(), Direction.WEST))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongGreenstonePower(pos.east(), Direction.EAST))) >= 16) {
            return i;
        }
        return i;
    }

    public int getReceivedStrongYellowstonePower(BlockPos pos) {
        int i = 0;
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.down(), Direction.DOWN))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.up(), Direction.UP))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.north(), Direction.NORTH))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.south(), Direction.SOUTH))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.west(), Direction.WEST))) >= 16) {
            return i;
        }
        if ((i = Math.max(i, ((WorldViewInterface) this).getStrongYellowstonePower(pos.east(), Direction.EAST))) >= 16) {
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
        if (this.getEmittedBluestonePower(pos.down(), Direction.DOWN) > 1) {
            return true;
        }
        if (this.getEmittedBluestonePower(pos.up(), Direction.UP) > 1) {
            return true;
        }
        if (this.getEmittedBluestonePower(pos.north(), Direction.NORTH) > 1) {
            return true;
        }
        if (this.getEmittedBluestonePower(pos.south(), Direction.SOUTH) > 1) {
            return true;
        }
        if (this.getEmittedBluestonePower(pos.west(), Direction.WEST) > 1) {
            return true;
        }
        return this.getEmittedBluestonePower(pos.east(), Direction.EAST) > 1;
    }

    @Override
    public boolean isReceivingGreenstonePower(BlockPos pos) { 
        if (this.getEmittedGreenstonePower(pos.down(), Direction.DOWN) > 1) {
            return true;
        }
        if (this.getEmittedGreenstonePower(pos.up(), Direction.UP) > 1) {
            return true;
        }
        if (this.getEmittedGreenstonePower(pos.north(), Direction.NORTH) > 1) {
            return true;
        }
        if (this.getEmittedGreenstonePower(pos.south(), Direction.SOUTH) > 1) {
            return true;
        }
        if (this.getEmittedGreenstonePower(pos.west(), Direction.WEST) > 1) {
            return true;
        }
        return this.getEmittedGreenstonePower(pos.east(), Direction.EAST) > 1;
    }

    @Override
    public boolean isReceivingYellowstonePower(BlockPos pos) { 
        if (this.getEmittedYellowstonePower(pos.down(), Direction.DOWN) > 1) {
            return true;
        }
        if (this.getEmittedYellowstonePower(pos.up(), Direction.UP) > 1) {
            return true;
        }
        if (this.getEmittedYellowstonePower(pos.north(), Direction.NORTH) > 1) {
            return true;
        }
        if (this.getEmittedYellowstonePower(pos.south(), Direction.SOUTH) > 1) {
            return true;
        }
        if (this.getEmittedYellowstonePower(pos.west(), Direction.WEST) > 1) {
            return true;
        }
        return this.getEmittedYellowstonePower(pos.east(), Direction.EAST) > 1;
    }

    @Override
    public int getReceivedBluestonePower(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getEmittedBluestonePower(pos.offset(direction), direction);
            if (j >= 16) {
                return 16;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    @Override
    public int getReceivedGreenstonePower(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getEmittedGreenstonePower(pos.offset(direction), direction);
            if (j >= 16) {
                return 16;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    @Override
    public int getReceivedYellowstonePower(BlockPos pos) {
        int i = 0;
        for (Direction direction : DIRECTIONS) {
            int j = this.getEmittedYellowstonePower(pos.offset(direction), direction);
            if (j >= 16) {
                return 16;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

}
