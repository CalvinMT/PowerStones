package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.PowerChannel;
import com.calvinmt.powerstones.PowerColour;
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

import java.util.HashSet;
import java.util.Random;

import org.jetbrains.annotations.Nullable;

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
        PowerColour colour = channel == PowerChannel.A ? pair.getColourA() : pair.getColourB();

        return this.getConnectionStateForColour(colour, world, pos);
    }

    private BlockState getConnectionStateForColour(PowerColour colour, BlockView world, BlockPos pos) {
        switch (colour) {
            case RED:
                return ((RedstoneWireBlockInterface) Blocks.REDSTONE_WIRE).getConnectionState(world, pos);
            case BLUE:
                return ((PowerstoneWireBlock) PowerStones.BLUESTONE_WIRE).getConnectionState(world, pos);
            case GREEN:
                return ((PowerstoneWireBlock) PowerStones.GREENSTONE_WIRE).getConnectionState(world, pos);
            case YELLOW:
                return ((PowerstoneWireBlock) PowerStones.YELLOWSTONE_WIRE).getConnectionState(world, pos);
            default:
                throw new IllegalStateException("Unsupported power colour: " + colour);
        }
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

    private static PowerColour getPowerColour(ItemStack stack) {
        if (stack.isOf(Items.REDSTONE)) return PowerColour.RED;
        if (stack.isOf(PowerStones.BLUESTONE)) return PowerColour.BLUE;
        if (stack.isOf(PowerStones.GREENSTONE)) return PowerColour.GREEN;
        if (stack.isOf(PowerStones.YELLOWSTONE)) return PowerColour.YELLOW;

        return null;
    }

    private PowerColour getWireColour(BlockState state) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) return PowerColour.RED;
        if (state.isOf(PowerStones.BLUESTONE_WIRE)) return PowerColour.BLUE;
        if (state.isOf(PowerStones.GREENSTONE_WIRE)) return PowerColour.GREEN;
        if (state.isOf(PowerStones.YELLOWSTONE_WIRE)) return PowerColour.YELLOW;

        return null;
    }

    private static PowerChannel getSingleWireRenderChannel(PowerColour colour) {
        return switch (colour) {
            case RED, GREEN -> PowerChannel.A;
            case BLUE, YELLOW -> PowerChannel.B;
        };
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        BlockView world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        BlockState existingState = world.getBlockState(pos);

        PowerColour existingColour = this.getWireColour(existingState);
        PowerColour placedColour = getPowerColour(context.getStack());

        PowerPair pair = PowerPair.getPairFromColours(existingColour, placedColour);

        BlockState placementState = this.dotState.with(POWER_PAIR, pair);

        return this.getPlacementState(world, placementState, pos);
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

    public static int getPowerForColour(BlockState state, BlockView world, BlockPos pos, PowerColour colour) {
        if (!state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return 0;
        }

        PowerPair pair = state.get(POWER_PAIR);

        if (!pair.contains(colour)) {
            return 0;
        }

        return switch (pair.getChannel(colour)) {
            case A -> getPowerA(world, pos);
            case B -> getPowerB(world, pos);
        };
    }

    private int calculateTargetStrengthColour(PowerColour colour, World world, BlockPos pos) {
        switch (colour) {
            case RED:
                return this.calculateTargetStrengthRed(world, pos);
            case BLUE:
                return this.calculateTargetStrengthBlue(world, pos);
            case GREEN:
                return this.calculateTargetStrengthGreen(world, pos);
            case YELLOW:
                return this.calculateTargetStrengthYellow(world, pos);
            default:
                throw new IllegalStateException("Unexpected power colour: " + colour);
        }
    }

    @Override
    protected void updatePowerStrength(World world, BlockPos pos, BlockState state) {
        // Refresh the connection states before calculating each channel's power.
        this.refreshChannelConnections(world, pos);

        int powerA = getPowerA(world, pos);
        int powerB = getPowerB(world, pos);

        PowerPair powerPair = state.get(POWER_PAIR);

        int targetPowerA = this.calculateTargetStrengthColour(powerPair.getColourA(), world, pos);
        int targetPowerB = this.calculateTargetStrengthColour(powerPair.getColourB(), world, pos);

        if (powerA == targetPowerA && powerB == targetPowerB) {
            return;
        }

        if (world.getBlockState(pos) != state) {
            return;
        }

        setPowerA(world, pos, targetPowerA);
        setPowerB(world, pos, targetPowerB);

        HashSet<BlockPos> positionsToUpdate = Sets.newHashSet();
        positionsToUpdate.add(pos);

        for (Direction direction : Direction.values()) {
            positionsToUpdate.add(pos.offset(direction));
        }

        for (BlockPos blockPos : positionsToUpdate) {
            world.updateNeighbors(blockPos, this);
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

    private int getMultipleWireSignal(BlockState state, World world, BlockPos pos, PowerColour colour) {
        if (!state.isOf(this)) {
            return 0;
        }

        PowerPair pair = state.get(POWER_PAIR);

        if (pair.getColourA() == colour) {
            return getPowerA(world, pos);
        }

        if (pair.getColourB() == colour) {
            return getPowerB(world, pos);
        }

        return 0;
    }

    private int getWireSignalRed(BlockState state, World world, BlockPos pos) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) {
            return state.get(RedstoneWireBlock.POWER);
        }

        return this.getMultipleWireSignal(state, world, pos, PowerColour.RED);
    }

    private int getWireSignalBlue(BlockState state, World world, BlockPos pos) {
        if (state.isOf(PowerStones.BLUESTONE_WIRE)) {
            return state.get(PowerstoneWireBlock.POWER);
        }

        return this.getMultipleWireSignal(state, world, pos, PowerColour.BLUE);
    }

    private int getWireSignalGreen(BlockState state, World world, BlockPos pos) {
        if (state.isOf(PowerStones.GREENSTONE_WIRE)) {
            return state.get(PowerstoneWireBlock.POWER);
        }

        return this.getMultipleWireSignal(state, world, pos, PowerColour.GREEN);
    }

    private int getWireSignalYellow(BlockState state, World world, BlockPos pos) {
        if (state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            return state.get(PowerstoneWireBlock.POWER);
        }

        return this.getMultipleWireSignal(state, world, pos, PowerColour.YELLOW);
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

    private int getChannelPower(BlockView world, BlockPos pos, PowerChannel channel) {
        return switch (channel) {
            case A -> getPowerA(world, pos);
            case B -> getPowerB(world, pos);
        };
    }

    private int getWeakColourPower(BlockState state, BlockView world, BlockPos pos, Direction direction, PowerColour colour) {
        if (!wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }

        PowerPair pair = state.get(POWER_PAIR);

        if (! pair.contains(colour)) {
            return 0;
        }

        PowerChannel channel = pair.getChannel(colour);
        int power = this.getChannelPower(world, pos, channel);

        if (power == 0) {
            return 0;
        }

        if (direction == Direction.UP) {
            return power;
        }

        BlockState channelState = this.getChannelConnectionState(world, pos, state, channel);
        WireConnection connection = channelState.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()));

        return connection.isConnected() ? power : 0;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.getWeakColourPower(state, world, pos, direction, PowerColour.RED);
    }

    @Override
    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.getWeakColourPower(state, world, pos, direction, PowerColour.BLUE);
    }

    @Override
    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.getWeakColourPower(state, world, pos, direction, PowerColour.GREEN);
    }

    @Override
    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return this.getWeakColourPower(state, world, pos, direction, PowerColour.YELLOW);
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

    @Nullable
    private PowerColour getPowerSourceColour(BlockState state) {
        if (state.isOf(Blocks.REDSTONE_TORCH) || state.isOf(Blocks.REDSTONE_WALL_TORCH) || state.isOf(Blocks.REDSTONE_BLOCK)) {
            return PowerColour.RED;
        }
        if (state.isOf(PowerStones.BLUESTONE_TORCH_BLOCK) || state.isOf(PowerStones.BLUESTONE_WALL_TORCH) || state.isOf(PowerStones.BLUESTONE_BLOCK)) {
            return PowerColour.BLUE;
        }
        if (state.isOf(PowerStones.GREENSTONE_TORCH_BLOCK) || state.isOf(PowerStones.GREENSTONE_WALL_TORCH) || state.isOf(PowerStones.GREENSTONE_BLOCK)) {
            return PowerColour.GREEN;
        }
        if (state.isOf(PowerStones.YELLOWSTONE_TORCH_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_WALL_TORCH) || state.isOf(PowerStones.YELLOWSTONE_BLOCK)) {
            return PowerColour.YELLOW;
        }

        return null;
    }

    protected boolean shouldConnectTo(BlockState multipleWiresState, BlockView world, BlockPos pos, BlockState state, Direction direction) {
        PowerPair pair = multipleWiresState.get(POWER_PAIR);

        if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return pair.sharesColourWith(state.get(POWER_PAIR));
        }

        PowerColour wireColour = this.getWireColour(state);

        if (wireColour != null) {
            return pair.contains(wireColour);
        }

        if (direction != null) {
            PowerColour sourceColour = this.getPowerSourceColour(state);

            if (sourceColour != null) {
                return pair.contains(sourceColour);
            }
        }

        return this.connectsTo(state, direction);
    }

    public static int getColorForTintIndex(BlockState state, BlockView world, BlockPos pos, int tintIndex) {
        PowerPair powerPair = state.get(POWER_PAIR);
        int powerA = getPowerA(world, pos);
        int powerB = getPowerB(world, pos);

        // The blockstate packet can arrive before the completed block entity packet.
        // Use the same prediction as the custom model so powered wires
        // do not briefly use power level zero during that interval.
        MultipleWiresBlockEntity.RenderData predictedData = MultipleWiresBlockEntity.getPredictedRenderData(pos);

        if (predictedData != null && predictedData.powerPair() == powerPair) {
            powerA = predictedData.powerA();
            powerB = predictedData.powerB();
        }

        PowerColour colourA = powerPair.getColourA();
        PowerColour colourB = powerPair.getColourB();

        if (tintIndex == colourA.getTintIndex()) {
            return colourA.getWireColour(powerA);
        }

        if (tintIndex == colourB.getTintIndex()) {
            return colourB.getWireColour(powerB);
        }

        return PowerColour.WHITE;
    }

    @Override
    protected boolean hasPowerOn(World world, BlockPos pos) {
        return getPowerA(world, pos) > 0 || getPowerB(world, pos) > 0;
    }

    @Override
    protected Vec3d getPowerstoneColor(BlockState state, World world, BlockPos pos, Random random) {
        PowerPair powerPair = state.get(POWER_PAIR);
        int powerA = getPowerA(world, pos);
        int powerB = getPowerB(world, pos);

        boolean channelAPowered = powerA > 0;
        boolean channelBPowered = powerB > 0;

        if (channelAPowered && channelBPowered) {
            return random.nextBoolean() ? powerPair.getColourA().getColour(powerA) : powerPair.getColourB().getColour(powerB);
        }
        if (channelAPowered) {
            return powerPair.getColourA().getColour(powerA);
        }
        if (channelBPowered) {
            return powerPair.getColourB().getColour(powerB);
        }

        return Vec3d.ZERO;
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

    private int getSingleWirePower(BlockState state) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) {
            return state.get(RedstoneWireBlock.POWER);
        }

        if (state.isOf(PowerStones.BLUESTONE_WIRE) || state.isOf(PowerStones.GREENSTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            return state.get(PowerstoneWireBlock.POWER);
        }

        throw new IllegalArgumentException("Unsupported single wire state: " + state);
    }

    public boolean convertFromSingleWire(World world, BlockPos pos, BlockState singleWireState, PlayerEntity player, Hand hand) {
        ItemStack heldItemStack = player.getStackInHand(hand);

        PowerColour existingColour = this.getWireColour(singleWireState);
        PowerColour placedColour = getPowerColour(heldItemStack);

        if (existingColour == null || placedColour == null || existingColour == placedColour) {
            return false;
        }

        PowerPair powerPair = PowerPair.getPairFromColours(existingColour, placedColour);

        boolean renderChannelsSwapped = powerPair.getChannel(existingColour) != getSingleWireRenderChannel(existingColour);

        int existingPower = this.getSingleWirePower(singleWireState);

        int powerA;
        int powerB;

        BlockState channelAState;
        BlockState channelBState;

        // Calculate both channels while the original single wire is still in the world.
        // The original channel therefore retains its exact current shape.
        if (powerPair.getColourA() == existingColour) {
            powerA = existingPower;
            powerB = 0;

            channelAState = singleWireState;
            channelBState = this.getConnectionStateForColour(powerPair.getColourB(), world, pos);
        }
        else {
            powerA = 0;
            powerB = existingPower;

            channelAState = this.getConnectionStateForColour(powerPair.getColourA(), world, pos);
            channelBState = singleWireState;
        }

        MultipleWiresBlockEntity.RenderData initialRenderData = MultipleWiresBlockEntity.createRenderData(renderChannelsSwapped, powerPair, powerA, powerB, channelAState, channelBState);

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
        multipleWiresBlockEntity.setInitialData(renderChannelsSwapped, powerA, powerB, channelAState, channelBState);

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

    private static boolean heldColourIsInPair(BlockState state, ItemStack heldItemStack) {
        if (!state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return false;
        }

        PowerColour heldColour = getPowerColour(heldItemStack);

        return heldColour != null && state.get(POWER_PAIR).contains(heldColour);
    }

    public static boolean shouldBreakBlock(BlockState state, ItemStack heldItemStack) {
        if (!state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return true;
        }

        PowerColour heldColour = getPowerColour(heldItemStack);

        return heldColour == null || state.get(POWER_PAIR).contains(heldColour);
    }

    public static boolean shouldBreakIntoSingle(BlockState state, ItemStack heldItemStack) {
        return heldColourIsInPair(state, heldItemStack);
    }

    private BlockState withWirePower(BlockState state, PowerColour colour, int power) {
        if (colour == PowerColour.RED) {
            return state.with(RedstoneWireBlock.POWER, power);
        }

        return state.with(PowerstoneWireBlock.POWER, power);
    }

    public void breakSingle(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ItemStack heldItemStack = player.getMainHandStack();

        if (!shouldBreakIntoSingle(state, heldItemStack)) {
            return;
        }

        PowerColour removedColour = getPowerColour(heldItemStack);

        if (removedColour == null) {
            return;
        }

        PowerPair powerPair = state.get(POWER_PAIR);
        PowerChannel removedChannel = powerPair.getChannel(removedColour);

        // Get the current power levels for both channels and the connection states for each channel
        // while the block is still in the world.
        int powerA = getPowerA(world, pos);
        int powerB = getPowerB(world, pos);
        BlockState stateChannelA = this.getChannelConnectionState(world, pos, state, PowerChannel.A);
        BlockState stateChannelB = this.getChannelConnectionState(world, pos, state, PowerChannel.B);

        PowerColour remainingColour;
        BlockState remainingState;
        int remainingPower;

        if (removedChannel == PowerChannel.A) {
            remainingColour = powerPair.getColourB();
            remainingState = stateChannelB;
            remainingPower = powerB;
        }
        else {
            remainingColour = powerPair.getColourA();
            remainingState = stateChannelA;
            remainingPower = powerA;
        }

        remainingState = this.withWirePower(remainingState, remainingColour, remainingPower);

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
