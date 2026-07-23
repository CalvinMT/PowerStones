package com.calvinmt.powerstones.block;

import java.util.List;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.PowerColour;
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

public class GreenstoneWireBlock extends PowerstoneWireBlock {

    public GreenstoneWireBlock(AbstractBlock.Settings settings) {
        super(settings, PowerColour.GREEN.getColours());
    }

    @Override
    protected List<PowerPair> getPowerPairs() {
        return PowerPair.getPairsFor(PowerColour.GREEN);
    }

    @Override
    protected int getReceivedPower(World world, BlockPos pos) {
        return ((WorldInterface) world).getReceivedGreenstonePower(pos);
    }

    @Override
    protected int getPower(BlockState state, World world, BlockPos pos) {
        if (state.isOf(this)) {
            return state.get(POWER);
        }

        return MultipleWiresBlock.getPowerForColour(state, world, pos, PowerColour.GREEN);
    }

    @Override
    public int getStrongGreenstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        if (! wiresGivePower) {
            return 0;
        }
        if (blockState.isOf(this)
         || (blockState.isOf(PowerStones.MULTIPLE_WIRES) && blockState.get(PowerStones.POWER_PAIR).hasGreen())) {
            return ((AbstractBlockStateInterface)blockState).getWeakGreenstonePower(blockAccess, pos, side);
        }
        return 0;
    }

    @Override
    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (! wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        int i = 0;
        if (state.isOf(this)) {
            i = state.get(POWER);
        }
        else if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            i = MultipleWiresBlock.getPowerForColour(state, world, pos, PowerColour.GREEN);
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
        return state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(PowerStones.BLUESTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)
         || state.isOf(Blocks.REDSTONE_BLOCK) || state.isOf(PowerStones.BLUESTONE_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_BLOCK)
         || state.isOf(Blocks.REDSTONE_TORCH) || state.isOf(Blocks.REDSTONE_WALL_TORCH)
         || state.isOf(PowerStones.BLUESTONE_TORCH_BLOCK) || state.isOf(PowerStones.BLUESTONE_WALL_TORCH)
         || state.isOf(PowerStones.YELLOWSTONE_TORCH_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_WALL_TORCH);
    }

}
