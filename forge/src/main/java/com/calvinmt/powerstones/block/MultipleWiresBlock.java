package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.BlockStateBaseInterface;
import com.calvinmt.powerstones.LevelInterface;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.levelgen.RandomSource;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class MultipleWiresBlock extends PowerstoneWireBlockBase implements EntityBlock {

    public static final EnumProperty<PowerPair> POWER_PAIR = PowerStones.POWER_PAIR;
    
    public MultipleWiresBlock(BlockBehaviour.Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MultipleWiresBlockEntity(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if (pContext.getItemInHand().is(Items.REDSTONE) || pContext.getItemInHand().is(PowerStones.BLUESTONE.get())) {
            this.registerDefaultState(this.defaultBlockState().setValue(POWER_PAIR, PowerPair.RED_BLUE));
        }
        if (pContext.getItemInHand().is(PowerStones.GREENSTONE.get()) || pContext.getItemInHand().is(PowerStones.YELLOWSTONE.get())) {
            this.registerDefaultState(this.defaultBlockState().setValue(POWER_PAIR, PowerPair.GREEN_YELLOW));
        }
        return this.getConnectionState(pContext.getLevel(), this.crossState.setValue(POWER_PAIR, this.defaultBlockState().getValue(POWER_PAIR)), pContext.getClickedPos());
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

    @Override
    protected void updatePowerStrength(Level level, BlockPos pos, BlockState state) {
        int powerA = getPowerA(level, pos);
        int powerB = getPowerB(level, pos);
        int r = this.calculateTargetStrengthRed(level, pos);
        int b = this.calculateTargetStrengthBlue(level, pos);
        int g = this.calculateTargetStrengthGreen(level, pos);
        int y = this.calculateTargetStrengthYellow(level, pos);
        if ((state.getValue(POWER_PAIR) == PowerPair.RED_BLUE && powerA != r)
            || (state.getValue(POWER_PAIR) == PowerPair.RED_BLUE && powerB != b)
            || (state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW && powerA != g)
            || (state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW && powerB != y)) {
            if (level.getBlockState(pos) == state) {
                if (state.getValue(POWER_PAIR) == PowerPair.RED_BLUE && powerA != r)
                    powerA = r;
                if (state.getValue(POWER_PAIR) == PowerPair.RED_BLUE && powerB != b)
                    powerB = b;
                if (state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW && powerA != g)
                    powerA = g;
                if (state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW && powerB != y)
                    powerB = y;
                setPowerA(level, pos, powerA);
                setPowerB(level, pos, powerB);
            }
            HashSet<BlockPos> set = Sets.newHashSet();
            set.add(pos);
            for (Direction direction : Direction.values()) {
                set.add(pos.relative(direction));
            }
            for (BlockPos blockPos : set) {
                level.updateNeighborsAt(blockPos, this);
            }
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
                if (blockstate.isRedstoneConductor(level, blockpos) && !level.getBlockState(blockpos1).isRedstoneConductor(level, blockpos1)) {
                    BlockPos blockPosAbove = blockpos.above();
                    BlockState blockStateAbove = level.getBlockState(blockPosAbove);
                    j = Math.max(j, this.getWireSignalRed(blockStateAbove, level, blockPosAbove));
                } else if (!blockstate.isRedstoneConductor(level, blockpos)) {
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
                if (blockState.isRedstoneConductor(level, blockPos) && !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                    BlockPos blockPosAbove = blockPos.above();
                    BlockState blockStateAbove = level.getBlockState(blockPosAbove);
                    j = Math.max(j, this.getWireSignalBlue(blockStateAbove, level, blockPosAbove));
                    continue;
                }
                if (blockState.isRedstoneConductor(level, blockPos)) continue;
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
                if (blockState.isRedstoneConductor(level, blockPos) && !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                    BlockPos blockPosAbove = blockPos.above();
                    BlockState blockStateAbove = level.getBlockState(blockPosAbove);
                    j = Math.max(j, this.getWireSignalGreen(blockStateAbove, level, blockPosAbove));
                    continue;
                }
                if (blockState.isRedstoneConductor(level, blockPos)) continue;
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
                if (blockState.isRedstoneConductor(level, blockPos) && !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                    BlockPos blockPosAbove = blockPos.above();
                    BlockState blockStateAbove = level.getBlockState(blockPosAbove);
                    j = Math.max(j, this.getWireSignalYellow(blockStateAbove, level, blockPosAbove));
                    continue;
                }
                if (blockState.isRedstoneConductor(level, blockPos)) continue;
                BlockPos blockPosBelow = blockPos.below();
                BlockState blockStateBelow = level.getBlockState(blockPosBelow);
                j = Math.max(j, this.getWireSignalYellow(blockStateBelow, level, blockPosBelow));
            }
        }
        return Math.max(i, j - 1);
    }

    private int getWireSignalRed(BlockState state, Level level, BlockPos pos) {
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return state.getValue(RedStoneWireBlock.POWER);
        }
        if (state.is(this) && state.getValue(POWER_PAIR) == PowerPair.RED_BLUE) {
            return getPowerA(level, pos);
        }
        return 0;
    }

    private int getWireSignalBlue(BlockState state, Level level, BlockPos pos) {
        if (state.is(PowerStones.BLUESTONE_WIRE.get())) {
            return state.getValue(PowerstoneWireBlock.POWER);
        }
        if (state.is(this) && state.getValue(POWER_PAIR) == PowerPair.RED_BLUE) {
            return getPowerB(level, pos);
        }
        return 0;
    }

    private int getWireSignalGreen(BlockState state, Level level, BlockPos pos) {
        if (state.is(PowerStones.GREENSTONE_WIRE.get())) {
            return state.getValue(PowerstoneWireBlock.POWER);
        }
        if (state.is(this) && state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            return getPowerA(level, pos);
        }
        return 0;
    }

    private int getWireSignalYellow(BlockState state, Level level, BlockPos pos) {
        if (state.is(PowerStones.YELLOWSTONE_WIRE.get())) {
            return state.getValue(PowerstoneWireBlock.POWER);
        }
        if (state.is(this) && state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            return getPowerB(level, pos);
        }
        return 0;
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

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (! shouldSignal || direction == Direction.DOWN) {
            return 0;
        }
        if (state.getValue(POWER_PAIR) != PowerPair.RED_BLUE) {
            return 0;
        }
        int i = getPowerA((Level) level, pos);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((RedstoneSide)this.getConnectionState(level, state, pos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    public int getSignalBlue(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (! shouldSignal || direction == Direction.DOWN) {
            return 0;
        }
        if (state.getValue(POWER_PAIR) != PowerPair.RED_BLUE) {
            return 0;
        }
        int i = getPowerB((Level) level, pos);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((RedstoneSide)this.getConnectionState(level, state, pos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    public int getSignalGreen(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (! shouldSignal || direction == Direction.DOWN) {
            return 0;
        }
        if (state.getValue(POWER_PAIR) != PowerPair.GREEN_YELLOW) {
            return 0;
        }
        int i = getPowerA((Level) level, pos);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((RedstoneSide)this.getConnectionState(level, state, pos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    @Override
    public int getSignalYellow(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (! shouldSignal || direction == Direction.DOWN) {
            return 0;
        }
        if (state.getValue(POWER_PAIR) != PowerPair.GREEN_YELLOW) {
            return 0;
        }
        int i = getPowerB((Level) level, pos);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((RedstoneSide)this.getConnectionState(level, state, pos).getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
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

    protected boolean shouldConnectTo(BlockState multipleWiresState, BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        if (state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return multipleWiresState.getValue(POWER_PAIR) == state.getValue(POWER_PAIR);
        }
        else if (state.is(Blocks.REDSTONE_WIRE) || state.is(PowerStones.BLUESTONE_WIRE.get())) {
            return multipleWiresState.getValue(POWER_PAIR) == PowerPair.RED_BLUE;
        }
        else if (state.is(PowerStones.GREENSTONE_WIRE.get()) || state.is(PowerStones.YELLOWSTONE_WIRE.get())) {
            return multipleWiresState.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW;
        }
        else if (direction != null
         && (state.is(Blocks.REDSTONE_TORCH) || state.is(Blocks.REDSTONE_WALL_TORCH)
         || state.is(PowerStones.BLUESTONE_TORCH_BLOCK.get()) || state.is(PowerStones.BLUESTONE_WALL_TORCH.get())
         || state.is(Blocks.REDSTONE_BLOCK) || state.is(PowerStones.BLUESTONE_BLOCK.get()))) {
            return multipleWiresState.getValue(POWER_PAIR) == PowerPair.RED_BLUE;
        }
        else if (direction != null
         && (state.is(PowerStones.GREENSTONE_TORCH_BLOCK.get()) || state.is(PowerStones.GREENSTONE_WALL_TORCH.get())
         || state.is(PowerStones.YELLOWSTONE_TORCH_BLOCK.get()) || state.is(PowerStones.YELLOWSTONE_WALL_TORCH.get())
         || state.is(PowerStones.GREENSTONE_BLOCK.get()) || state.is(PowerStones.YELLOWSTONE_BLOCK.get()))) {
            return multipleWiresState.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW;
        }
        else {
            return state.canRedstoneConnectTo(level, pos, direction);
        }
    }

    public static int getColorForTintIndex(BlockState state, BlockGetter level, BlockPos pos, int tintIndex) {
        if (state.getValue(POWER_PAIR) == PowerPair.RED_BLUE && tintIndex == 0) {
			return PowerstoneWireBlock.getWireColorRed(getPowerA(level, pos));
		}
		else if (state.getValue(POWER_PAIR) == PowerPair.RED_BLUE && tintIndex == 1) {
			return PowerstoneWireBlock.getWireColorBlue(getPowerB(level, pos));
		}
		else if (state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW && tintIndex == 2) {
			return PowerstoneWireBlock.getWireColorGreen(getPowerA(level, pos));
		}
		else if (state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW && tintIndex == 3) {
			return PowerstoneWireBlock.getWireColorYellow(getPowerB(level, pos));
		}
		else {
			return PowerstoneWireBlock.getWireColorWhite();
		}
    }

    @Override
    protected boolean hasPowerOn(BlockState state, Level level, BlockPos pos) {
        return getPowerA(level, pos) > 0 && getPowerB(level, pos) > 0;
    }

    @Override
    protected Vec3 getPowerstoneColor(BlockState state, Level level, BlockPos pos, RandomSource random) {
        List<Vec3[]> colorsList = new ArrayList<>();
        List<Integer> powerList = new ArrayList<>();
        int powerA = getPowerA(level, pos);
        int powerB = getPowerB(level, pos);
        if (state.getValue(POWER_PAIR) == PowerPair.RED_BLUE) {
            if (powerA > 0) {
                colorsList.add(PowerstoneWireBlock.RED_COLORS);
                powerList.add(powerA);
            }
            if (powerB > 0) {
                colorsList.add(PowerstoneWireBlock.BLUE_COLORS);
                powerList.add(powerB);
            }
        }
        if (state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
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

    public static boolean canBreakFromHeldItem(BlockState state, ItemStack heldItemStack) {
        if ((state.is(PowerStones.MULTIPLE_WIRES.get()) && state.getValue(POWER_PAIR) == PowerPair.RED_BLUE && (heldItemStack.is(PowerStones.GREENSTONE.get()) || heldItemStack.is(PowerStones.YELLOWSTONE.get())))
         || (state.is(PowerStones.MULTIPLE_WIRES.get()) && state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW && (heldItemStack.is(Items.REDSTONE) || heldItemStack.is(PowerStones.BLUESTONE.get())))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (! canBreakFromHeldItem(state, player.getMainHandItem())) {
            return false;
        }
        boolean result = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (result) {
            if (state.getValue(POWER_PAIR) == PowerPair.RED_BLUE) {
                if (player.getMainHandItem().is(Items.REDSTONE)) {
                    state = PowerStones.BLUESTONE_WIRE.get().defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(RedStoneWireBlock.POWER, getPowerB(level, pos));
                    level.setBlock(pos, state, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, level, pos);
                }
                else if (player.getMainHandItem().is(PowerStones.BLUESTONE.get())) {
                    state = Blocks.REDSTONE_WIRE.defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(RedStoneWireBlock.POWER, getPowerA(level, pos));
                    level.setBlock(pos, state, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
                    ((RedstoneWireBlockInterface)state.getBlock()).updateAll(state, level, pos);
                }
            }
            else if (state.getValue(POWER_PAIR) == PowerPair.GREEN_YELLOW) {
                if (player.getMainHandItem().is(PowerStones.GREENSTONE.get())) {
                    state = PowerStones.YELLOWSTONE_WIRE.get().defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(RedStoneWireBlock.POWER, getPowerB(level, pos));
                    level.setBlock(pos, state, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, level, pos);
                }
                else if (player.getMainHandItem().is(PowerStones.YELLOWSTONE.get())) {
                    state = PowerStones.GREENSTONE_WIRE.get().defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(RedStoneWireBlock.POWER, getPowerA(level, pos));
                    level.setBlock(pos, state, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
                    ((PowerstoneWireBlock)state.getBlock()).updateAll(state, level, pos);
                }
            }
        }
        return result;
    }

}

