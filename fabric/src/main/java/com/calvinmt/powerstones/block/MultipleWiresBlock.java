package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.WorldInterface;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;
import com.google.common.collect.Sets;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MultipleWiresBlock extends PowerstoneWireBlockBase {

    public static final IntProperty POWER_B = PowerStones.POWER_B;
    public static final EnumProperty<PowerPair> POWER_PAIR = PowerStones.POWER_PAIR;
    
    public MultipleWiresBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(WIRE_CONNECTION_NORTH, WireConnection.NONE).with(WIRE_CONNECTION_EAST, WireConnection.NONE).with(WIRE_CONNECTION_SOUTH, WireConnection.NONE).with(WIRE_CONNECTION_WEST, WireConnection.NONE).with(POWER, Integer.valueOf(0)).with(POWER_B, Integer.valueOf(0)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        if (context.getStack().isOf(Items.REDSTONE) || context.getStack().isOf(PowerStones.BLUESTONE)) {
            this.setDefaultState(this.getDefaultState().with(POWER, 0).with(POWER_B, 0).with(POWER_PAIR, PowerPair.RED_BLUE));
        }
        if (context.getStack().isOf(PowerStones.GREENSTONE) || context.getStack().isOf(PowerStones.YELLOWSTONE)) {
            this.setDefaultState(this.getDefaultState().with(POWER, 0).with(POWER_B, 0).with(POWER_PAIR, PowerPair.GREEN_YELLOW));
        }
        return this.getPlacementState((BlockView) context.getWorld(), this.dotState.with(POWER, this.getDefaultState().get(POWER)).with(POWER_B, this.getDefaultState().get(POWER_B)).with(POWER_PAIR, this.getDefaultState().get(POWER_PAIR)), context.getBlockPos());
    }

    @Override
    protected BlockState getDefaultBlockStateWithPowerProperties(BlockState state) {
        return this.getDefaultState().with(POWER, state.get(POWER)).with(POWER_B, state.get(POWER_B)).with(POWER_PAIR, state.get(POWER_PAIR));
    }

    @Override
    protected BlockState getCrossStateWithPowerProperties(BlockState state) {
        return this.dotState.with(POWER, state.get(POWER)).with(POWER_B, state.get(POWER_B)).with(POWER_PAIR, state.get(POWER_PAIR));
    }

    @Override
    protected boolean isOtherConnectablePowerstone(BlockState state) {
        if (state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(PowerStones.BLUESTONE_WIRE) || state.isOf(PowerStones.GREENSTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            return true;
        }
        return false;
    }

    @Override
    protected void updatePowerStrength(World world, BlockPos pos, BlockState state) {
        int power = state.get(POWER);
        int power_b = state.get(POWER_B);
        int r = this.calculateTargetStrengthRed(world, pos);
        int b = this.calculateTargetStrengthBlue(world, pos);
        int g = this.calculateTargetStrengthGreen(world, pos);
        int y = this.calculateTargetStrengthYellow(world, pos);
        if ((state.get(POWER_PAIR) == PowerPair.RED_BLUE && state.get(POWER) != r)
            || (state.get(POWER_PAIR) == PowerPair.RED_BLUE && state.get(POWER_B) != b)
            || (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && state.get(POWER) != g)
            || (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && state.get(POWER_B) != y)) {
            if (world.getBlockState(pos) == state) {
                if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && state.get(POWER) != r)
                    power = r;
                if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && state.get(POWER_B) != b)
                    power_b = b;
                if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && state.get(POWER) != g)
                    power = g;
                if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && state.get(POWER_B) != y)
                    power_b = y;
                world.setBlockState(pos, (BlockState)state.with(POWER, power).with(POWER_B, power_b), 2);
            }
            HashSet<BlockPos> set = Sets.newHashSet();
            set.add(pos);
            for (Direction direction : Direction.values()) {
                set.add(pos.offset(direction));
            }
            for (BlockPos blockPos : set) {
                world.updateNeighbors(blockPos, this);
            }
        }
    }

    private int calculateTargetStrengthRed(World world, BlockPos pos) {
        wiresGivePower = false;
        ((RedstoneWireBlockInterface)Blocks.REDSTONE_WIRE).setShouldSignal(false);
        int i = world.getReceivedRedstonePower(pos);
        wiresGivePower = true;
        ((RedstoneWireBlockInterface)Blocks.REDSTONE_WIRE).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for(Direction direction : Type.HORIZONTAL) {
                BlockPos blockpos = pos.offset(direction);
                BlockState blockstate = world.getBlockState(blockpos);
                j = Math.max(j, this.getWireSignalRed(blockstate));
                BlockPos blockpos1 = pos.up();
                if (blockstate.isSolidBlock(world, blockpos) && !world.getBlockState(blockpos1).isSolidBlock(world, blockpos1)) {
                    j = Math.max(j, this.getWireSignalRed(world.getBlockState(blockpos.up())));
                } else if (!blockstate.isSolidBlock(world, blockpos)) {
                    j = Math.max(j, this.getWireSignalRed(world.getBlockState(blockpos.down())));
                }
            }
        }
        return Math.max(i, j - 1);
    }

    private int calculateTargetStrengthBlue(World world, BlockPos pos) {
        wiresGivePower = false;
        ((BluestoneWireBlock)PowerStones.BLUESTONE_WIRE).setShouldSignal(false);
        int i = ((WorldInterface) world).getReceivedBluestonePower(pos);
        wiresGivePower = true;
        ((BluestoneWireBlock)PowerStones.BLUESTONE_WIRE).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for (Direction direction : Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.getWireSignalBlue(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.getWireSignalBlue(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.getWireSignalBlue(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j - 1);
    }

    private int calculateTargetStrengthGreen(World world, BlockPos pos) {
        wiresGivePower = false;
        ((GreenstoneWireBlock)PowerStones.GREENSTONE_WIRE).setShouldSignal(false);
        int i = ((WorldInterface) world).getReceivedGreenstonePower(pos);
        wiresGivePower = true;
        ((GreenstoneWireBlock)PowerStones.GREENSTONE_WIRE).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for (Direction direction : Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.getWireSignalGreen(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.getWireSignalGreen(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.getWireSignalGreen(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j - 1);
    }

    private int calculateTargetStrengthYellow(World world, BlockPos pos) {
        wiresGivePower = false;
        ((YellowstoneWireBlock)PowerStones.YELLOWSTONE_WIRE).setShouldSignal(false);
        int i = ((WorldInterface) world).getReceivedYellowstonePower(pos);
        wiresGivePower = true;
        ((YellowstoneWireBlock)PowerStones.YELLOWSTONE_WIRE).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for (Direction direction : Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.getWireSignalYellow(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.getWireSignalYellow(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.getWireSignalYellow(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j - 1);
    }

    private int getWireSignalRed(BlockState state) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) {
            return state.get(POWER);
        }
        if (state.isOf(this) && state.get(POWER_PAIR) == PowerPair.RED_BLUE) {
            return state.get(POWER);
        }
        return 0;
    }

    private int getWireSignalBlue(BlockState state) {
        if (state.isOf(PowerStones.BLUESTONE_WIRE)) {
            return state.get(POWER);
        }
        if (state.isOf(this) && state.get(POWER_PAIR) == PowerPair.RED_BLUE) {
            return state.get(POWER_B);
        }
        return 0;
    }

    private int getWireSignalGreen(BlockState state) {
        if (state.isOf(PowerStones.GREENSTONE_WIRE)) {
            return state.get(POWER);
        }
        if (state.isOf(this) && state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            return state.get(POWER);
        }
        return 0;
    }

    private int getWireSignalYellow(BlockState state) {
        if (state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            return state.get(POWER);
        }
        if (state.isOf(this) && state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            return state.get(POWER_B);
        }
        return 0;
    }

    @Override
    public int getStrongRedstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return ! wiresGivePower ? 0 : blockState.getWeakRedstonePower(blockAccess, pos, side);
    }

    @Override
    public int getStrongBluestonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return ! wiresGivePower ? 0 : ((AbstractBlockStateInterface)blockState).getWeakBluestonePower(blockAccess, pos, side);
    }

    @Override
    public int getStrongGreenstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return ! wiresGivePower ? 0 : ((AbstractBlockStateInterface)blockState).getWeakGreenstonePower(blockAccess, pos, side);
    }

    @Override
    public int getStrongYellowstonePower(BlockState blockState, BlockView blockAccess, BlockPos pos, Direction side) {
        return ! wiresGivePower ? 0 : ((AbstractBlockStateInterface)blockState).getWeakYellowstonePower(blockAccess, pos, side);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (! wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        if (state.get(POWER_PAIR) != PowerPair.RED_BLUE) {
            return 0;
        }
        int i = state.get(POWER);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (! wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        if (state.get(POWER_PAIR) != PowerPair.RED_BLUE) {
            return 0;
        }
        int i = state.get(POWER_B);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (! wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        if (state.get(POWER_PAIR) != PowerPair.GREEN_YELLOW) {
            return 0;
        }
        int i = state.get(POWER);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (! wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        if (state.get(POWER_PAIR) != PowerPair.GREEN_YELLOW) {
            return 0;
        }
        int i = state.get(POWER_B);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    protected boolean shouldConnectToAbove(BlockView world, BlockPos posAbove, BlockState stateAbove, Direction direction) {
        BlockState originalState = world.getBlockState(posAbove.down().offset(direction.getOpposite()));
        return this.shouldConnectTo(originalState, world, posAbove, stateAbove, null);
    }

    @Override
    protected boolean shouldConnectToBelow(BlockView world, BlockPos posBelow, BlockState stateBelow, Direction direction) {
        BlockState originalState = world.getBlockState(posBelow.up().offset(direction.getOpposite()));
        return this.shouldConnectTo(originalState, world, posBelow, stateBelow, null);
    }
    
    @Override
    protected boolean shouldConnectTo(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        BlockState originalState = world.getBlockState(pos.offset(direction.getOpposite()));
        return this.shouldConnectTo(originalState, world, pos, state, direction);
    }

    protected boolean shouldConnectTo(BlockState multipleWiresState, BlockView world, BlockPos pos, BlockState state, Direction direction) {
        if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return multipleWiresState.get(POWER_PAIR) == state.get(POWER_PAIR);
        }
        else if (state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(PowerStones.BLUESTONE_WIRE)) {
            return multipleWiresState.get(POWER_PAIR) == PowerPair.RED_BLUE;
        }
        else if (state.isOf(PowerStones.GREENSTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            return multipleWiresState.get(POWER_PAIR) == PowerPair.GREEN_YELLOW;
        }
        else if (direction != null
         && (state.isOf(Blocks.REDSTONE_TORCH) || state.isOf(Blocks.REDSTONE_WALL_TORCH)
         || state.isOf(PowerStones.BLUESTONE_TORCH_BLOCK) || state.isOf(PowerStones.BLUESTONE_WALL_TORCH)
         || state.isOf(Blocks.REDSTONE_BLOCK) || state.isOf(PowerStones.BLUESTONE_BLOCK))) {
            return multipleWiresState.get(POWER_PAIR) == PowerPair.RED_BLUE;
        }
        else if (direction != null
         && (state.isOf(PowerStones.GREENSTONE_TORCH_BLOCK) || state.isOf(PowerStones.GREENSTONE_WALL_TORCH)
         || state.isOf(PowerStones.YELLOWSTONE_TORCH_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_WALL_TORCH)
         || state.isOf(PowerStones.GREENSTONE_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_BLOCK))) {
            return multipleWiresState.get(POWER_PAIR) == PowerPair.GREEN_YELLOW;
        }
        else {
            return this.connectsTo(state, direction);
        }
    }

    public static int getColorForTintIndex(BlockState state, int tintIndex) {
        if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && tintIndex == 0) {
			return PowerstoneWireBlock.getWireColorRed(state.get(POWER));
		}
		else if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && tintIndex == 1) {
			return PowerstoneWireBlock.getWireColorBlue(state.get(POWER_B));
		}
		else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && tintIndex == 2) {
			return PowerstoneWireBlock.getWireColorGreen(state.get(POWER));
		}
		else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && tintIndex == 3) {
			return PowerstoneWireBlock.getWireColorYellow(state.get(POWER_B));
		}
		else {
			return PowerstoneWireBlock.getWireColorWhite();
		}
    }

    @Override
    protected boolean hasPowerOn(BlockState state) {
        return state.get(POWER) > 0 && state.get(POWER_B) > 0;
    }

    @Override
    protected Vec3d getPowerstoneColor(BlockState state, Random random) {
        List<Vec3d[]> colorsList = new ArrayList<>();
        List<Integer> powerList = new ArrayList<>();
        int power = state.get(POWER);
        int powerB = state.get(POWER_B);
        if (state.get(POWER_PAIR) == PowerPair.RED_BLUE) {
            if (power > 0) {
                colorsList.add(PowerstoneWireBlock.RED_COLORS);
                powerList.add(power);
            }
            if (powerB > 0) {
                colorsList.add(PowerstoneWireBlock.BLUE_COLORS);
                powerList.add(powerB);
            }
        }
        if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            if (power > 0) {
                colorsList.add(PowerstoneWireBlock.GREEN_COLORS);
                powerList.add(power);
            }
            if (powerB > 0) {
                colorsList.add(PowerstoneWireBlock.YELLOW_COLORS);
                powerList.add(powerB);
            }
        }
        int i = random.nextInt(colorsList.size());
        return colorsList.get(i)[powerList.get(i)];
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWER_B, POWER_PAIR);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand pHand, BlockHitResult pHit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            if (isFullyConnected(state) || isNotConnected(state)) {
                BlockState blockstate = isFullyConnected(state) ? this.getDefaultState().with(POWER, state.get(POWER)).with(POWER_B, state.get(POWER_B)).with(POWER_PAIR, state.get(POWER_PAIR)) : this.dotState.with(POWER, state.get(POWER)).with(POWER_B, state.get(POWER_B)).with(POWER_PAIR, state.get(POWER_PAIR));
                blockstate = blockstate.with(POWER, state.get(POWER)).with(POWER_B, state.get(POWER_B)).with(POWER_PAIR, state.get(POWER_PAIR));
                blockstate = this.getPlacementState(world, blockstate, pos);
                if (blockstate != state) {
                    world.setBlockState(pos, blockstate, 3);
                    this.updateForNewState(world, pos, state, blockstate);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        }
    }

    public static boolean canBreakFromHeldItem(BlockState state, ItemStack heldItemStack) {
        if ((state.isOf(PowerStones.MULTIPLE_WIRES) && state.get(POWER_PAIR) == PowerPair.RED_BLUE && (heldItemStack.isOf(PowerStones.GREENSTONE) || heldItemStack.isOf(PowerStones.YELLOWSTONE)))
         || (state.isOf(PowerStones.MULTIPLE_WIRES) && state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && (heldItemStack.isOf(Items.REDSTONE) || heldItemStack.isOf(PowerStones.BLUESTONE)))) {
            return false;
        }
        return true;
    }

    public void breakSingle(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (canBreakFromHeldItem(state, player.getMainHandStack())) {
            if (state.get(POWER_PAIR) == PowerPair.RED_BLUE) {
                if (player.getMainHandStack().isOf(Items.REDSTONE)) {
                    state = PowerStones.BLUESTONE_WIRE.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(POWER, state.get(POWER_B));
                    world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, world, pos);
                }
                else if (player.getMainHandStack().isOf(PowerStones.BLUESTONE)) {
                    state = Blocks.REDSTONE_WIRE.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(POWER, state.get(POWER));
                    world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    ((RedstoneWireBlockInterface)state.getBlock()).updateAll(state, world, pos);
                }
            }
            else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
                if (player.getMainHandStack().isOf(PowerStones.GREENSTONE)) {
                    state = PowerStones.YELLOWSTONE_WIRE.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(POWER, state.get(POWER_B));
                    world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, world, pos);
                }
                else if (player.getMainHandStack().isOf(PowerStones.YELLOWSTONE)) {
                    state = PowerStones.GREENSTONE_WIRE.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(POWER, state.get(POWER));
                    world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, world, pos);
                }
            }
        }
    }

}

