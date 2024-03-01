package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.BlockStateBaseInterface;
import com.calvinmt.powerstones.LevelInterface;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;

public class GreenstoneWireBlock extends PowerstoneWireBlock {

    public GreenstoneWireBlock(Properties properties) {
        super(properties, PowerstoneWireBlock.GREEN_COLORS);
    }

    @Override
    protected PowerPair getPowerPair() {
        return PowerPair.GREEN_YELLOW;
    }

    @Override
    protected int getBestNeighborSignal(Level level, BlockPos pos) {
        return ((LevelInterface) level).getBestNeighborSignalGreen(pos);
    }

    @Override
    protected int getWireSignal(BlockState state) {
        if (state.is(this)) {
            return state.getValue(POWER);
        }
        if (state.is(PowerStones.MULTIPLE_WIRES.get()) && state.getValue(PowerStones.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            return state.getValue(POWER);
        }
        return 0;
    }

    @Override
    public int getDirectSignalGreen(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        if (! shouldSignal) {
            return 0;
        }
        if (blockState.is(this)
         || (blockState.is(PowerStones.MULTIPLE_WIRES.get()) && blockState.getValue(PowerStones.POWER_PAIR) == PowerPair.GREEN_YELLOW)) {
            return ((BlockStateBaseInterface)blockState).getSignalGreen(blockAccess, pos, side);
        }
        return 0;
    }

    @Override
    public int getSignalGreen(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (! shouldSignal || direction == Direction.DOWN) {
            return 0;
        }
        int i = 0;
        if (state.is(this)) {
            i = state.getValue(POWER);
        }
        else if (state.is(PowerStones.MULTIPLE_WIRES.get())) {
            if (state.getValue(PowerStones.POWER_PAIR) != PowerPair.GREEN_YELLOW) {
                return 0;
            }
            i = state.getValue(POWER);
        }
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((RedstoneSide)this.getConnectionState(level, state, pos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    protected boolean shouldNotConnectTo(BlockState state) {
        return state.is(Blocks.REDSTONE_WIRE) || state.is(PowerStones.BLUESTONE_WIRE.get()) || state.is(PowerStones.YELLOWSTONE_WIRE.get())
         || state.is(Blocks.REDSTONE_BLOCK) || state.is(PowerStones.BLUESTONE_BLOCK.get()) || state.is(PowerStones.YELLOWSTONE_BLOCK.get())
         || state.is(Blocks.REDSTONE_TORCH) || state.is(Blocks.REDSTONE_WALL_TORCH)
         || state.is(PowerStones.BLUESTONE_TORCH_BLOCK.get()) || state.is(PowerStones.BLUESTONE_WALL_TORCH.get())
         || state.is(PowerStones.YELLOWSTONE_TORCH_BLOCK.get()) || state.is(PowerStones.YELLOWSTONE_WALL_TORCH.get());
    }

}
