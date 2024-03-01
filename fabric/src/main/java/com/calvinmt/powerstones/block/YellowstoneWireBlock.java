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

public class YellowstoneWireBlock extends PowerstoneWireBlock {

    public YellowstoneWireBlock(AbstractBlock.Settings settings) {
        super(settings, PowerstoneWireBlock.YELLOW_COLORS);
    }

    @Override
    protected PowerPair getPowerPair() {
        return PowerPair.GREEN_YELLOW;
    }

    @Override
    protected int getReceivedPower(World world, BlockPos pos) {
        return ((WorldInterface) world).getReceivedYellowstonePower(pos);
    }

    @Override
    protected int getPower(BlockState state) {
        if (state.isOf(this)) {
            return state.get(POWER);
        }
        if (state.isOf(PowerStones.MULTIPLE_WIRES) && state.get(PowerStones.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            return state.get(PowerStones.POWER_B);
        }
        return 0;
    }

    @Override
    public int getStrongYellowstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        if (! wiresGivePower) {
            return 0;
        }
        if (blockState.isOf(this)
         || (blockState.isOf(PowerStones.MULTIPLE_WIRES) && blockState.get(PowerStones.POWER_PAIR) == PowerPair.GREEN_YELLOW)) {
            return ((AbstractBlockStateInterface)blockState).getWeakYellowstonePower(blockAccess, pos, side);
        }
        return 0;
    }

    @Override
    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (! wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        int i = 0;
        if (state.isOf(this)) {
            i = state.get(POWER);
        }
        else if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            if (state.get(PowerStones.POWER_PAIR) != PowerPair.GREEN_YELLOW) {
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
        return state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(PowerStones.BLUESTONE_WIRE) || state.isOf(PowerStones.GREENSTONE_WIRE)
         || state.isOf(Blocks.REDSTONE_BLOCK) || state.isOf(PowerStones.BLUESTONE_BLOCK) || state.isOf(PowerStones.GREENSTONE_BLOCK)
         || state.isOf(Blocks.REDSTONE_TORCH) || state.isOf(Blocks.REDSTONE_WALL_TORCH)
         || state.isOf(PowerStones.BLUESTONE_TORCH_BLOCK) || state.isOf(PowerStones.BLUESTONE_WALL_TORCH)
         || state.isOf(PowerStones.GREENSTONE_TORCH_BLOCK) || state.isOf(PowerStones.GREENSTONE_WALL_TORCH);
    }

}
