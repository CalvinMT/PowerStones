package com.calvinmt.powerstones.mixin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.WorldInterface;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin extends Block {

    @Shadow
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_NORTH = Properties.NORTH_WIRE_CONNECTION;
    @Shadow
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_EAST = Properties.EAST_WIRE_CONNECTION;
    @Shadow
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_SOUTH = Properties.SOUTH_WIRE_CONNECTION;
    @Shadow
    public static final EnumProperty<WireConnection> WIRE_CONNECTION_WEST = Properties.WEST_WIRE_CONNECTION;
    @Shadow
    public static final IntProperty POWER = Properties.POWER;
    @Shadow
    private BlockState getDefaultWireState(BlockView world, BlockState state, BlockPos pos) { return state; }
    private static boolean isNotConnected(BlockState state) {
        return !state.get(WIRE_CONNECTION_NORTH).isConnected() && !state.get(WIRE_CONNECTION_SOUTH).isConnected() && !state.get(WIRE_CONNECTION_EAST).isConnected() && !state.get(WIRE_CONNECTION_WEST).isConnected();
    }
    @Shadow
    public static final Map<Direction, EnumProperty<WireConnection>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, WIRE_CONNECTION_NORTH, Direction.EAST, WIRE_CONNECTION_EAST, Direction.SOUTH, WIRE_CONNECTION_SOUTH, Direction.WEST, WIRE_CONNECTION_WEST));
    @Shadow
    private WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction) { return null; }
    @Shadow
    private @Final BlockState dotState;
    @Shadow
    private static boolean isFullyConnected(BlockState state) { return false; }
    @Shadow
    private void updateForNewState(World world, BlockPos pos, BlockState oldState, BlockState newState) {}
    @Shadow
    private boolean canRunOnTop(BlockView world, BlockPos pos, BlockState floor) { return false; }

    @Shadow
    private boolean wiresGivePower;

    @Shadow
    private int increasePower(BlockState state) { return 0; }

    private int powerstoneSources = 0;

    public RedstoneWireBlockMixin(Settings settings) {
        super(settings);
        // Auto-generated constructor stub
        // Will be ignored
    }

    @ModifyConstant(method = "<init>", constant = @Constant(intValue = 0))
    private int redPowerStoneInitialValueOff(int oldInitialPower) {
        return 1;
    }

    // https://discord.com/channels/507304429255393322/807617700734042122/1066848990286589962
    @Inject(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V", at = @At("TAIL"))
    public void initialisePowerStoneProperties(AbstractBlock.Settings settings, CallbackInfo callbackInfo) {
        setDefaultState(getDefaultState().with(POWER, 1));
        setDefaultState(getDefaultState().with(PowerstoneWireBlock.BLUE_WIRE, 0));
        setDefaultState(getDefaultState().with(PowerstoneWireBlock.GREEN_WIRE, 0));
        setDefaultState(getDefaultState().with(PowerstoneWireBlock.YELLOW_WIRE, 0));
        powerstoneSources = 0;
    }

    private boolean isSamePowerStoneType(BlockState currentState, BlockState state) {
        boolean result = false;
        if ((currentState.get(POWER) > 0 && state.get(POWER) > 0)
         || (currentState.get(PowerstoneWireBlock.BLUE_WIRE) > 0 && state.get(PowerstoneWireBlock.BLUE_WIRE) > 0)
         || (currentState.get(PowerstoneWireBlock.GREEN_WIRE) > 0 && state.get(PowerstoneWireBlock.GREEN_WIRE) > 0)
         || (currentState.get(PowerstoneWireBlock.YELLOW_WIRE) > 0 && state.get(PowerstoneWireBlock.YELLOW_WIRE) > 0)) {
            result = true;
        }
        return result;
    }

    private boolean isPowerStoneWire(BlockState state) {
        boolean result = false;
        if (state.isOf(Blocks.REDSTONE_WIRE)
         || state.isOf(PowerStones.BLUESTONE_WIRE)
         || state.isOf(PowerStones.GREENSTONE_WIRE)
         || state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            result = true;
        }
        return result;
    }

    public void addPowerstoneSources(int sources) {
        powerstoneSources += sources;
    }

    public void removePowerstoneSources(int sources) {
        powerstoneSources -= sources;
    }

    public int getPowerstoneSources() {
        return powerstoneSources;
    }

    /*private boolean hasMoreThanOnePowerstone(BlockState state) {
        int i = 0;
        if (state.get(POWER) > 0) {
            i++;
        }
        if (state.get(PowerstoneWireBlock.BLUE_WIRE) > 0) {
            i++;
        }
        if (state.get(PowerstoneWireBlock.GREEN_WIRE) > 0) {
            i++;
        }
        if (state.get(PowerstoneWireBlock.YELLOW_WIRE) > 0) {
            i++;
        }
        return i > 1;
    }*/

    @ModifyArg(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;getPlacementState(Lnet/minecraft/world/BlockView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), index = 1)
    public BlockState getPlacementStateArg2(BlockState state) {
        return this.getDefaultState();
    }

    @Overwrite
    private BlockState getPlacementState(BlockView world, BlockState state, BlockPos pos) {
        BlockState oldState = state;
        boolean bl = isNotConnected(state);
        state = this.getDefaultWireState(world, (BlockState)this.getDefaultState(), pos);
        if (bl && isNotConnected(state)) {
            return state;
        }
        state = (BlockState)((BlockState)((BlockState)((BlockState)state.with(POWER, oldState.get(POWER))).with(PowerstoneWireBlock.BLUE_WIRE, oldState.get(PowerstoneWireBlock.BLUE_WIRE))).with(PowerstoneWireBlock.GREEN_WIRE, oldState.get(PowerstoneWireBlock.GREEN_WIRE))).with(PowerstoneWireBlock.YELLOW_WIRE, oldState.get(PowerstoneWireBlock.YELLOW_WIRE));
        boolean bl2 = state.get(WIRE_CONNECTION_NORTH).isConnected();
        boolean bl3 = state.get(WIRE_CONNECTION_SOUTH).isConnected();
        boolean bl4 = state.get(WIRE_CONNECTION_EAST).isConnected();
        boolean bl5 = state.get(WIRE_CONNECTION_WEST).isConnected();
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

    /*@Overwrite
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) {
            return state;
        }
        if (direction == Direction.UP) {
            return this.getPlacementState(world, state, pos);
        }
        WireConnection wireConnection = this.getRenderConnectionType(world, pos, direction);
        if (wireConnection.isConnected() == ((WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction))).isConnected() && !isFullyConnected(state)) {
            return (BlockState)state.with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), wireConnection);
        }
        return this.getPlacementState(world, (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.dotState).with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), wireConnection)).with(POWER, state.get(POWER))).with(PowerstoneWireBlock.BLUE_WIRE, state.get(PowerstoneWireBlock.BLUE_WIRE))).with(PowerstoneWireBlock.GREEN_WIRE, state.get(PowerstoneWireBlock.GREEN_WIRE))).with(PowerstoneWireBlock.YELLOW_WIRE, state.get(PowerstoneWireBlock.YELLOW_WIRE)), pos);
    }*/
    @Inject(method = "getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void getStateForNeighborUpdateLastReturn(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> callbackInfo, WireConnection wireConnection) {
        callbackInfo.setReturnValue(this.getPlacementState(world, (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.dotState).with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), wireConnection)).with(POWER, state.get(POWER))).with(PowerstoneWireBlock.BLUE_WIRE, state.get(PowerstoneWireBlock.BLUE_WIRE))).with(PowerstoneWireBlock.GREEN_WIRE, state.get(PowerstoneWireBlock.GREEN_WIRE))).with(PowerstoneWireBlock.YELLOW_WIRE, state.get(PowerstoneWireBlock.YELLOW_WIRE)), pos));
    }

    private int getWeakPower(IntProperty wire, BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!this.wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        int i = state.get(wire);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getWeakPower(PowerstoneWireBlock.BLUE_WIRE, state, world, pos, direction);
    }

    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getWeakPower(PowerstoneWireBlock.GREEN_WIRE, state, world, pos, direction);
    }

    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getWeakPower(PowerstoneWireBlock.YELLOW_WIRE, state, world, pos, direction);
    }

    private int getStrongPower(int power) {
        if (!this.wiresGivePower) {
            return 0;
        }
        return power;
    }

    public int getStrongBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getStrongPower(((AbstractBlockStateInterface) state).getWeakBluestonePower(world, pos, direction));
    }

    public int getStrongGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getStrongPower(((AbstractBlockStateInterface) state).getWeakGreenstonePower(world, pos, direction));
    }

    public int getStrongYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getStrongPower(((AbstractBlockStateInterface) state).getWeakYellowstonePower(world, pos, direction));
    }

    @Overwrite
    private WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction, boolean bl) {
        BlockState currentState = world.getBlockState(pos);
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        if (! isPowerStoneWire(currentState)) {
            currentState = this.getDefaultState();
        }
        if (bl && this.canRunOnTop(world, blockPos, blockState) && connectsTo(currentState, world.getBlockState(blockPos.up()))) {
            if (blockState.isSideSolidFullSquare(world, blockPos, direction.getOpposite())) {
                return WireConnection.UP;
            }
            return WireConnection.SIDE;
        }
        if (connectsTo(currentState, blockState, direction) || !blockState.isSolidBlock(world, blockPos) && connectsTo(currentState, world.getBlockState(blockPos.down()))) {
            return WireConnection.SIDE;
        }
        return WireConnection.NONE;
    }

    //@Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;)Z"))
    protected boolean connectsTo(BlockState currentState, BlockState state) {
        return this.connectsTo(currentState, state, null);
    }

    //@Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z"))
    protected boolean connectsTo(BlockState currentState, BlockState state, Direction dir) {
        boolean result = false;
        if (isPowerStoneWire(state) && isSamePowerStoneType(currentState, state)) {
            result = true;
        }
        else if (state.isOf(Blocks.REPEATER)) {
            Direction direction = state.get(RepeaterBlock.FACING);
            result = direction == dir || direction.getOpposite() == dir;
        }
        else if (state.isOf(Blocks.OBSERVER)) {
            result = dir == state.get(ObserverBlock.FACING);
        }
        else if (! isPowerStoneWire(state)) {
            result = state.emitsRedstonePower() && dir != null;
        }
        return result;
    }

    private boolean haveDifferentPowerLevel(int i, int j) {
        return i != 0 && j != 0 && i != j;
    }

    @Overwrite
    private void update(World world, BlockPos pos, BlockState state) {
        int r = this.getReceivedRedstonePower(world, pos);
        int b = this.getReceivedBluestonePower(world, pos);
        int g = this.getReceivedGreenstonePower(world, pos);
        int y = this.getReceivedYellowstonePower(world, pos);
        if (haveDifferentPowerLevel(state.get(POWER), r)
         || haveDifferentPowerLevel(state.get(PowerstoneWireBlock.BLUE_WIRE), b)
         || haveDifferentPowerLevel(state.get(PowerstoneWireBlock.GREEN_WIRE), g)
         || haveDifferentPowerLevel(state.get(PowerstoneWireBlock.YELLOW_WIRE), y)) {
            if (world.getBlockState(pos) == state) {
                if (haveDifferentPowerLevel(state.get(POWER), r))
                    world.setBlockState(pos, (BlockState)state.with(POWER, r), Block.NOTIFY_LISTENERS);
                if (haveDifferentPowerLevel(state.get(PowerstoneWireBlock.BLUE_WIRE), b))
                    world.setBlockState(pos, (BlockState)state.with(PowerstoneWireBlock.BLUE_WIRE, b), Block.NOTIFY_LISTENERS);
                if (haveDifferentPowerLevel(state.get(PowerstoneWireBlock.GREEN_WIRE), g))
                    world.setBlockState(pos, (BlockState)state.with(PowerstoneWireBlock.GREEN_WIRE, g), Block.NOTIFY_LISTENERS);
                if (haveDifferentPowerLevel(state.get(PowerstoneWireBlock.YELLOW_WIRE), y))
                    world.setBlockState(pos, (BlockState)state.with(PowerstoneWireBlock.YELLOW_WIRE, y), Block.NOTIFY_LISTENERS);
            }
            HashSet<BlockPos> set = Sets.newHashSet();
            set.add(pos);
            for (Direction direction : Direction.values()) {
                set.add(pos.offset(direction));
            }
            for (BlockPos blockPos : set) {
                world.updateNeighborsAlways(blockPos, this);
            }
        }
    }

    /*public int getReceivedRedstonePowerFromWorld(World world, BlockPos pos) {
        int i = 0;
        BlockState currentState = world.getBlockState(pos);
        for (Direction direction : Direction.values()) {
            BlockState offsetBlockState = world.getBlockState(pos.offset(direction));
            if (! offsetBlockState.emitsRedstonePower()) {
                continue;
            }
            if (isPowerStoneWire(currentState) && isPowerStoneWire(offsetBlockState) && ! isSamePowerStoneType(currentState, offsetBlockState)) {
                continue;
            }
            int j = world.getEmittedRedstonePower(pos.offset(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }*/

    @Overwrite
    private int getReceivedRedstonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        //int i = getReceivedRedstonePowerFromWorld(world, pos);
        int i = world.getReceivedRedstonePower(pos);
        this.wiresGivePower = true;
        int j = 1;
        if (i < 2) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increasePower(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increasePower(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.increasePower(world.getBlockState(blockPos.down())));
            }
        }
        // TODO - if i == 0 and no_source then return 1
        return Math.max(i, j);
    }
    
    private int getReceivedBluestonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = ((WorldInterface) world).getReceivedBluestonePower(pos);
        this.wiresGivePower = true;
        int j = 1;
        if (i < 2) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increaseBluestonePower(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increaseBluestonePower(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.increaseBluestonePower(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j);
    }
    
    private int getReceivedGreenstonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = ((WorldInterface) world).getReceivedGreenstonePower(pos);
        this.wiresGivePower = true;
        int j = 1;
        if (i < 2) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increaseGreenstonePower(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increaseGreenstonePower(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.increaseGreenstonePower(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j);
    }
    
    private int getReceivedYellowstonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = ((WorldInterface) world).getReceivedYellowstonePower(pos);
        this.wiresGivePower = true;
        int j = 1;
        if (i < 2) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increaseYellowstonePower(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increaseYellowstonePower(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.increaseYellowstonePower(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j);
    }

    private int increaseBluestonePower(BlockState state) {
        return state.isOf(this) ? state.get(PowerstoneWireBlock.BLUE_WIRE) : 0;
    }

    private int increaseGreenstonePower(BlockState state) {
        return state.isOf(this) ? state.get(PowerstoneWireBlock.GREEN_WIRE) : 0;
    }

    private int increaseYellowstonePower(BlockState state) {
        return state.isOf(this) ? state.get(PowerstoneWireBlock.YELLOW_WIRE) : 0;
    }

    private Vec3d getPowerStoneColor(BlockState state, Random random) {
        List<Vec3d[]> colorsList = new ArrayList<>();
        if (state.get(POWER) == 2) {
            colorsList.add(PowerstoneWireBlock.RED_COLORS);
        }
        if (state.get(PowerstoneWireBlock.BLUE_WIRE) == 2) {
            colorsList.add(PowerstoneWireBlock.BLUE_COLORS);
        }
        if (state.get(PowerstoneWireBlock.GREEN_WIRE) == 2) {
            colorsList.add(PowerstoneWireBlock.GREEN_COLORS);
        }
        if (state.get(PowerstoneWireBlock.YELLOW_WIRE) == 2) {
            colorsList.add(PowerstoneWireBlock.YELLOW_COLORS);
        }
        int i = random.nextInt(colorsList.size());
        return colorsList.get(i)[2];
    }

    @Overwrite
    private void addPoweredParticles(World world, Random random, BlockPos pos, Vec3d color, Direction direction, Direction direction2, float f, float g) {
        float h = g - f;
        if (random.nextFloat() >= 0.2f * h) {
            return;
        }
        float i = 0.4375f;
        float j = f + h * random.nextFloat();
        double d = 0.5 + (double)(i * (float)direction.getOffsetX()) + (double)(j * (float)direction2.getOffsetX());
        double e = 0.5 + (double)(i * (float)direction.getOffsetY()) + (double)(j * (float)direction2.getOffsetY());
        double k = 0.5 + (double)(i * (float)direction.getOffsetZ()) + (double)(j * (float)direction2.getOffsetZ());
        world.addParticle(new DustParticleEffect(color.toVector3f(), 1.0f), (double)pos.getX() + d, (double)pos.getY() + e, (double)pos.getZ() + k, 0.0, 0.0, 0.0);
    }

    @Overwrite
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(POWER) < 2 && state.get(PowerstoneWireBlock.BLUE_WIRE) < 2 && state.get(PowerstoneWireBlock.GREEN_WIRE) < 2 && state.get(PowerstoneWireBlock.YELLOW_WIRE) < 2) {
            return;
        }
        block4: for (Direction direction : Direction.Type.HORIZONTAL) {
            WireConnection wireConnection = (WireConnection)state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
            switch (wireConnection) {
                case UP: {
                    this.addPoweredParticles(world, random, pos, getPowerStoneColor(state, random), direction, Direction.UP, -0.5f, 0.5f);
                }
                case SIDE: {
                    this.addPoweredParticles(world, random, pos, getPowerStoneColor(state, random), Direction.DOWN, direction, 0.0f, 0.5f);
                    continue block4;
                }
                case NONE:
                    break;
                default:
                    break;
            }
            this.addPoweredParticles(world, random, pos, getPowerStoneColor(state, random), Direction.DOWN, direction, 0.0f, 0.3f);
        }
    }

    @Inject(method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"), cancellable = true)
    public void onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> callbackInfo) {
        if (! player.getAbilities().allowModifyWorld) {
            callbackInfo.setReturnValue(ActionResult.PASS);
        }
        if (player.getMainHandStack().isOf(Items.REDSTONE) && state.get(POWER) == 0) {
            world.setBlockState(pos, state.with(POWER, 1), Block.NOTIFY_ALL);
            callbackInfo.setReturnValue(ActionResult.SUCCESS);
        }
        if (player.getMainHandStack().isOf(PowerStones.BLUESTONE_WIRE.asItem()) && state.get(PowerstoneWireBlock.BLUE_WIRE) == 0) {
            world.setBlockState(pos, state.with(PowerstoneWireBlock.BLUE_WIRE, 1), Block.NOTIFY_ALL);
            callbackInfo.setReturnValue(ActionResult.SUCCESS);
        }
        if (player.getMainHandStack().isOf(PowerStones.GREENSTONE_WIRE.asItem()) && state.get(PowerstoneWireBlock.GREEN_WIRE) == 0) {
            world.setBlockState(pos, state.with(PowerstoneWireBlock.GREEN_WIRE, 1), Block.NOTIFY_ALL);
            callbackInfo.setReturnValue(ActionResult.SUCCESS);
        }
        if (player.getMainHandStack().isOf(PowerStones.YELLOWSTONE_WIRE.asItem()) && state.get(PowerstoneWireBlock.YELLOW_WIRE) == 0) {
            world.setBlockState(pos, state.with(PowerstoneWireBlock.YELLOW_WIRE, 1), Block.NOTIFY_ALL);
            callbackInfo.setReturnValue(ActionResult.SUCCESS);
        }
        /*if (hasMoreThanOnePowerstone(state)) {
            callbackInfo.setReturnValue(ActionResult.PASS);
        }*/
        callbackInfo.setReturnValue(ActionResult.PASS);
    }

    @Inject(method = "appendProperties(Lnet/minecraft/state/StateManager$Builder;)V", at = @At("TAIL"))
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo callbackInfo) {
        builder.add(PowerstoneWireBlock.BLUE_WIRE, PowerstoneWireBlock.GREEN_WIRE, PowerstoneWireBlock.YELLOW_WIRE);
    }

}
