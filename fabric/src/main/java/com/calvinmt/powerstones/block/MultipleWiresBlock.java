package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.PowerChannel;
import com.calvinmt.powerstones.WorldInterface;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;
import com.google.common.collect.Sets;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MultipleWiresBlock extends PowerstoneWireBlockBase implements BlockEntityProvider {

    public static final EnumProperty<PowerPair> POWER_PAIR = PowerStones.POWER_PAIR;
    
    public MultipleWiresBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(WIRE_CONNECTION_NORTH, WireConnection.NONE).with(WIRE_CONNECTION_EAST, WireConnection.NONE).with(WIRE_CONNECTION_SOUTH, WireConnection.NONE).with(WIRE_CONNECTION_WEST, WireConnection.NONE));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return (VoxelShape)SHAPES.get(state);
    }

    private BlockState getChannelConnectionState(BlockView world, BlockPos pos, BlockState multipleWiresState, PowerChannel channel) {
        PowerPair pair = multipleWiresState.get(POWER_PAIR);

        if (pair == PowerPair.RED_BLUE) {
            if (channel == PowerChannel.A) {
                return ((RedstoneWireBlockInterface) Blocks.REDSTONE_WIRE).getConnectionState(world, pos);
            }

            return ((PowerstoneWireBlock) PowerStones.BLUESTONE_WIRE).getConnectionState(world, pos);
        }

        if (pair == PowerPair.GREEN_YELLOW) {
            if (channel == PowerChannel.A) {
                return ((PowerstoneWireBlock) PowerStones.GREENSTONE_WIRE).getConnectionState(world, pos);
            }

            return ((PowerstoneWireBlock) PowerStones.YELLOWSTONE_WIRE).getConnectionState(world, pos);
        }

        throw new IllegalStateException("Unsupported power pair: " + pair);
    }

    private void refreshChannelConnections(World world, BlockPos pos) {
        if (world.isClient) {
            return;
        }

        BlockState multipleWiresState = world.getBlockState(pos);

        // Prevents potential issues if the block state has changed to a different type of block.
        if (!multipleWiresState.isOf(this)) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof MultipleWiresBlockEntity)) {
            return;
        }

        MultipleWiresBlockEntity multipleWiresBlockEntity = (MultipleWiresBlockEntity) blockEntity;

        BlockState channelAState = this.getChannelConnectionState(world, pos, multipleWiresState, PowerChannel.A);
        BlockState channelBState = this.getChannelConnectionState(world, pos, multipleWiresState, PowerChannel.B);

        multipleWiresBlockEntity.setConnectionStates(channelAState, channelBState);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MultipleWiresBlockEntity(pos, state);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        if (context.getStack().isOf(Items.REDSTONE) || context.getStack().isOf(PowerStones.BLUESTONE)) {
            this.setDefaultState(this.getDefaultState().with(POWER_PAIR, PowerPair.RED_BLUE));
        }
        if (context.getStack().isOf(PowerStones.GREENSTONE) || context.getStack().isOf(PowerStones.YELLOWSTONE)) {
            this.setDefaultState(this.getDefaultState().with(POWER_PAIR, PowerPair.GREEN_YELLOW));
        }
        return this.getPlacementState((BlockView) context.getWorld(), this.dotState.with(POWER_PAIR, this.getDefaultState().get(POWER_PAIR)), context.getBlockPos());
    }

    @Override
    protected BlockState getDefaultBlockStateWithPowerProperties(BlockState state) {
        return this.getDefaultState().with(POWER_PAIR, state.get(POWER_PAIR));
    }

    @Override
    protected BlockState getCrossStateWithPowerProperties(BlockState state) {
        return this.dotState.with(POWER_PAIR, state.get(POWER_PAIR));
    }

    @Override
    protected boolean isOtherConnectablePowerstone(BlockState state) {
        if (state.isOf(Blocks.REDSTONE_WIRE) || state.isOf(PowerStones.BLUESTONE_WIRE) || state.isOf(PowerStones.GREENSTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            return true;
        }
        return false;
    }

    public static void setPowerA(BlockView world, BlockPos pos, int power) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MultipleWiresBlockEntity multipleWiresBlockEntity) {
            multipleWiresBlockEntity.setPowerA(power);
        }
    }

    public static void setPowerB(BlockView world, BlockPos pos, int power) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MultipleWiresBlockEntity multipleWiresBlockEntity) {
            multipleWiresBlockEntity.setPowerB(power);
        }
    }

    public static int getPowerA(BlockView world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MultipleWiresBlockEntity multipleWiresBlockEntity) {
            return multipleWiresBlockEntity.getPowerA();
        }
        return 0;
    }

    public static int getPowerB(BlockView world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof MultipleWiresBlockEntity multipleWiresBlockEntity) {
            return multipleWiresBlockEntity.getPowerB();
        }
        return 0;
    }

    @Override
    protected void updatePowerStrength(World world, BlockPos pos, BlockState state) {
        // Refresh the connection states for each channel before calculating the power strengths.
        // Ensures that the power strengths are calculated based on the most up-to-date connection information.
        this.refreshChannelConnections(world, pos);

        int powerA = getPowerA(world, pos);
        int powerB = getPowerB(world, pos);
        int r = this.calculateTargetStrengthRed(world, pos);
        int b = this.calculateTargetStrengthBlue(world, pos);
        int g = this.calculateTargetStrengthGreen(world, pos);
        int y = this.calculateTargetStrengthYellow(world, pos);
        if ((state.get(POWER_PAIR) == PowerPair.RED_BLUE && powerA != r)
            || (state.get(POWER_PAIR) == PowerPair.RED_BLUE && powerB != b)
            || (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && powerA != g)
            || (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && powerB != y)) {
            if (world.getBlockState(pos) == state) {
                if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && powerA != r)
                    powerA = r;
                if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && powerB != b)
                    powerB = b;
                if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && powerA != g)
                    powerA = g;
                if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && powerB != y)
                    powerB = y;
                setPowerA(world, pos, powerA);
                setPowerB(world, pos, powerB);
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
                j = Math.max(j, this.getWireSignalRed(blockstate, world, blockpos));
                BlockPos blockpos1 = pos.up();
                if (blockstate.isSolidBlock(world, blockpos) && !world.getBlockState(blockpos1).isSolidBlock(world, blockpos1)) {
                    BlockPos blockPosUp = blockpos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getWireSignalRed(blockStateUp, world, blockPosUp));
                } else if (!blockstate.isSolidBlock(world, blockpos)) {
                    BlockPos blockPosDown = blockpos.down();
                    BlockState blockStateDown = world.getBlockState(blockPosDown);
                    j = Math.max(j, this.getWireSignalRed(blockStateDown, world, blockPosDown));
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
                j = Math.max(j, this.getWireSignalBlue(blockState, world, blockPos));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    BlockPos blockPosUp = blockPos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getWireSignalBlue(blockStateUp, world, blockPosUp));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                BlockPos blockPosDown = blockPos.down();
                BlockState blockStateDown = world.getBlockState(blockPosDown);
                j = Math.max(j, this.getWireSignalBlue(blockStateDown, world, blockPosDown));
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
                j = Math.max(j, this.getWireSignalGreen(blockState, world, blockPos));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    BlockPos blockPosUp = blockPos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getWireSignalGreen(blockStateUp, world, blockPosUp));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                BlockPos blockPosDown = blockPos.down();
                BlockState blockStateDown = world.getBlockState(blockPosDown);
                j = Math.max(j, this.getWireSignalGreen(blockStateDown, world, blockPosDown));
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
                j = Math.max(j, this.getWireSignalYellow(blockState, world, blockPos));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    BlockPos blockPosUp = blockPos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getWireSignalYellow(blockStateUp, world, blockPosUp));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                BlockPos blockPosDown = blockPos.down();
                BlockState blockStateDown = world.getBlockState(blockPosDown);
                j = Math.max(j, this.getWireSignalYellow(blockStateDown, world, blockPosDown));
            }
        }
        return Math.max(i, j - 1);
    }

    private int getWireSignalRed(BlockState state, World world, BlockPos pos) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) {
            return state.get(RedstoneWireBlock.POWER);
        }
        if (state.isOf(this) && state.get(POWER_PAIR) == PowerPair.RED_BLUE) {
            return getPowerA(world, pos);
        }
        return 0;
    }

    private int getWireSignalBlue(BlockState state, World world, BlockPos pos) {
        if (state.isOf(PowerStones.BLUESTONE_WIRE)) {
            return state.get(PowerstoneWireBlock.POWER);
        }
        if (state.isOf(this) && state.get(POWER_PAIR) == PowerPair.RED_BLUE) {
            return getPowerB(world, pos);
        }
        return 0;
    }

    private int getWireSignalGreen(BlockState state, World world, BlockPos pos) {
        if (state.isOf(PowerStones.GREENSTONE_WIRE)) {
            return state.get(PowerstoneWireBlock.POWER);
        }
        if (state.isOf(this) && state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            return getPowerA(world, pos);
        }
        return 0;
    }

    private int getWireSignalYellow(BlockState state, World world, BlockPos pos) {
        if (state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            return state.get(PowerstoneWireBlock.POWER);
        }
        if (state.isOf(this) && state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            return getPowerB(world, pos);
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
        int i = getPowerA(world, pos);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP) {
            return i;
        }
        BlockState stateChannelA = this.getChannelConnectionState(world, pos, state, PowerChannel.A);
        WireConnection connectionA = (WireConnection) stateChannelA.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()));
        if (connectionA.isConnected()) {
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
        int i = getPowerB(world, pos);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP) {
            return i;
        }
        BlockState stateChannelB = this.getChannelConnectionState(world, pos, state, PowerChannel.B);
        WireConnection connectionB = (WireConnection) stateChannelB.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()));
        if (connectionB.isConnected()) {
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
        int i = getPowerA(world, pos);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP) {
            return i;
        }
        BlockState stateChannelA = this.getChannelConnectionState(world, pos, state, PowerChannel.A);
        WireConnection connectionA = (WireConnection) stateChannelA.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()));
        if (connectionA.isConnected()) {
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
        int i = getPowerB(world, pos);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP) {
            return i;
        }
        BlockState stateChannelB = this.getChannelConnectionState(world, pos, state, PowerChannel.B);
        WireConnection connectionB = (WireConnection) stateChannelB.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()));
        if (connectionB.isConnected()) {
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

    public static int getColorForTintIndex(BlockState state, BlockView world, BlockPos pos, int tintIndex) {
        if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && tintIndex == 0) {
			return PowerstoneWireBlock.getWireColorRed(getPowerA(world, pos));
		}
		else if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && tintIndex == 1) {
			return PowerstoneWireBlock.getWireColorBlue(getPowerB(world, pos));
		}
		else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && tintIndex == 2) {
			return PowerstoneWireBlock.getWireColorGreen(getPowerA(world, pos));
		}
		else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && tintIndex == 3) {
			return PowerstoneWireBlock.getWireColorYellow(getPowerB(world, pos));
		}
		else {
			return PowerstoneWireBlock.getWireColorWhite();
		}
    }

    @Override
    protected boolean hasPowerOn(World world, BlockPos pos) {
        return getPowerA(world, pos) > 0 || getPowerB(world, pos) > 0;
    }

    @Override
    protected Vec3d getPowerstoneColor(BlockState state, World world, BlockPos pos, Random random) {
        List<Vec3d[]> colorsList = new ArrayList<>();
        List<Integer> powerList = new ArrayList<>();
        int powerA = getPowerA(world, pos);
        int powerB = getPowerB(world, pos);
        if (state.get(POWER_PAIR) == PowerPair.RED_BLUE) {
            if (powerA > 0) {
                colorsList.add(PowerstoneWireBlock.RED_COLORS);
                powerList.add(powerA);
            }
            if (powerB > 0) {
                colorsList.add(PowerstoneWireBlock.BLUE_COLORS);
                powerList.add(powerB);
            }
        }
        if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            if (powerA > 0) {
                colorsList.add(PowerstoneWireBlock.GREEN_COLORS);
                powerList.add(powerA);
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
        builder.add(POWER_PAIR);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand pHand, BlockHitResult pHit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            if (isFullyConnected(state) || isNotConnected(state)) {
                BlockState blockstate = isFullyConnected(state) ? this.getDefaultState() : this.dotState;
                blockstate = blockstate.with(MultipleWiresBlock.POWER_PAIR, state.get(POWER_PAIR));
                blockstate = this.getPlacementState(world, blockstate, pos);
                setPowerA(world, pos, getPowerA(world, pos));
                setPowerB(world, pos, getPowerB(world, pos));
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
                    state = PowerStones.BLUESTONE_WIRE.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(RedstoneWireBlock.POWER, getPowerB(world, pos));
                    world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, world, pos);
                }
                else if (player.getMainHandStack().isOf(PowerStones.BLUESTONE)) {
                    state = Blocks.REDSTONE_WIRE.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(PowerstoneWireBlock.POWER, getPowerA(world, pos));
                    world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    ((RedstoneWireBlockInterface)state.getBlock()).updateAll(state, world, pos);
                }
            }
            else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
                if (player.getMainHandStack().isOf(PowerStones.GREENSTONE)) {
                    state = PowerStones.YELLOWSTONE_WIRE.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(PowerstoneWireBlock.POWER, getPowerB(world, pos));
                    world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, world, pos);
                }
                else if (player.getMainHandStack().isOf(PowerStones.YELLOWSTONE)) {
                    state = PowerStones.GREENSTONE_WIRE.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(PowerstoneWireBlock.POWER, getPowerA(world, pos));
                    world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, world, pos);
                }
            }
        }
    }

}

