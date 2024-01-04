package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.WorldInterface;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BluestoneWireBlock extends PowerstoneWireBlock {

    public BluestoneWireBlock(AbstractBlock.Settings settings) {
        super(settings, PowerstoneWireBlock.BLUE_COLORS);
    }

    @Override
    protected PowerPair getPowerPair() {
        return PowerPair.RED_BLUE;
    }

    @Override
    protected int getReceivedPower(World world, BlockPos pos) {
        return ((WorldInterface) world).getReceivedBluestonePower(pos);
    }

    @Override
    protected int getPower(BlockState state) {
        if (state.isOf(this)) {
            return state.get(POWER);
        }
        if (state.isOf(PowerStones.MULTIPLE_WIRES) && state.get(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE) {
            return state.get(PowerStones.POWER_B);
        }
        return 0;
    }

    public int getStrongBluestonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        if (! wiresGivePower) {
            return 0;
        }
        if (blockState.isOf(this)
         || (blockState.isOf(PowerStones.MULTIPLE_WIRES) && blockState.get(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE)) {
            return ((AbstractBlockStateInterface)blockState).getWeakBluestonePower(blockAccess, pos, side);
        }
        return 0;
    }

    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (! wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        int i = 0;
        if (state.isOf(this)) {
            i = state.get(POWER);
        }
        else if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            if (state.get(PowerStones.POWER_PAIR) != PowerPair.RED_BLUE) {
                return 0;
            }
            i = state.get(PowerStones.POWER_B);
        }
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    protected boolean shouldNotConnectTo(BlockState state) {
        return state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(PowerStones.GREENSTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)
         || state.isOf(Blocks.REDSTONE_BLOCK) || state.isOf(PowerStones.GREENSTONE_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_BLOCK)
         || state.isOf(Blocks.REDSTONE_TORCH) || state.isOf(Blocks.REDSTONE_WALL_TORCH)
         || state.isOf(PowerStones.GREENSTONE_TORCH_BLOCK) || state.isOf(PowerStones.GREENSTONE_WALL_TORCH)
         || state.isOf(PowerStones.YELLOWSTONE_TORCH_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_WALL_TORCH);
    }

}
