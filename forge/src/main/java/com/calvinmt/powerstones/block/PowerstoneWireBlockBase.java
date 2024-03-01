package com.calvinmt.powerstones.block;

import java.util.Map;
import java.util.Random;

import com.calvinmt.powerstones.BlockBehaviourInterface;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.math.Vector3f;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class PowerstoneWireBlockBase extends Block implements BlockBehaviourInterface {
    
    public static final EnumProperty<RedstoneSide> NORTH = BlockStateProperties.NORTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> EAST = BlockStateProperties.EAST_REDSTONE;
    public static final EnumProperty<RedstoneSide> SOUTH = BlockStateProperties.SOUTH_REDSTONE;
    public static final EnumProperty<RedstoneSide> WEST = BlockStateProperties.WEST_REDSTONE;
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST));
    protected static final int H = 1;
    protected static final int W = 3;
    protected static final int E = 13;
    protected static final int N = 3;
    protected static final int S = 13;
    private static final VoxelShape SHAPE_DOT = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.box(3.0D, 0.0D, 0.0D, 13.0D, 1.0D, 13.0D), Direction.SOUTH, Block.box(3.0D, 0.0D, 3.0D, 13.0D, 1.0D, 16.0D), Direction.EAST, Block.box(3.0D, 0.0D, 3.0D, 16.0D, 1.0D, 13.0D), Direction.WEST, Block.box(0.0D, 0.0D, 3.0D, 13.0D, 1.0D, 13.0D)));
    private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Shapes.or(SHAPES_FLOOR.get(Direction.NORTH), Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 1.0D)), Direction.SOUTH, Shapes.or(SHAPES_FLOOR.get(Direction.SOUTH), Block.box(3.0D, 0.0D, 15.0D, 13.0D, 16.0D, 16.0D)), Direction.EAST, Shapes.or(SHAPES_FLOOR.get(Direction.EAST), Block.box(15.0D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D)), Direction.WEST, Shapes.or(SHAPES_FLOOR.get(Direction.WEST), Block.box(0.0D, 0.0D, 3.0D, 1.0D, 16.0D, 13.0D))));
    private static final Map<BlockState, VoxelShape> SHAPES_CACHE = Maps.newHashMap();
    protected final BlockState crossState;
    protected boolean shouldSignal = true;

    public PowerstoneWireBlockBase(Properties properties) {
        super(properties);
        this.crossState = this.defaultBlockState().setValue(NORTH, RedstoneSide.SIDE).setValue(EAST, RedstoneSide.SIDE).setValue(SOUTH, RedstoneSide.SIDE).setValue(WEST, RedstoneSide.SIDE);

        for(BlockState blockstate : this.getStateDefinition().getPossibleStates()) {
            if (blockstate.getValue(POWER) == 0) {
                SHAPES_CACHE.put(blockstate, this.calculateShape(blockstate));
            }
        }
    }

    private VoxelShape calculateShape(BlockState state) {
        VoxelShape voxelshape = SHAPE_DOT;

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = state.getValue(PROPERTY_BY_DIRECTION.get(direction));
            if (redstoneside == RedstoneSide.SIDE) {
                voxelshape = Shapes.or(voxelshape, SHAPES_FLOOR.get(direction));
            } else if (redstoneside == RedstoneSide.UP) {
                voxelshape = Shapes.or(voxelshape, SHAPES_UP.get(direction));
            }
        }

        return voxelshape;
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES_CACHE.get(state.setValue(POWER, Integer.valueOf(0)));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.getConnectionState(context.getLevel(), this.crossState, context.getClickedPos());
    }

    protected BlockState getConnectionState(BlockGetter level, BlockState state, BlockPos pos) {
        boolean flag = isDot(state);
        state = this.getMissingConnections(level, this.getDefaultBlockStateWithPowerProperties(state), pos);
        if (flag && isDot(state)) {
           return state;
        } else {
           boolean flag1 = state.getValue(NORTH).isConnected();
           boolean flag2 = state.getValue(SOUTH).isConnected();
           boolean flag3 = state.getValue(EAST).isConnected();
           boolean flag4 = state.getValue(WEST).isConnected();
           boolean flag5 = !flag1 && !flag2;
           boolean flag6 = !flag3 && !flag4;
           if (!flag4 && flag5) {
              state = state.setValue(WEST, RedstoneSide.SIDE);
           }
  
           if (!flag3 && flag5) {
              state = state.setValue(EAST, RedstoneSide.SIDE);
           }
  
           if (!flag1 && flag6) {
              state = state.setValue(NORTH, RedstoneSide.SIDE);
           }
  
           if (!flag2 && flag6) {
              state = state.setValue(SOUTH, RedstoneSide.SIDE);
           }
  
           return state;
        }
    }

    protected abstract BlockState getDefaultBlockStateWithPowerProperties(BlockState state);

    protected BlockState getMissingConnections(BlockGetter level, BlockState state, BlockPos pos) {
        boolean flag = !level.getBlockState(pos.above()).isRedstoneConductor(level, pos);

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            if (!state.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected()) {
                RedstoneSide redstoneside = this.getConnectingSide(level, pos, direction, flag);
                state = state.setValue(PROPERTY_BY_DIRECTION.get(direction), redstoneside);
            }
        }

        return state;
    }

    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (facing == Direction.DOWN) {
            return state;
        } else if (facing == Direction.UP) {
            return this.getConnectionState(level, state, currentPos);
        } else {
            RedstoneSide redstoneside = this.getConnectingSide(level, currentPos, facing);
            return redstoneside.isConnected() == state.getValue(PROPERTY_BY_DIRECTION.get(facing)).isConnected() && !isCross(state) ? state.setValue(PROPERTY_BY_DIRECTION.get(facing), redstoneside) : this.getConnectionState(level, this.getCrossStateWithPowerProperties(state).setValue(PROPERTY_BY_DIRECTION.get(facing), redstoneside), currentPos);
        }
    }

    protected abstract BlockState getCrossStateWithPowerProperties(BlockState state);

    protected static boolean isCross(BlockState state) {
        return state.getValue(NORTH).isConnected() && state.getValue(SOUTH).isConnected() && state.getValue(EAST).isConnected() && state.getValue(WEST).isConnected();
    }

    protected static boolean isDot(BlockState state) {
        return !state.getValue(NORTH).isConnected() && !state.getValue(SOUTH).isConnected() && !state.getValue(EAST).isConnected() && !state.getValue(WEST).isConnected();
    }

    public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int pFlags, int pRecursionLeft) {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = state.getValue(PROPERTY_BY_DIRECTION.get(direction));
            blockpos$mutableblockpos.setWithOffset(pos, direction);
            if (redstoneside != RedstoneSide.NONE
             && (! level.getBlockState(blockpos$mutableblockpos).is(this) || ! this.isOtherConnectablePowerstone(level.getBlockState(blockpos$mutableblockpos)))) {
                blockpos$mutableblockpos.move(Direction.DOWN);
                BlockState blockstate = level.getBlockState(blockpos$mutableblockpos);
                if (blockstate.is(this) || this.isOtherConnectablePowerstone(blockstate)) {
                    BlockPos blockpos = blockpos$mutableblockpos.relative(direction.getOpposite());
                    BlockState newBlockstate = blockstate.updateShape(direction.getOpposite(), level.getBlockState(blockpos), level, blockpos$mutableblockpos, blockpos);
                    updateOrDestroy(blockstate, newBlockstate, level, blockpos$mutableblockpos, pFlags, pRecursionLeft);
                }

                blockpos$mutableblockpos.setWithOffset(pos, direction).move(Direction.UP);
                BlockState blockstate1 = level.getBlockState(blockpos$mutableblockpos);
                if (blockstate1.is(this) || this.isOtherConnectablePowerstone(blockstate1)) {
                    BlockPos blockpos1 = blockpos$mutableblockpos.relative(direction.getOpposite());
                    BlockState newBlockstate1 = blockstate1.updateShape(direction.getOpposite(), level.getBlockState(blockpos1), level, blockpos$mutableblockpos, blockpos1);
                    updateOrDestroy(blockstate1, newBlockstate1, level, blockpos$mutableblockpos, pFlags, pRecursionLeft);
                }
            }
        }
    }

    protected abstract boolean isOtherConnectablePowerstone(BlockState state);

    protected RedstoneSide getConnectingSide(BlockGetter level, BlockPos pos, Direction pFace) {
        return this.getConnectingSide(level, pos, pFace, !level.getBlockState(pos.above()).isRedstoneConductor(level, pos));
    }

    protected RedstoneSide getConnectingSide(BlockGetter level, BlockPos pos, Direction direction, boolean nonNormalCubeAbove) {
        BlockPos blockpos = pos.relative(direction);
        BlockState blockstate = level.getBlockState(blockpos);
        if (nonNormalCubeAbove) {
           boolean flag = this.canSurviveOn(level, blockpos, blockstate);
           BlockPos posAbove = blockpos.above();
           BlockState stateAbove = level.getBlockState(posAbove);
           if (flag && (this.shouldConnectToAbove(level, posAbove, stateAbove, direction))) {
              if (blockstate.isFaceSturdy(level, blockpos, direction.getOpposite())) {
                 return RedstoneSide.UP;
              }
              return RedstoneSide.SIDE;
           }
        }

        if (this.shouldConnectTo(level, blockpos, blockstate, direction)) {
            return RedstoneSide.SIDE;
        } else if (blockstate.isRedstoneConductor(level, blockpos)) {
            return RedstoneSide.NONE;
        } else {
            BlockPos posBelow = blockpos.below();
            BlockState stateBelow = level.getBlockState(posBelow);
            return (this.shouldConnectToBelow(level, posBelow, stateBelow, direction)) ? RedstoneSide.SIDE : RedstoneSide.NONE;
        }
    }

    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos blockpos = pos.below();
        BlockState blockstate = level.getBlockState(blockpos);
        return this.canSurviveOn(level, blockpos, blockstate);
    }

    protected boolean canSurviveOn(BlockGetter reader, BlockPos pos, BlockState state) {
        return state.isFaceSturdy(reader, pos, Direction.UP) || state.is(Blocks.HOPPER);
    }

    protected abstract void updatePowerStrength(Level level, BlockPos pos, BlockState state);

    public void setShouldSignal(boolean shouldSignal) {
        this.shouldSignal = shouldSignal;
    }

    private void checkCornerChangeAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.is(this)) {
            level.updateNeighborsAt(pos, this);

            for(Direction direction : Direction.values()) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }

        }
    }

    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!oldState.is(state.getBlock()) && !level.isClientSide) {
            this.updatePowerStrength(level, pos, state);

            for(Direction direction : Direction.Plane.VERTICAL) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }

            this.updateNeighborsOfNeighboringWires(level, pos);
        }
    }

    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && !state.is(newState.getBlock())) {
            super.onRemove(state, level, pos, newState, isMoving);
            if (!level.isClientSide) {
                for(Direction direction : Direction.values()) {
                    level.updateNeighborsAt(pos.relative(direction), this);
                }

                this.updatePowerStrength(level, pos, state);
                this.updateNeighborsOfNeighboringWires(level, pos);
            }
        }
    }

    private void updateNeighborsOfNeighboringWires(Level level, BlockPos pos) {
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            this.checkCornerChangeAt(level, pos.relative(direction));
        }

        for(Direction direction1 : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction1);
            if (level.getBlockState(blockpos).isRedstoneConductor(level, blockpos)) {
                this.checkCornerChangeAt(level, blockpos.above());
            } else {
                this.checkCornerChangeAt(level, blockpos.below());
            }
        }
    }

    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block pBlock, BlockPos pFromPos, boolean pIsMoving) {
        if (!level.isClientSide) {
            if (state.canSurvive(level, pos)) {
                this.updatePowerStrength(level, pos, state);
            } else {
                dropResources(state, level, pos);
                level.removeBlock(pos, false);
            }

        }
    }

    protected abstract boolean shouldConnectToAbove(BlockGetter level, BlockPos posAbove, BlockState stateAbove, Direction direction);

    protected abstract boolean shouldConnectToBelow(BlockGetter level, BlockPos posBelow, BlockState stateBelow, Direction direction);

    protected abstract boolean shouldConnectTo(BlockGetter level, BlockPos pos, BlockState state, Direction direction);

    public boolean isSignalSource(BlockState state) {
        return shouldSignal;
    }

    private void spawnParticlesAlongLine(Level level, Random random, BlockPos pos, Vec3 particleVec, Direction xDirection, Direction zDirection, float min, float max) {
        float f = max - min;
        if (!(random.nextFloat() >= 0.2F * f)) {
            float f2 = min + f * random.nextFloat();
            double d0 = 0.5D + (double)(0.4375F * (float)xDirection.getStepX()) + (double)(f2 * (float)zDirection.getStepX());
            double d1 = 0.5D + (double)(0.4375F * (float)xDirection.getStepY()) + (double)(f2 * (float)zDirection.getStepY());
            double d2 = 0.5D + (double)(0.4375F * (float)xDirection.getStepZ()) + (double)(f2 * (float)zDirection.getStepZ());
            level.addParticle(new DustParticleOptions(new Vector3f(particleVec), 1.0F), (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, 0.0D, 0.0D, 0.0D);
        }
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, Random random) {
        if (this.hasPowerOn(state)) {
            for(Direction direction : Direction.Plane.HORIZONTAL) {
                RedstoneSide redstoneside = state.getValue(PROPERTY_BY_DIRECTION.get(direction));
                switch (redstoneside) {
                case UP:
                    this.spawnParticlesAlongLine(level, random, pos, this.getPowerstoneColor(state, random), direction, Direction.UP, -0.5F, 0.5F);
                case SIDE:
                    this.spawnParticlesAlongLine(level, random, pos, this.getPowerstoneColor(state, random), Direction.DOWN, direction, 0.0F, 0.5F);
                    break;
                case NONE:
                default:
                    this.spawnParticlesAlongLine(level, random, pos, this.getPowerstoneColor(state, random), Direction.DOWN, direction, 0.0F, 0.3F);
                }
            }

        }
    }

    protected abstract boolean hasPowerOn(BlockState state);

    protected abstract Vec3 getPowerstoneColor(BlockState state, Random random);

    public BlockState rotate(BlockState state, Rotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                return state.setValue(NORTH, state.getValue(SOUTH)).setValue(EAST, state.getValue(WEST)).setValue(SOUTH, state.getValue(NORTH)).setValue(WEST, state.getValue(EAST));
            case COUNTERCLOCKWISE_90:
                return state.setValue(NORTH, state.getValue(EAST)).setValue(EAST, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(WEST)).setValue(WEST, state.getValue(NORTH));
            case CLOCKWISE_90:
                return state.setValue(NORTH, state.getValue(WEST)).setValue(EAST, state.getValue(NORTH)).setValue(SOUTH, state.getValue(EAST)).setValue(WEST, state.getValue(SOUTH));
            default:
                return state;
        }
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return state.setValue(NORTH, state.getValue(SOUTH)).setValue(SOUTH, state.getValue(NORTH));
            case FRONT_BACK:
                return state.setValue(EAST, state.getValue(WEST)).setValue(WEST, state.getValue(EAST));
            default:
                return super.mirror(state, mirror);
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, SOUTH, WEST, POWER);
    }

    public void updateAll(BlockState state, Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), this);
        }
        state = this.getConnectionState(level, state, pos);
        this.updatePowerStrength(level, pos, state);
        this.updateNeighborsOfNeighboringWires(level, pos);
        state = this.getConnectionState(level, level.getBlockState(pos), pos);
        level.setBlock(pos, state, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) {
           return InteractionResult.PASS;
        } else {
            if (isCross(state) || isDot(state)) {
                BlockState blockstate = isCross(state) ? this.defaultBlockState() : this.crossState;
                blockstate = blockstate.setValue(POWER, state.getValue(POWER));
                blockstate = this.getConnectionState(level, blockstate, pos);
                if (blockstate != state) {
                    level.setBlockAndUpdate(pos, blockstate);
                    this.updatesOnShapeChange(level, pos, state, blockstate);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }
    }

    protected void updatesOnShapeChange(Level level, BlockPos pos, BlockState pOldState, BlockState pNewState) {
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockpos = pos.relative(direction);
            if (pOldState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() != pNewState.getValue(PROPERTY_BY_DIRECTION.get(direction)).isConnected() && level.getBlockState(blockpos).isRedstoneConductor(level, blockpos)) {
                level.updateNeighborsAtExceptFromFacing(blockpos, pNewState.getBlock(), direction.getOpposite());
            }
        }
    }
    
}
