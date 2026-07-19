package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.PowerChannel;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;
import com.calvinmt.powerstones.WorldInterface;
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
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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
                if (this.canRunOnTop(world, blockpos, blockstate) && !world.getBlockState(blockpos1).isSolidBlock(world, blockpos1)) {
                    BlockPos blockPosUp = blockpos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getWireSignalRed(blockStateUp, world, blockPosUp));
                } else if (!this.canRunOnTop(world, blockpos, blockstate)) {
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
                if (this.canRunOnTop(world, blockPos, blockState) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    BlockPos blockPosUp = blockPos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getWireSignalBlue(blockStateUp, world, blockPosUp));
                    continue;
                }
                if (this.canRunOnTop(world, blockPos, blockState)) continue;
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
                if (this.canRunOnTop(world, blockPos, blockState) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    BlockPos blockPosUp = blockPos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getWireSignalGreen(blockStateUp, world, blockPosUp));
                    continue;
                }
                if (this.canRunOnTop(world, blockPos, blockState)) continue;
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
                if (this.canRunOnTop(world, blockPos, blockState) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    BlockPos blockPosUp = blockPos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getWireSignalYellow(blockStateUp, world, blockPosUp));
                    continue;
                }
                if (this.canRunOnTop(world, blockPos, blockState)) continue;
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
        int powerA = getPowerA(world, pos);
        int powerB = getPowerB(world, pos);

        // The blockstate packet can arrive before the completed block entity packet.
        // Use the same prediction as the custom model so powered wires
        // do not briefly use power level zero during that interval.
        MultipleWiresBlockEntity.RenderData predictedData = MultipleWiresBlockEntity.getPredictedRenderData(pos);

        if (predictedData != null && predictedData.powerPair() == state.get(POWER_PAIR)) {
            powerA = predictedData.powerA();
            powerB = predictedData.powerB();
        }

        if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && tintIndex == 0) {
			return PowerstoneWireBlock.getWireColorRed(powerA);
		}
		else if (state.get(POWER_PAIR) == PowerPair.RED_BLUE && tintIndex == 1) {
			return PowerstoneWireBlock.getWireColorBlue(powerB);
		}
		else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && tintIndex == 2) {
			return PowerstoneWireBlock.getWireColorGreen(powerA);
		}
		else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW && tintIndex == 3) {
			return PowerstoneWireBlock.getWireColorYellow(powerB);
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

    public boolean convertFromSingleWire(World world, BlockPos pos, BlockState singleWireState, PlayerEntity player, Hand hand) {
        ItemStack heldItemStack = player.getStackInHand(hand);

        PowerPair powerPair;
        int powerA;
        int powerB;

        BlockState channelAState;
        BlockState channelBState;

        // Calculate both channels while the original single wire is still in the world.
        // The original channel therefore retains its exact current shape.
        if (singleWireState.isOf(Blocks.REDSTONE_WIRE) && heldItemStack.isOf(PowerStones.BLUESTONE)) {
            powerPair = PowerPair.RED_BLUE;
            powerA = singleWireState.get(RedstoneWireBlock.POWER);
            powerB = 0;
            channelAState = singleWireState;
            channelBState = ((PowerstoneWireBlock) PowerStones.BLUESTONE_WIRE).getConnectionState(world, pos);
        }
        else if (singleWireState.isOf(PowerStones.BLUESTONE_WIRE) && heldItemStack.isOf(Items.REDSTONE)) {
            powerPair = PowerPair.RED_BLUE;
            powerA = 0;
            powerB = singleWireState.get(PowerstoneWireBlock.POWER);
            channelAState = ((RedstoneWireBlockInterface) Blocks.REDSTONE_WIRE).getConnectionState(world, pos);
            channelBState = singleWireState;
        }
        else if (singleWireState.isOf(PowerStones.GREENSTONE_WIRE) && heldItemStack.isOf(PowerStones.YELLOWSTONE)) {
            powerPair = PowerPair.GREEN_YELLOW;
            powerA = singleWireState.get(PowerstoneWireBlock.POWER);
            powerB = 0;
            channelAState = singleWireState;
            channelBState = ((PowerstoneWireBlock) PowerStones.YELLOWSTONE_WIRE).getConnectionState(world, pos);
        }
        else if (singleWireState.isOf(PowerStones.YELLOWSTONE_WIRE) && heldItemStack.isOf(PowerStones.GREENSTONE)) {
            powerPair = PowerPair.GREEN_YELLOW;
            powerA = 0;
            powerB = singleWireState.get(PowerstoneWireBlock.POWER);
            channelAState = ((PowerstoneWireBlock) PowerStones.GREENSTONE_WIRE).getConnectionState(world, pos);
            channelBState = singleWireState;
        }
        else {
            return false;
        }

        MultipleWiresBlockEntity.RenderData initialRenderData = MultipleWiresBlockEntity.createRenderData(powerPair, powerA, powerB, channelAState, channelBState);

        // 'onUse' runs on both the client and server.
        // On the client, only store the expected render data.
        // Do not replace the block or notify neighbours on the client.
        if (world.isClient) {
            MultipleWiresBlockEntity.setPredictedRenderData(pos, initialRenderData);

            return true;
        }

        // Preserve the current connection state of the original single wire
        // when initially creating the MultipleWiresBlock.
        BlockState stateMultipleWires = this.getDefaultState()
            .with(WIRE_CONNECTION_NORTH, singleWireState.get(WIRE_CONNECTION_NORTH))
            .with(WIRE_CONNECTION_EAST, singleWireState.get(WIRE_CONNECTION_EAST))
            .with(WIRE_CONNECTION_SOUTH, singleWireState.get(WIRE_CONNECTION_SOUTH))
            .with(WIRE_CONNECTION_WEST, singleWireState.get(WIRE_CONNECTION_WEST))
            .with(POWER_PAIR, powerPair);

        // Mark this conversion before replacing the block.
        // The new block entity is constructed synchronously inside 'setBlockState',
        // allowing it to suppress incomplete update packets during its callbacks.
        MultipleWiresBlockEntity.beginConversionInitialisation(pos, initialRenderData);

        boolean placed;

        try {
            // Install the new block silently on the server.
            //
            // NOTIFY_ALL would notify neighbouring wires before the new block
            // entity has received powerA and powerB. Those neighbours would
            // therefore observe a real zero-power MultipleWiresBlock and
            // briefly recalculate the network down to zero.
            //
            // FORCE_STATE also prevents onBlockAdded from running during this
            // incomplete state. The normal wire update process is invoked
            // explicitly below, after setInitialData has completed.
            placed = world.setBlockState(pos, stateMultipleWires, Block.FORCE_STATE);
        }
        finally {
            MultipleWiresBlockEntity.endConversionInitialisation(pos);
        }

        if (!placed) {
            return false;
        }

        // Get the block entity for the new MultipleWiresBlock.
        // It should be present because the block was just placed.
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof MultipleWiresBlockEntity)) {
            // This should not normally happen, but restore the original block
            // rather than leaving an invalid MultipleWiresBlock.
            world.setBlockState(pos, singleWireState, Block.NOTIFY_ALL);

            return false;
        }

        MultipleWiresBlockEntity multipleWiresBlockEntity = (MultipleWiresBlockEntity) blockEntity;

        // Initialise both powers and both independent channel connection states together.
        multipleWiresBlockEntity.setInitialData(powerA, powerB, channelAState, channelBState);

        // Keep the normal wire update process.
        // This updates direct wires, offset wires, power strengths
        // and the final shared connection state.
        this.updateAll(world.getBlockState(pos), world, pos);

        // Permit update packets only after powers and both channel states have been fully established.
        // This makes the first block entity packet received by the client a complete snapshot.
        multipleWiresBlockEntity.finishConversionInitialisation();

        // Send the final block state and completed block entity to the client.
        // This also replaces the temporary predicted render data.
        BlockState finalState = world.getBlockState(pos);
        world.updateListeners(pos, singleWireState, finalState, Block.NOTIFY_LISTENERS);

        BlockSoundGroup soundGroup = singleWireState.getSoundGroup();
        world.playSound(null, pos, soundGroup.getPlaceSound(), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0F) / 2.0F, soundGroup.getPitch() * 0.8F);

        // Decrement the held item stack if the player is not in creative mode.
        if (!player.getAbilities().creativeMode) {
            heldItemStack.decrement(1);
        }

        return true;
    }

    public static boolean shouldBreakBlock(BlockState state, ItemStack heldItemStack) {
        if (!state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return true;
        }

        PowerPair powerPair = state.get(POWER_PAIR);

        return !(powerPair == PowerPair.RED_BLUE && (heldItemStack.isOf(PowerStones.GREENSTONE) || heldItemStack.isOf(PowerStones.YELLOWSTONE)))
            && !(powerPair == PowerPair.GREEN_YELLOW && (heldItemStack.isOf(Items.REDSTONE) || heldItemStack.isOf(PowerStones.BLUESTONE)));
    }

    public static boolean shouldBreakIntoSingle(BlockState state, ItemStack heldItemStack) {
        if (!state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return false;
        }

        PowerPair powerPair = state.get(POWER_PAIR);

        return powerPair == PowerPair.RED_BLUE && (heldItemStack.isOf(Items.REDSTONE) || heldItemStack.isOf(PowerStones.BLUESTONE))
            || powerPair == PowerPair.GREEN_YELLOW && (heldItemStack.isOf(PowerStones.GREENSTONE) || heldItemStack.isOf(PowerStones.YELLOWSTONE));
    }

    public void breakSingle(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack heldItemStack = player.getMainHandStack();

        if (!shouldBreakIntoSingle(state, heldItemStack)) {
            return;
        }

        // Get the current power levels for both channels and the connection states for each channel
        // while the block is still in the world.
        int powerA = getPowerA(world, pos);
        int powerB = getPowerB(world, pos);
        BlockState stateChannelA = this.getChannelConnectionState(world, pos, state, PowerChannel.A);
        BlockState stateChannelB = this.getChannelConnectionState(world, pos, state, PowerChannel.B);

        BlockState remainingState = state;

        if (state.get(POWER_PAIR) == PowerPair.RED_BLUE) {
            if (heldItemStack.isOf(Items.REDSTONE)) {
                remainingState = stateChannelB.with(PowerstoneWireBlock.POWER, powerB);
            }
            else {
                remainingState = stateChannelA.with(PowerstoneWireBlock.POWER, powerA);
            }
        }
        else if (state.get(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            if (heldItemStack.isOf(PowerStones.GREENSTONE)) {
                remainingState = stateChannelB.with(PowerstoneWireBlock.POWER, powerB);
            }
            else {
                remainingState = stateChannelA.with(PowerstoneWireBlock.POWER, powerA);
            }
        }

        // Vanilla breaking is cancelled for a partial break,
        // so play the block's breaking sound manually.
        BlockSoundGroup soundGroup = state.getSoundGroup();
        world.playSound(null, pos, soundGroup.getBreakSound(), SoundCategory.BLOCKS, (soundGroup.getVolume() + 1.0F) / 2.0F, soundGroup.getPitch() * 0.8F);

        // The first state sent to the client already has the surviving channel's correct connections.
        // Do not force an additional synchronous redraw here.
        world.setBlockState(pos, remainingState, Block.NOTIFY_ALL);

        if (remainingState.isOf(Blocks.REDSTONE_WIRE)) {
            ((RedstoneWireBlockInterface) remainingState.getBlock()).updateAll(remainingState, world, pos);
        }
        else {
            ((PowerstoneWireBlock) remainingState.getBlock()).updateAll(remainingState, world, pos);
        }
    }

}
