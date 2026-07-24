package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.BlockStateBaseInterface;
import com.calvinmt.powerstones.LevelInterface;
import com.calvinmt.powerstones.PowerChannel;
import com.calvinmt.powerstones.PowerColour;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;
import com.google.common.collect.Sets;

import java.util.HashSet;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MultipleWiresBlock extends PowerstoneWireBlockBase implements EntityBlock {

    public static final EnumProperty<PowerPair> POWER_PAIR = PowerStones.POWER_PAIR;

    private static final int MULTIPLE_WIRE_TINT_A = 0;
    private static final int MULTIPLE_WIRE_TINT_B = 1;
    
    public MultipleWiresBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (VoxelShape)SHAPES_CACHE.get(state);
    }

    private BlockState getChannelConnectionState(BlockGetter level, BlockPos pos, BlockState multipleWiresState, PowerChannel channel) {
        PowerPair pair = multipleWiresState.getValue(POWER_PAIR);

        PowerColour colour = channel == PowerChannel.A ? pair.getColourA() : pair.getColourB();

        return this.getConnectionStateForColour(colour, level, pos);
    }

    private BlockState getConnectionStateForColour(PowerColour colour, BlockGetter world, BlockPos pos) {
        switch (colour) {
            case RED:
                return ((RedstoneWireBlockInterface) Blocks.REDSTONE_WIRE).getPlacementState(world, pos);
            case BLUE:
                return ((PowerstoneWireBlock) PowerStones.BLUESTONE_WIRE.get()).getPlacementState(world, pos);
            case GREEN:
                return ((PowerstoneWireBlock) PowerStones.GREENSTONE_WIRE.get()).getPlacementState(world, pos);
            case YELLOW:
                return ((PowerstoneWireBlock) PowerStones.YELLOWSTONE_WIRE.get()).getPlacementState(world, pos);
            default:
                throw new IllegalStateException("Unsupported power colour: " + colour);
        }
    }

    private void refreshChannelConnections(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }

        BlockState multipleWiresState = level.getBlockState(pos);

        // Prevents potential issues if the block state has changed to a different type of block.
        if (!multipleWiresState.is(this)) {
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof MultipleWiresBlockEntity)) {
            return;
        }

        MultipleWiresBlockEntity multipleWiresBlockEntity = (MultipleWiresBlockEntity) blockEntity;

        BlockState channelAState = this.getChannelConnectionState(level, pos, multipleWiresState, PowerChannel.A);
        BlockState channelBState = this.getChannelConnectionState(level, pos, multipleWiresState, PowerChannel.B);

        multipleWiresBlockEntity.setConnectionStates(channelAState, channelBState);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultipleWiresBlockEntity(pos, state);
    }

    private static PowerColour getPowerColour(ItemStack stack) {
        if (stack.is(Items.REDSTONE)) return PowerColour.RED;
        if (stack.is(PowerStones.BLUESTONE.get())) return PowerColour.BLUE;
        if (stack.is(PowerStones.GREENSTONE.get())) return PowerColour.GREEN;
        if (stack.is(PowerStones.YELLOWSTONE.get())) return PowerColour.YELLOW;

        return null;
    }

    private PowerColour getWireColour(BlockState state) {
        if (state.is(Blocks.REDSTONE_WIRE)) return PowerColour.RED;
        if (state.is(PowerStones.BLUESTONE_WIRE.get())) return PowerColour.BLUE;
        if (state.is(PowerStones.GREENSTONE_WIRE.get())) return PowerColour.GREEN;
        if (state.is(PowerStones.YELLOWSTONE_WIRE.get())) return PowerColour.YELLOW;

        return null;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();

        BlockState existingState = level.getBlockState(pos);

        PowerColour existingColour = this.getWireColour(existingState);
        PowerColour placedColour = getPowerColour(pContext.getItemInHand());

        PowerPair pair = PowerPair.getPairFromColours(existingColour, placedColour);

        BlockState placementState = this.crossState.setValue(POWER_PAIR, pair);

        return this.getConnectionState(level, placementState, pos);
    }

    @Override
    protected BlockState getDefaultBlockStateWithPowerProperties(BlockState state) {
        return this.defaultBlockState().setValue(POWER_PAIR, state.getValue(POWER_PAIR));
    }

    @Override
    protected BlockState getCrossStateWithPowerProperties(BlockState state) {
        return this.crossState.setValue(POWER_PAIR, state.getValue(POWER_PAIR));
    }

    @Override
    protected boolean isOtherConnectablePowerstone(BlockState state) {
        if (state.is(Blocks.REDSTONE_WIRE) || state.is(PowerStones.BLUESTONE_WIRE.get()) || state.is(PowerStones.GREENSTONE_WIRE.get()) || state.is(PowerStones.YELLOWSTONE_WIRE.get())) {
            return true;
        }
        return false;
    }

    public static void setPowerA(Level level, BlockPos pos, int power) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MultipleWiresBlockEntity multipleWiresBlockEntity) {
            multipleWiresBlockEntity.setPowerA(power);
        }
    }

    public static void setPowerB(Level level, BlockPos pos,int power) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MultipleWiresBlockEntity multipleWiresBlockEntity) {
            multipleWiresBlockEntity.setPowerB(power);
        }
    }

    public static int getPowerA(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MultipleWiresBlockEntity multipleWiresBlockEntity) {
            return multipleWiresBlockEntity.getPowerA();
        }
        return 0;
    }

    public static int getPowerB(BlockGetter level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MultipleWiresBlockEntity multipleWiresBlockEntity) {
            return multipleWiresBlockEntity.getPowerB();
        }
        return 0;
    }

    public static int getPowerForColour(BlockState state, BlockGetter level, BlockPos pos, PowerColour colour) {
        if (!state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return 0;
        }

        PowerPair pair = state.getValue(POWER_PAIR);

        if (!pair.contains(colour)) {
            return 0;
        }

        return switch (pair.getChannel(colour)) {
            case A -> getPowerA(level, pos);
            case B -> getPowerB(level, pos);
        };
    }

    private int calculateTargetStrengthColour(PowerColour colour, Level level, BlockPos pos) {
        switch (colour) {
            case RED:
                return this.calculateTargetStrengthRed(level, pos);
            case BLUE:
                return this.calculateTargetStrengthBlue(level, pos);
            case GREEN:
                return this.calculateTargetStrengthGreen(level, pos);
            case YELLOW:
                return this.calculateTargetStrengthYellow(level, pos);
            default:
                throw new IllegalStateException("Unexpected power colour: " + colour);
        }
    }

    @Override
    protected void updatePowerStrength(Level level, BlockPos pos, BlockState state) {
        // Refresh the connection states before calculating each channel's power.
        this.refreshChannelConnections(level, pos);

        int powerA = getPowerA(level, pos);
        int powerB = getPowerB(level, pos);

        PowerPair powerPair = state.getValue(POWER_PAIR);

        int targetPowerA = this.calculateTargetStrengthColour(powerPair.getColourA(), level, pos);
        int targetPowerB = this.calculateTargetStrengthColour(powerPair.getColourB(), level, pos);

        if (powerA == targetPowerA && powerB == targetPowerB) {
            return;
        }

        if (level.getBlockState(pos) != state) {
            return;
        }

        setPowerA(level, pos, targetPowerA);
        setPowerB(level, pos, targetPowerB);

        HashSet<BlockPos> positionsToUpdate = Sets.newHashSet();
        positionsToUpdate.add(pos);

        for (Direction direction : Direction.values()) {
            positionsToUpdate.add(pos.relative(direction));
        }

        for (BlockPos blockPos : positionsToUpdate) {
            level.updateNeighborsAt(blockPos, this);
        }
    }

    private int calculateTargetStrengthRed(Level level, BlockPos pos) {
        shouldSignal = false;
        ((RedstoneWireBlockInterface)Blocks.REDSTONE_WIRE).setShouldSignal(false);
        int i = level.getBestNeighborSignal(pos);
        shouldSignal = true;
        ((RedstoneWireBlockInterface)Blocks.REDSTONE_WIRE).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockpos = pos.relative(direction);
                BlockState blockstate = level.getBlockState(blockpos);
                j = Math.max(j, this.getWireSignalRed(blockstate,level, blockpos));
                BlockPos blockpos1 = pos.above();
                if (this.canSurviveOn(level, blockpos, blockstate) && !level.getBlockState(blockpos1).isRedstoneConductor(level, blockpos1)) {
                    BlockPos blockPosAbove = blockpos.above();
                    BlockState blockStateAbove = level.getBlockState(blockPosAbove);
                    j = Math.max(j, this.getWireSignalRed(blockStateAbove, level, blockPosAbove));
                } else if (!this.canSurviveOn(level, blockpos, blockstate)) {
                    BlockPos blockPosBelow = blockpos.below();
                    BlockState blockStateBelow = level.getBlockState(blockPosBelow);
                    j = Math.max(j, this.getWireSignalRed(blockStateBelow, level, blockPosBelow));
                }
            }
        }
        return Math.max(i, j - 1);
    }

    private int calculateTargetStrengthBlue(Level level, BlockPos pos) {
        shouldSignal = false;
        ((BluestoneWireBlock)PowerStones.BLUESTONE_WIRE.get()).setShouldSignal(false);
        int i = ((LevelInterface) level).getBestNeighborSignalBlue(pos);
        shouldSignal = true;
        ((BluestoneWireBlock)PowerStones.BLUESTONE_WIRE.get()).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockPos = pos.relative(direction);
                BlockState blockState = level.getBlockState(blockPos);
                j = Math.max(j, this.getWireSignalBlue(blockState, level, blockPos));
                BlockPos blockPos2 = pos.above();
                if (this.canSurviveOn(level, blockPos, blockState) && !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                    BlockPos blockPosAbove = blockPos.above();
                    BlockState blockStateAbove = level.getBlockState(blockPosAbove);
                    j = Math.max(j, this.getWireSignalBlue(blockStateAbove, level, blockPosAbove));
                    continue;
                }
                if (this.canSurviveOn(level, blockPos, blockState)) continue;
                BlockPos blockPosBelow = blockPos.below();
                BlockState blockStateBelow = level.getBlockState(blockPosBelow);
                j = Math.max(j, this.getWireSignalBlue(blockStateBelow, level, blockPosBelow));
            }
        }
        return Math.max(i, j - 1);
    }

    private int calculateTargetStrengthGreen(Level level, BlockPos pos) {
        shouldSignal = false;
        ((GreenstoneWireBlock)PowerStones.GREENSTONE_WIRE.get()).setShouldSignal(false);
        int i = ((LevelInterface) level).getBestNeighborSignalGreen(pos);
        shouldSignal = true;
        ((GreenstoneWireBlock)PowerStones.GREENSTONE_WIRE.get()).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockPos = pos.relative(direction);
                BlockState blockState = level.getBlockState(blockPos);
                j = Math.max(j, this.getWireSignalGreen(blockState, level, blockPos));
                BlockPos blockPos2 = pos.above();
                if (this.canSurviveOn(level, blockPos, blockState) && !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                    BlockPos blockPosAbove = blockPos.above();
                    BlockState blockStateAbove = level.getBlockState(blockPosAbove);
                    j = Math.max(j, this.getWireSignalGreen(blockStateAbove, level, blockPosAbove));
                    continue;
                }
                if (this.canSurviveOn(level, blockPos, blockState)) continue;
                BlockPos blockPosBelow = blockPos.below();
                BlockState blockStateBelow = level.getBlockState(blockPosBelow);
                j = Math.max(j, this.getWireSignalGreen(blockStateBelow, level, blockPosBelow));
            }
        }
        return Math.max(i, j - 1);
    }

    private int calculateTargetStrengthYellow(Level level, BlockPos pos) {
        shouldSignal = false;
        ((YellowstoneWireBlock)PowerStones.YELLOWSTONE_WIRE.get()).setShouldSignal(false);
        int i = ((LevelInterface) level).getBestNeighborSignalYellow(pos);
        shouldSignal = true;
        ((YellowstoneWireBlock)PowerStones.YELLOWSTONE_WIRE.get()).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockPos = pos.relative(direction);
                BlockState blockState = level.getBlockState(blockPos);
                j = Math.max(j, this.getWireSignalYellow(blockState, level, blockPos));
                BlockPos blockPos2 = pos.above();
                if (this.canSurviveOn(level, blockPos, blockState) && !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                    BlockPos blockPosAbove = blockPos.above();
                    BlockState blockStateAbove = level.getBlockState(blockPosAbove);
                    j = Math.max(j, this.getWireSignalYellow(blockStateAbove, level, blockPosAbove));
                    continue;
                }
                if (this.canSurviveOn(level, blockPos, blockState)) continue;
                BlockPos blockPosBelow = blockPos.below();
                BlockState blockStateBelow = level.getBlockState(blockPosBelow);
                j = Math.max(j, this.getWireSignalYellow(blockStateBelow, level, blockPosBelow));
            }
        }
        return Math.max(i, j - 1);
    }

    private int getMultipleWireSignal(BlockState state, Level level, BlockPos pos, PowerColour colour) {
        if (!state.is(this)) {
            return 0;
        }

        PowerPair pair = state.getValue(POWER_PAIR);

        if (pair.getColourA() == colour) {
            return getPowerA(level, pos);
        }

        if (pair.getColourB() == colour) {
            return getPowerB(level, pos);
        }

        return 0;
    }

    private int getWireSignalRed(BlockState state, Level level, BlockPos pos) {
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return state.getValue(RedStoneWireBlock.POWER);
        }

        return this.getMultipleWireSignal(state, level, pos, PowerColour.RED);
    }

    private int getWireSignalBlue(BlockState state, Level level, BlockPos pos) {
        if (state.is(PowerStones.BLUESTONE_WIRE.get())) {
            return state.getValue(PowerstoneWireBlock.POWER);
        }

        return this.getMultipleWireSignal(state, level, pos, PowerColour.BLUE);
    }

    private int getWireSignalGreen(BlockState state, Level level, BlockPos pos) {
        if (state.is(PowerStones.GREENSTONE_WIRE.get())) {
            return state.getValue(PowerstoneWireBlock.POWER);
        }

        return this.getMultipleWireSignal(state, level, pos, PowerColour.GREEN);
    }

    private int getWireSignalYellow(BlockState state, Level level, BlockPos pos) {
        if (state.is(PowerStones.YELLOWSTONE_WIRE.get())) {
            return state.getValue(PowerstoneWireBlock.POWER);
        }

        return this.getMultipleWireSignal(state, level, pos, PowerColour.YELLOW);
    }

    @Override
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return ! shouldSignal ? 0 : blockState.getSignal(blockAccess, pos, side);
    }

    @Override
    public int getDirectSignalBlue(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return ! shouldSignal ? 0 : ((BlockStateBaseInterface)blockState).getSignalBlue(blockAccess, pos, side);
    }

    @Override
    public int getDirectSignalGreen(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return ! shouldSignal ? 0 : ((BlockStateBaseInterface)blockState).getSignalGreen(blockAccess, pos, side);
    }

    @Override
    public int getDirectSignalYellow(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return ! shouldSignal ? 0 : ((BlockStateBaseInterface)blockState).getSignalYellow(blockAccess, pos, side);
    }

    private int getChannelPower(BlockGetter level, BlockPos pos, PowerChannel channel) {
        return switch (channel) {
            case A -> getPowerA(level, pos);
            case B -> getPowerB(level, pos);
        };
    }

    private int getWeakColourPower(BlockState state, BlockGetter level, BlockPos pos, Direction direction, PowerColour colour) {
        if (!shouldSignal || direction == Direction.DOWN) {
            return 0;
        }

        PowerPair pair = state.getValue(POWER_PAIR);

        if (! pair.contains(colour)) {
            return 0;
        }

        PowerChannel channel = pair.getChannel(colour);
        int power = this.getChannelPower(level, pos, channel);

        if (power == 0) {
            return 0;
        }

        if (direction == Direction.UP) {
            return power;
        }

        BlockState channelState = this.getChannelConnectionState(level, pos, state, channel);
        RedstoneSide connection = channelState.getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()));

        return connection.isConnected() ? power : 0;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getWeakColourPower(state, level, pos, direction, PowerColour.RED);
    }

    @Override
    public int getSignalBlue(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getWeakColourPower(state, level, pos, direction, PowerColour.BLUE);
    }

    @Override
    public int getSignalGreen(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getWeakColourPower(state, level, pos, direction, PowerColour.GREEN);
    }

    @Override
    public int getSignalYellow(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getWeakColourPower(state, level, pos, direction, PowerColour.YELLOW);
    }

    @Override
    protected boolean shouldConnectToAbove(BlockGetter level, BlockPos posAbove, BlockState stateAbove, Direction direction) {
        BlockState originalState = level.getBlockState(posAbove.below().relative(direction.getOpposite()));
        return this.shouldConnectTo(originalState, level, posAbove, stateAbove, null);
    }

    @Override
    protected boolean shouldConnectToBelow(BlockGetter level, BlockPos posBelow, BlockState stateBelow, Direction direction) {
        BlockState originalState = level.getBlockState(posBelow.above().relative(direction.getOpposite()));
        return this.shouldConnectTo(originalState, level, posBelow, stateBelow, null);
    }
    
    @Override
    protected boolean shouldConnectTo(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        BlockState originalState = level.getBlockState(pos.relative(direction.getOpposite()));
        return this.shouldConnectTo(originalState, level, pos, state, direction);
    }

    private static boolean connectsTo(BlockState state, @Nullable Direction direction) {
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return true;
        }

        if (state.is(Blocks.REPEATER)) {
            Direction facing = state.getValue(RepeaterBlock.FACING);

            return facing == direction || facing.getOpposite() == direction;
        }

        if (state.is(Blocks.OBSERVER)) {
            return direction == state.getValue(ObserverBlock.FACING);
        }

        return direction != null && state.isSignalSource();
    }

    @Nullable
    private PowerColour getPowerSourceColour(BlockState state) {
        if (state.is(Blocks.REDSTONE_TORCH) || state.is(Blocks.REDSTONE_WALL_TORCH) || state.is(Blocks.REDSTONE_BLOCK)) {
            return PowerColour.RED;
        }
        if (state.is(PowerStones.BLUESTONE_TORCH_BLOCK.get()) || state.is(PowerStones.BLUESTONE_WALL_TORCH.get()) || state.is(PowerStones.BLUESTONE_BLOCK.get())) {
            return PowerColour.BLUE;
        }
        if (state.is(PowerStones.GREENSTONE_TORCH_BLOCK.get()) || state.is(PowerStones.GREENSTONE_WALL_TORCH.get()) || state.is(PowerStones.GREENSTONE_BLOCK.get())) {
            return PowerColour.GREEN;
        }
        if (state.is(PowerStones.YELLOWSTONE_TORCH_BLOCK.get()) || state.is(PowerStones.YELLOWSTONE_WALL_TORCH.get()) || state.is(PowerStones.YELLOWSTONE_BLOCK.get())) {
            return PowerColour.YELLOW;
        }

        return null;
    }

    protected boolean shouldConnectTo(BlockState multipleWiresState, BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        PowerPair pair = multipleWiresState.getValue(POWER_PAIR);

        if (state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return pair.sharesColourWith(state.getValue(POWER_PAIR));
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

        return connectsTo(state, direction);
    }

    public static int getColorForTintIndex(BlockState state, BlockGetter level, BlockPos pos, int tintIndex) {
        PowerPair powerPair = state.getValue(POWER_PAIR);
        int powerA = getPowerA(level, pos);
        int powerB = getPowerB(level, pos);

        if (level != null && pos != null) {
            powerA = getPowerA(level, pos);
            powerB = getPowerB(level, pos);

            // The blockstate packet can arrive before the completed block entity packet.
            // Use the same prediction as the custom model so powered wires
            // do not briefly use power level zero during that interval.
            MultipleWiresBlockEntity.RenderData predictedData = MultipleWiresBlockEntity.getPredictedRenderData(pos);

            if (predictedData != null && predictedData.powerPair() == powerPair) {
                powerA = predictedData.powerA();
                powerB = predictedData.powerB();
            }
        }

        if (tintIndex == MULTIPLE_WIRE_TINT_A) {
            return powerPair.getColourA().getWireColour(powerA);
        }

        if (tintIndex == MULTIPLE_WIRE_TINT_B) {
            return powerPair.getColourB().getWireColour(powerB);
        }

        return PowerColour.WHITE;
    }

    @Override
    protected boolean hasPowerOn(BlockState state, Level level, BlockPos pos) {
        return getPowerA(level, pos) > 0 || getPowerB(level, pos) > 0;
    }

    @Override
    protected Vec3 getPowerstoneColor(BlockState state, Level level, BlockPos pos, Random random) {
        PowerPair powerPair = state.getValue(POWER_PAIR);
        int powerA = getPowerA(level, pos);
        int powerB = getPowerB(level, pos);

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

        return Vec3.ZERO;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWER_PAIR);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand pHand, BlockHitResult pHit) {
        if (!player.getAbilities().mayBuild) {
            return InteractionResult.PASS;
        } else {
            if (isCross(state) || isDot(state)) {
                BlockState blockstate = isCross(state) ? this.defaultBlockState() : this.crossState;
                blockstate = blockstate.setValue(POWER_PAIR, state.getValue(POWER_PAIR));
                blockstate = this.getConnectionState(level, blockstate, pos);
                setPowerA(level, pos, getPowerA(level, pos));
                setPowerB(level, pos, getPowerB(level, pos));
                if (blockstate != state) {
                    level.setBlock(pos, blockstate, 3);
                    this.updatesOnShapeChange(level, pos, state, blockstate);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }
    }

    private int getSingleWirePower(BlockState state) {
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return state.getValue(RedStoneWireBlock.POWER);
        }

        if (state.is(PowerStones.BLUESTONE_WIRE.get()) || state.is(PowerStones.GREENSTONE_WIRE.get()) || state.is(PowerStones.YELLOWSTONE_WIRE.get())) {
            return state.getValue(PowerstoneWireBlock.POWER);
        }

        throw new IllegalArgumentException("Unsupported single wire state: " + state);
    }

    public boolean convertFromSingleWire(Level level, BlockPos pos, BlockState singleWireState, Player player, InteractionHand hand) {
        ItemStack heldItemStack = player.getItemInHand(hand);

        PowerColour existingColour = this.getWireColour(singleWireState);
        PowerColour placedColour = getPowerColour(heldItemStack);

        if (existingColour == null || placedColour == null || existingColour == placedColour) {
            return false;
        }

        PowerPair powerPair = PowerPair.getPairFromColours(existingColour, placedColour);

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
            channelBState = this.getConnectionStateForColour(powerPair.getColourB(), level, pos);
        }
        else {
            powerA = 0;
            powerB = existingPower;

            channelAState = this.getConnectionStateForColour(powerPair.getColourA(), level, pos);
            channelBState = singleWireState;
        }

        MultipleWiresBlockEntity.RenderData initialRenderData = MultipleWiresBlockEntity.createRenderData(powerPair, powerA, powerB, channelAState, channelBState);

        // 'onUse' runs on both the client and server.
        // On the client, only store the expected render data.
        // Do not replace the block or notify neighbours on the client.
        if (level.isClientSide) {
            MultipleWiresBlockEntity.setPredictedRenderData(pos, initialRenderData);

            return true;
        }

        // Preserve the current connection state of the original single wire
        // when initially creating the MultipleWiresBlock.
        BlockState stateMultipleWires = this.defaultBlockState()
            .setValue(NORTH, singleWireState.getValue(NORTH))
            .setValue(EAST, singleWireState.getValue(EAST))
            .setValue(SOUTH, singleWireState.getValue(SOUTH))
            .setValue(WEST, singleWireState.getValue(WEST))
            .setValue(POWER_PAIR, powerPair);

        // Mark this conversion before replacing the block.
        // The new block entity is constructed synchronously inside 'setBlockState',
        // allowing it to suppress incomplete update packets during its callbacks.
        MultipleWiresBlockEntity.beginConversionInitialisation(pos, initialRenderData);

        boolean placed;

        try {
            // Install the new block silently on the server.
            //
            // UPDATE_ALL would notify neighbouring wires before the new block
            // entity has received powerA and powerB. Those neighbours would
            // therefore observe a real zero-power MultipleWiresBlock and
            // briefly recalculate the network down to zero.
            //
            // UPDATE_KNOWN_SHAPE also prevents onBlockAdded from running during this
            // incomplete state. The normal wire update process is invoked
            // explicitly below, after setInitialData has completed.
            placed = level.setBlock(pos, stateMultipleWires, Block.UPDATE_KNOWN_SHAPE);
        }
        finally {
            MultipleWiresBlockEntity.endConversionInitialisation(pos);
        }

        if (!placed) {
            return false;
        }

        // Get the block entity for the new MultipleWiresBlock.
        // It should be present because the block was just placed.
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof MultipleWiresBlockEntity)) {
            // This should not normally happen, but restore the original block
            // rather than leaving an invalid MultipleWiresBlock.
            level.setBlock(pos, singleWireState, Block.UPDATE_ALL);

            return false;
        }

        MultipleWiresBlockEntity multipleWiresBlockEntity = (MultipleWiresBlockEntity) blockEntity;

        // Initialise both powers and both independent channel connection states together.
        multipleWiresBlockEntity.setInitialData(powerA, powerB, channelAState, channelBState);

        // Keep the normal wire update process.
        // This updates direct wires, offset wires, power strengths
        // and the final shared connection state.
        this.updateAll(level.getBlockState(pos), level, pos);

        // Permit update packets only after powers and both channel states have been fully established.
        // This makes the first block entity packet received by the client a complete snapshot.
        multipleWiresBlockEntity.finishConversionInitialisation();

        // Send the final block state and completed block entity to the client.
        // This also replaces the temporary predicted render data.
        BlockState finalState = level.getBlockState(pos);
        level.sendBlockUpdated(pos, singleWireState, finalState, Block.UPDATE_CLIENTS);

        SoundType soundType = singleWireState.getSoundType();
        level.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

        // Decrement the held item stack if the player is not in creative mode.
        if (!player.getAbilities().instabuild) {
            heldItemStack.shrink(1);
        }

        return true;
    }

    private static boolean heldColourIsInPair(BlockState state, ItemStack heldItemStack) {
        if (!state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return false;
        }

        PowerColour heldColour = getPowerColour(heldItemStack);

        return heldColour != null && state.getValue(POWER_PAIR).contains(heldColour);
    }

    public static boolean shouldBreakBlock(BlockState state, ItemStack heldItemStack) {
        if (!state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return true;
        }

        PowerColour heldColour = getPowerColour(heldItemStack);

        return heldColour == null || state.getValue(POWER_PAIR).contains(heldColour);
    }

    public static boolean shouldBreakIntoSingle(BlockState state, ItemStack heldItemStack) {
        return heldColourIsInPair(state, heldItemStack);
    }

    private BlockState withWirePower(BlockState state, PowerColour colour, int power) {
        if (colour == PowerColour.RED) {
            return state.setValue(RedStoneWireBlock.POWER, power);
        }

        return state.setValue(PowerstoneWireBlock.POWER, power);

    }

    public void breakSingle(Level level, BlockPos pos, BlockState state, Player player) {
        ItemStack heldItemStack = player.getMainHandItem();

        if (!shouldBreakIntoSingle(state, heldItemStack)) {
            return;
        }

        PowerColour removedColour = getPowerColour(heldItemStack);

        if (removedColour == null) {
            return;
        }

        PowerPair powerPair = state.getValue(POWER_PAIR);
        PowerChannel removedChannel = powerPair.getChannel(removedColour);

        // Get the current power levels for both channels and the connection states for each channel
        // while the block is still in the world.
        int powerA = getPowerA(level, pos);
        int powerB = getPowerB(level, pos);
        BlockState stateChannelA = this.getChannelConnectionState(level, pos, state, PowerChannel.A);
        BlockState stateChannelB = this.getChannelConnectionState(level, pos, state, PowerChannel.B);

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
        SoundType soundType = state.getSoundType();
        level.playSound(null, pos, soundType.getBreakSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0F) / 2.0F, soundType.getPitch() * 0.8F);

        // The first state sent to the client already has the surviving channel's correct connections.
        // Do not force an additional synchronous redraw here.
        level.setBlock(pos, remainingState, Block.UPDATE_ALL);

        if (remainingState.is(Blocks.REDSTONE_WIRE)) {
            ((RedstoneWireBlockInterface) remainingState.getBlock()).updateAll(remainingState, level, pos);
        }
        else {
            ((PowerstoneWireBlock) remainingState.getBlock()).updateAll(remainingState, level, pos);
        }
    }

}
