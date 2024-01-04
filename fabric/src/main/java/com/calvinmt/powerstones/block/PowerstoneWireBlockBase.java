package com.calvinmt.powerstones.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class PowerstoneWireBlockBase extends Block {

    public static final EnumProperty<WireConnection> WIRE_CONNECTION_NORTH = Properties.NORTH_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_EAST = Properties.EAST_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_SOUTH = Properties.SOUTH_WIRE_CONNECTION;
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_WEST = Properties.WEST_WIRE_CONNECTION;
    public static final IntProperty POWER = Properties.POWER;
    public static final Map<Direction, EnumProperty<WireConnection>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, WIRE_CONNECTION_NORTH, Direction.EAST, WIRE_CONNECTION_EAST, Direction.SOUTH, WIRE_CONNECTION_SOUTH, Direction.WEST, WIRE_CONNECTION_WEST));
    protected static final int H = 1;
    protected static final int W = 3;
    protected static final int E = 13;
    protected static final int N = 3;
    protected static final int S = 13;
    private static final VoxelShape DOT_SHAPE = Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 1.0, 13.0);
    private static final Map<Direction, VoxelShape> SHAPES_FLOOR = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, Block.createCuboidShape(3.0, 0.0, 0.0, 13.0, 1.0, 13.0), Direction.SOUTH, Block.createCuboidShape(3.0, 0.0, 3.0, 13.0, 1.0, 16.0), Direction.EAST, Block.createCuboidShape(3.0, 0.0, 3.0, 16.0, 1.0, 13.0), Direction.WEST, Block.createCuboidShape(0.0, 0.0, 3.0, 13.0, 1.0, 13.0)));
    private static final Map<Direction, VoxelShape> SHAPES_UP = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, VoxelShapes.union((VoxelShape)SHAPES_FLOOR.get(Direction.NORTH), Block.createCuboidShape(3.0, 0.0, 0.0, 13.0, 16.0, 1.0)), Direction.SOUTH, VoxelShapes.union((VoxelShape)SHAPES_FLOOR.get(Direction.SOUTH), Block.createCuboidShape(3.0, 0.0, 15.0, 13.0, 16.0, 16.0)), Direction.EAST, VoxelShapes.union((VoxelShape)SHAPES_FLOOR.get(Direction.EAST), Block.createCuboidShape(15.0, 0.0, 3.0, 16.0, 16.0, 13.0)), Direction.WEST, VoxelShapes.union((VoxelShape)SHAPES_FLOOR.get(Direction.WEST), Block.createCuboidShape(0.0, 0.0, 3.0, 1.0, 16.0, 13.0))));
    private static final Map<BlockState, VoxelShape> SHAPES = Maps.newHashMap();
    protected final BlockState dotState;
    protected boolean wiresGivePower = true;

    public PowerstoneWireBlockBase(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(WIRE_CONNECTION_NORTH, WireConnection.NONE)).with(WIRE_CONNECTION_EAST, WireConnection.NONE)).with(WIRE_CONNECTION_SOUTH, WireConnection.NONE)).with(WIRE_CONNECTION_WEST, WireConnection.NONE)).with(POWER, 0));
        this.dotState = (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(WIRE_CONNECTION_NORTH, WireConnection.SIDE)).with(WIRE_CONNECTION_EAST, WireConnection.SIDE)).with(WIRE_CONNECTION_SOUTH, WireConnection.SIDE)).with(WIRE_CONNECTION_WEST, WireConnection.SIDE);

        for(BlockState blockState : this.getStateManager().getStates()) {
            if ((Integer)blockState.get(POWER) == 0) {
                SHAPES.put(blockState, this.getShapeForState(blockState));
            }
        }

    }

    private VoxelShape getShapeForState(BlockState state) {
        VoxelShape voxelShape = DOT_SHAPE;

        for(Direction direction : Type.HORIZONTAL) {
            WireConnection wireConnection = (WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
            if (wireConnection == WireConnection.SIDE) {
                voxelShape = VoxelShapes.union(voxelShape, (VoxelShape)SHAPES_FLOOR.get(direction));
            } else if (wireConnection == WireConnection.UP) {
                voxelShape = VoxelShapes.union(voxelShape, (VoxelShape)SHAPES_UP.get(direction));
            }
        }

        return voxelShape;
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return (VoxelShape)SHAPES.get(state.with(POWER, 0));
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getPlacementState(ctx.getWorld(), this.dotState, ctx.getBlockPos());
    }

    protected BlockState getPlacementState(BlockView world, BlockState state, BlockPos pos) {
        boolean bl = isNotConnected(state);
        state = this.getDefaultWireState(world, this.getDefaultBlockStateWithPowerProperties(state), pos);
        if (bl && isNotConnected(state)) {
            return state;
        } else {
            boolean bl2 = ((WireConnection)state.get(WIRE_CONNECTION_NORTH)).isConnected();
            boolean bl3 = ((WireConnection)state.get(WIRE_CONNECTION_SOUTH)).isConnected();
            boolean bl4 = ((WireConnection)state.get(WIRE_CONNECTION_EAST)).isConnected();
            boolean bl5 = ((WireConnection)state.get(WIRE_CONNECTION_WEST)).isConnected();
            boolean bl6 = !bl2 && !bl3;
            boolean bl7 = !bl4 && !bl5;
            if (!bl5 && bl6) {
                state = (BlockState)state.with(WIRE_CONNECTION_WEST, WireConnection.SIDE);
            }

            if (!bl4 && bl6) {
                state = (BlockState)state.with(WIRE_CONNECTION_EAST, WireConnection.SIDE);
            }

            if (!bl2 && bl7) {
                state = (BlockState)state.with(WIRE_CONNECTION_NORTH, WireConnection.SIDE);
            }

            if (!bl3 && bl7) {
                state = (BlockState)state.with(WIRE_CONNECTION_SOUTH, WireConnection.SIDE);
            }

            return state;
        }
    }

    protected abstract BlockState getDefaultBlockStateWithPowerProperties(BlockState state);

    private BlockState getDefaultWireState(BlockView world, BlockState state, BlockPos pos) {
        boolean bl = !world.getBlockState(pos.up()).isSolidBlock(world, pos);

        for(Direction direction : Type.HORIZONTAL) {
            if (!((WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction))).isConnected()) {
                WireConnection wireConnection = this.getRenderConnectionType(world, pos, direction, bl);
                state = (BlockState)state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), wireConnection);
            }
        }

        return state;
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return state;
        } else if (direction == Direction.UP) {
            return this.getPlacementState(world, state, pos);
        } else {
            WireConnection wireConnection = this.getRenderConnectionType(world, pos, direction);
            return wireConnection.isConnected() == ((WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction))).isConnected() && !isFullyConnected(state) ? (BlockState)state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), wireConnection) : this.getPlacementState(world, this.getCrossStateWithPowerProperties(state).with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), wireConnection), pos);
        }
    }

    protected abstract BlockState getCrossStateWithPowerProperties(BlockState state);

    protected static boolean isFullyConnected(BlockState state) {
        return ((WireConnection)state.get(WIRE_CONNECTION_NORTH)).isConnected() && ((WireConnection)state.get(WIRE_CONNECTION_SOUTH)).isConnected() && ((WireConnection)state.get(WIRE_CONNECTION_EAST)).isConnected() && ((WireConnection)state.get(WIRE_CONNECTION_WEST)).isConnected();
    }

    protected static boolean isNotConnected(BlockState state) {
        return !((WireConnection)state.get(WIRE_CONNECTION_NORTH)).isConnected() && !((WireConnection)state.get(WIRE_CONNECTION_SOUTH)).isConnected() && !((WireConnection)state.get(WIRE_CONNECTION_EAST)).isConnected() && !((WireConnection)state.get(WIRE_CONNECTION_WEST)).isConnected();
    }

    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int flags, int maxUpdateDepth) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(Direction direction : Type.HORIZONTAL) {
            WireConnection wireConnection = (WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
            if (wireConnection != WireConnection.NONE
             && (! world.getBlockState(mutable.set(pos, direction)).isOf(this) || ! this.isOtherConnectablePowerstone(world.getBlockState(mutable.set(pos, direction))))) {
                mutable.move(Direction.DOWN);
                BlockState blockState = world.getBlockState(mutable);
                if (blockState.isOf(this) || this.isOtherConnectablePowerstone(blockState)) {
                    BlockPos blockPos = mutable.offset(direction.getOpposite());
                    BlockState blockState2 = blockState.getStateForNeighborUpdate(direction.getOpposite(), world.getBlockState(blockPos), world, mutable, blockPos);
                    replace(blockState, blockState2, world, mutable, flags, maxUpdateDepth);
                }

                mutable.set(pos, direction).move(Direction.UP);
                BlockState blockState3 = world.getBlockState(mutable);
                if (blockState3.isOf(this) || this.isOtherConnectablePowerstone(blockState3)) {
                    BlockPos blockPos2 = mutable.offset(direction.getOpposite());
                    BlockState blockState4 = blockState3.getStateForNeighborUpdate(direction.getOpposite(), world.getBlockState(blockPos2), world, mutable, blockPos2);
                    replace(blockState3, blockState4, world, mutable, flags, maxUpdateDepth);
                }
            }
        }
    }

    protected abstract boolean isOtherConnectablePowerstone(BlockState state);

    private WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction) {
        return this.getRenderConnectionType(world, pos, direction, !world.getBlockState(pos.up()).isSolidBlock(world, pos));
    }

    private WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction, boolean nonNormalCubeAbove) {
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        if (nonNormalCubeAbove) {
            boolean flag = this.canRunOnTop(world, blockPos, blockState);
            BlockPos posAbove = blockPos.up();
            BlockState stateAbove = world.getBlockState(posAbove);
            if (flag && this.shouldConnectToAbove(world, posAbove, stateAbove, direction)) {
                if (blockState.isSideSolidFullSquare(world, blockPos, direction.getOpposite())) {
                    return WireConnection.UP;
                }

                return WireConnection.SIDE;
            }
        }

        if (this.shouldConnectTo(world, blockPos, blockState, direction)) {
            return WireConnection.SIDE;
        } else if (blockState.isSolidBlock(world, blockPos)) {
            return WireConnection.NONE;
        } else {
            BlockPos posBelow = blockPos.down();
            BlockState stateBelow = world.getBlockState(posBelow);
            return (this.shouldConnectToBelow(world, posBelow, stateBelow, direction)) ? WireConnection.SIDE : WireConnection.NONE;
        }
    }

    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        return this.canRunOnTop(world, blockPos, blockState);
    }

    private boolean canRunOnTop(BlockView world, BlockPos pos, BlockState floor) {
        return floor.isSideSolidFullSquare(world, pos, Direction.UP) || floor.isOf(Blocks.HOPPER);
    }

    protected abstract void updatePowerStrength(World world, BlockPos pos, BlockState state);

    public void setShouldSignal(boolean wiresGivePower) {
        this.wiresGivePower = wiresGivePower;
    }

    private void updateNeighbors(World world, BlockPos pos) {
        if (world.getBlockState(pos).isOf(this)) {
            world.updateNeighborsAlways(pos, this);
            Direction[] var3 = Direction.values();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Direction direction = var3[var5];
                world.updateNeighborsAlways(pos.offset(direction), this);
            }

        }
    }

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock()) && !world.isClient) {
            this.updatePowerStrength(world, pos, state);

            for(Direction direction : Type.VERTICAL) {
                world.updateNeighborsAlways(pos.offset(direction), this);
            }

            this.updateOffsetNeighbors(world, pos);
        }
    }

    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!moved && !state.isOf(newState.getBlock())) {
            super.onStateReplaced(state, world, pos, newState, moved);
            if (!world.isClient) {
                Direction[] var6 = Direction.values();
                int var7 = var6.length;

                for(int var8 = 0; var8 < var7; ++var8) {
                    Direction direction = var6[var8];
                    world.updateNeighborsAlways(pos.offset(direction), this);
                }

                this.updatePowerStrength(world, pos, state);
                this.updateOffsetNeighbors(world, pos);
            }
        }
    }

    private void updateOffsetNeighbors(World world, BlockPos pos) {

        for(Direction direction : Type.HORIZONTAL) {
            this.updateNeighbors(world, pos.offset(direction));
        }

        for(Direction direction : Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            if (world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                this.updateNeighbors(world, blockPos.up());
            } else {
                this.updateNeighbors(world, blockPos.down());
            }
        }

    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            if (state.canPlaceAt(world, pos)) {
                this.updatePowerStrength(world, pos, state);
            } else {
                dropStacks(state, world, pos);
                world.removeBlock(pos, false);
            }

        }
    }

    protected boolean connectsTo(BlockState state, @Nullable Direction dir) {
       if (state.isOf(Blocks.REDSTONE_WIRE)) {
          return true;
       } else if (state.isOf(Blocks.REPEATER)) {
          Direction direction = (Direction)state.get(RepeaterBlock.FACING);
          return direction == dir || direction.getOpposite() == dir;
       } else if (state.isOf(Blocks.OBSERVER)) {
          return dir == state.get(ObserverBlock.FACING);
       } else {
          return state.emitsRedstonePower() && dir != null;
       }
    }

    protected abstract boolean shouldConnectToAbove(BlockView world, BlockPos posAbove, BlockState stateAbove, Direction direction);

    protected abstract boolean shouldConnectToBelow(BlockView world, BlockPos posBelow, BlockState stateBelow, Direction direction);

    protected abstract boolean shouldConnectTo(BlockView world, BlockPos pos, BlockState state, Direction direction);

    public boolean emitsRedstonePower(BlockState state) {
        return this.wiresGivePower;
    }

    private void addPoweredParticles(World world, Random random, BlockPos pos, Vec3d color, Direction direction, Direction direction2, float f, float g) {
        float h = g - f;
        if (!(random.nextFloat() >= 0.2F * h)) {
            float j = f + h * random.nextFloat();
            double d = 0.5 + (double)(0.4375F * (float)direction.getOffsetX()) + (double)(j * (float)direction2.getOffsetX());
            double e = 0.5 + (double)(0.4375F * (float)direction.getOffsetY()) + (double)(j * (float)direction2.getOffsetY());
            double k = 0.5 + (double)(0.4375F * (float)direction.getOffsetZ()) + (double)(j * (float)direction2.getOffsetZ());
            world.addParticle(new DustParticleEffect(color.toVector3f(), 1.0F), (double)pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + k, 0.0, 0.0, 0.0);
        }
    }

    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (this.hasPowerOn(state)) {

        for(Direction direction : Type.HORIZONTAL) {
                WireConnection wireConnection = (WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
                switch (wireConnection) {
                case UP:
                    this.addPoweredParticles(world, random, pos, this.getPowerstoneColor(state, random), direction, Direction.UP, -0.5F, 0.5F);
                case SIDE:
                    this.addPoweredParticles(world, random, pos, this.getPowerstoneColor(state, random), Direction.DOWN, direction, 0.0F, 0.5F);
                    break;
                case NONE:
                default:
                    this.addPoweredParticles(world, random, pos, this.getPowerstoneColor(state, random), Direction.DOWN, direction, 0.0F, 0.3F);
                }
            }

        }
    }

    protected abstract boolean hasPowerOn(BlockState state);

    protected abstract Vec3d getPowerstoneColor(BlockState state, Random random);

    public BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_90:
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(WIRE_CONNECTION_NORTH, (WireConnection)state.get(WIRE_CONNECTION_SOUTH))).with(WIRE_CONNECTION_EAST, (WireConnection)state.get(WIRE_CONNECTION_WEST))).with(WIRE_CONNECTION_SOUTH, (WireConnection)state.get(WIRE_CONNECTION_NORTH))).with(WIRE_CONNECTION_WEST, (WireConnection)state.get(WIRE_CONNECTION_EAST));
            case CLOCKWISE_180:
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(WIRE_CONNECTION_NORTH, (WireConnection)state.get(WIRE_CONNECTION_EAST))).with(WIRE_CONNECTION_EAST, (WireConnection)state.get(WIRE_CONNECTION_SOUTH))).with(WIRE_CONNECTION_SOUTH, (WireConnection)state.get(WIRE_CONNECTION_WEST))).with(WIRE_CONNECTION_WEST, (WireConnection)state.get(WIRE_CONNECTION_NORTH));
            case COUNTERCLOCKWISE_90:
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(WIRE_CONNECTION_NORTH, (WireConnection)state.get(WIRE_CONNECTION_WEST))).with(WIRE_CONNECTION_EAST, (WireConnection)state.get(WIRE_CONNECTION_NORTH))).with(WIRE_CONNECTION_SOUTH, (WireConnection)state.get(WIRE_CONNECTION_EAST))).with(WIRE_CONNECTION_WEST, (WireConnection)state.get(WIRE_CONNECTION_SOUTH));
            default:
                return state;
        }
    }

    public BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT:
                return (BlockState)((BlockState)state.with(WIRE_CONNECTION_NORTH, (WireConnection)state.get(WIRE_CONNECTION_SOUTH))).with(WIRE_CONNECTION_SOUTH, (WireConnection)state.get(WIRE_CONNECTION_NORTH));
            case FRONT_BACK:
                return (BlockState)((BlockState)state.with(WIRE_CONNECTION_EAST, (WireConnection)state.get(WIRE_CONNECTION_WEST))).with(WIRE_CONNECTION_WEST, (WireConnection)state.get(WIRE_CONNECTION_EAST));
            default:
                return super.mirror(state, mirror);
        }
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{WIRE_CONNECTION_NORTH, WIRE_CONNECTION_EAST, WIRE_CONNECTION_SOUTH, WIRE_CONNECTION_WEST, POWER});
    }

    public void updateAll(BlockState state, World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            world.updateNeighborsAlways(pos.offset(direction), this);
        }
        state = this.getPlacementState(world, state, pos);
        this.updatePowerStrength(world, pos, state);
        this.updateOffsetNeighbors(world, pos);
        state = this.getPlacementState(world, world.getBlockState(pos), pos);
        world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            if (isFullyConnected(state) || isNotConnected(state)) {
                BlockState blockState = isFullyConnected(state) ? this.getDefaultState() : this.dotState;
                blockState = (BlockState)blockState.with(POWER, (Integer)state.get(POWER));
                blockState = this.getPlacementState(world, blockState, pos);
                if (blockState != state) {
                    world.setBlockState(pos, blockState, 3);
                    this.updateForNewState(world, pos, state, blockState);
                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        }
    }

    protected void updateForNewState(World world, BlockPos pos, BlockState oldState, BlockState newState) { 
        for(Direction direction : Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            if (((WireConnection)oldState.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction))).isConnected() != ((WireConnection)newState.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction))).isConnected() && world.getBlockState(blockPos).isSolidBlock(world, blockPos)) {
                world.updateNeighborsExcept(blockPos, newState.getBlock(), direction.getOpposite());
            }
        }
    }

 }
