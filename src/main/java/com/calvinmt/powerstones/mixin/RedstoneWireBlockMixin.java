package com.calvinmt.powerstones.mixin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.calvinmt.powerstones.AbstractBlockStateInterface;
import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.WorldInterface;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin extends Block {

    @Shadow
    private static final Vec3d[] COLORS = Util.make(new Vec3d[16], colors -> {
        for (int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0f;
            float g = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f);
            float h = MathHelper.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float j = MathHelper.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = new Vec3d(g, h, j);
        }
    });
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
    @Shadow
    public static final Map<Direction, EnumProperty<WireConnection>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY = Maps.newEnumMap(ImmutableMap.of(Direction.NORTH, WIRE_CONNECTION_NORTH, Direction.EAST, WIRE_CONNECTION_EAST, Direction.SOUTH, WIRE_CONNECTION_SOUTH, Direction.WEST, WIRE_CONNECTION_WEST));
    @Shadow
    private WireConnection getRenderConnectionType(BlockView world, BlockPos pos, Direction direction) { return null; }
    @Shadow
    private @Final BlockState dotState;
    @Shadow
    private static boolean isFullyConnected(BlockState state) { return false; }
    @Shadow
    private static boolean isNotConnected(BlockState state) { return false; }
    @Shadow
    private void updateForNewState(World world, BlockPos pos, BlockState oldState, BlockState newState) {}
    @Shadow
    private boolean canRunOnTop(BlockView world, BlockPos pos, BlockState floor) { return false; }

    @Shadow
    protected static boolean connectsTo(BlockState state, @Nullable Direction dir) { return false; }

    @Shadow
    private boolean wiresGivePower;

    @Shadow
    private int increasePower(BlockState state) { return 0; }
    @Shadow
    private void updateOffsetNeighbors(World world, BlockPos pos) {};
    @Shadow
    private BlockState getPlacementState(BlockView world, BlockState state, BlockPos pos) { return null; };
    @Shadow
    public abstract int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction);
    @Shadow
    private void addPoweredParticles(World world, Random random, BlockPos pos, Vec3d color, Direction direction, Direction direction2, float f, float g) {};

    private BlockState oldState = null;
    private static BlockState connectsToState = null;

    public RedstoneWireBlockMixin(Settings settings) {
        super(settings);
        // Auto-generated constructor stub
        // Ignored
    }

    @ModifyConstant(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V", constant = @Constant(intValue = 0))
    private int redPowerStoneInitialValueOff(int oldInitialPower) {
        return 1;
    }

    @ModifyArg(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;setDefaultState(Lnet/minecraft/block/BlockState;)V"))
    public BlockState initialiseDefaultStatePowerStoneProperties(BlockState state) {
        return (BlockState)state.with(PowerstoneWireBlock.POWER_B, 0);
    }

    @Inject(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"))
    private void getPlacementStateSetColor(ItemPlacementContext ctx, CallbackInfoReturnable<BlockState> callbackInfo) {
        if (ctx.getStack().isOf(PowerStones.YELLOWSTONE)) {
            this.setDefaultState(this.getDefaultState().with(POWER, 0));
            this.setDefaultState(this.getDefaultState().with(PowerstoneWireBlock.POWER_B, 1));
            this.setDefaultState(this.getDefaultState().with(PowerstoneWireBlock.POWER_PAIR, PowerPair.GREEN_YELLOW));
        }
        else if (ctx.getStack().isOf(PowerStones.GREENSTONE)) {
            this.setDefaultState(this.getDefaultState().with(POWER, 1));
            this.setDefaultState(this.getDefaultState().with(PowerstoneWireBlock.POWER_B, 0));
            this.setDefaultState(this.getDefaultState().with(PowerstoneWireBlock.POWER_PAIR, PowerPair.GREEN_YELLOW));
        }
        else if (ctx.getStack().isOf(PowerStones.BLUESTONE)) {
            this.setDefaultState(this.getDefaultState().with(POWER, 0));
            this.setDefaultState(this.getDefaultState().with(PowerstoneWireBlock.POWER_B, 1));
            this.setDefaultState(this.getDefaultState().with(PowerstoneWireBlock.POWER_PAIR, PowerPair.RED_BLUE));
        }
        else {
            this.setDefaultState(this.getDefaultState().with(POWER, 1));
            this.setDefaultState(this.getDefaultState().with(PowerstoneWireBlock.POWER_B, 0));
            this.setDefaultState(this.getDefaultState().with(PowerstoneWireBlock.POWER_PAIR, PowerPair.RED_BLUE));
        }
    }

    @ModifyArg(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;getPlacementState(Lnet/minecraft/world/BlockView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), index = 1)
    public BlockState getPlacementStateArg1(BlockState state) {
        return (BlockState)((BlockState)((BlockState)this.dotState.with(POWER, this.getDefaultState().get(POWER))).with(PowerstoneWireBlock.POWER_B, this.getDefaultState().get(PowerstoneWireBlock.POWER_B))).with(PowerstoneWireBlock.POWER_PAIR, this.getDefaultState().get(PowerstoneWireBlock.POWER_PAIR));
    }

    @Inject(method = "getPlacementState(Lnet/minecraft/world/BlockView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At("HEAD"))
    private void getPlacementStateSaveOldState(BlockView world, BlockState state, BlockPos pos, CallbackInfoReturnable<BlockState> callbackInfo) {
        this.oldState = state;
    }

    @ModifyArg(method = "getPlacementState(Lnet/minecraft/world/BlockView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;getDefaultWireState(Lnet/minecraft/world/BlockView;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"), index = 1)
    private BlockState getPlacementGetDefaultWireStateArg1(BlockState state) {
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(POWER, this.oldState.get(POWER))).with(PowerstoneWireBlock.POWER_B, this.oldState.get(PowerstoneWireBlock.POWER_B))).with(PowerstoneWireBlock.POWER_PAIR, this.oldState.get(PowerstoneWireBlock.POWER_PAIR));
    }

    @Inject(method = "getStateForNeighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;Lnet/minecraft/block/BlockState;Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    public void getStateForNeighborUpdateLastReturn(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos, CallbackInfoReturnable<BlockState> callbackInfo, WireConnection wireConnection) {
        callbackInfo.setReturnValue(this.getPlacementState(world, (BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.dotState).with(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction), wireConnection)).with(POWER, state.get(POWER))).with(PowerstoneWireBlock.POWER_B, state.get(PowerstoneWireBlock.POWER_B))).with(PowerstoneWireBlock.POWER_PAIR, state.get(PowerstoneWireBlock.POWER_PAIR)), pos));
    }

    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(PowerstoneWireBlock.POWER_PAIR) != PowerPair.RED_BLUE) {
            return 0;
        }
        if (!this.wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        int i = state.get(PowerstoneWireBlock.POWER_B);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(PowerstoneWireBlock.POWER_PAIR) != PowerPair.GREEN_YELLOW) {
            return 0;
        }
        if (!this.wiresGivePower || direction == Direction.DOWN) {
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

    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(PowerstoneWireBlock.POWER_PAIR) != PowerPair.GREEN_YELLOW) {
            return 0;
        }
        if (!this.wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        int i = state.get(PowerstoneWireBlock.POWER_B);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }

    public int getStrongBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!this.wiresGivePower) {
            return 0;
        }
        return ((AbstractBlockStateInterface) state).getWeakBluestonePower(world, pos, direction);
    }

    public int getStrongGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!this.wiresGivePower) {
            return 0;
        }
        return ((AbstractBlockStateInterface) state).getWeakGreenstonePower(world, pos, direction);
    }

    public int getStrongYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (!this.wiresGivePower) {
            return 0;
        }
        return ((AbstractBlockStateInterface) state).getWeakYellowstonePower(world, pos, direction);
    }

    private static boolean isSamePowerStoneType(BlockState currentState, BlockState state) {
        boolean result = false;
        if (currentState.get(PowerstoneWireBlock.POWER_PAIR) == state.get(PowerstoneWireBlock.POWER_PAIR)
         && ((currentState.get(POWER) > 0 && state.get(POWER) > 0)
          || (currentState.get(PowerstoneWireBlock.POWER_B) > 0 && state.get(PowerstoneWireBlock.POWER_B) > 0))) {
            result = true;
        }
        return result;
    }

    @Inject(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At("HEAD"), cancellable = true)
    private void saveConnectsToState(BlockView world, BlockPos pos, Direction direction, boolean bl, CallbackInfoReturnable<WireConnection> callbackInfo) {
        connectsToState = world.getBlockState(pos);
        if (! connectsToState.isOf(Blocks.REDSTONE_WIRE)) {
            connectsToState = this.getDefaultState();
        }
    }

    @Redirect(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z", ordinal = 0))
    private static boolean checkConnectsToState(BlockState state, Block block) {
        return state.isOf(block) && isSamePowerStoneType(connectsToState, state);
    }

    @Inject(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "RETURN", ordinal = 3), cancellable = true)
    private static void connectsToEmitsRedstonePower(BlockState state, @Nullable Direction dir, CallbackInfoReturnable<Boolean> callbackInfo) {
        callbackInfo.setReturnValue(! state.isOf(Blocks.REDSTONE_WIRE) && callbackInfo.getReturnValue());
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
        if ((state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE && haveDifferentPowerLevel(state.get(POWER), r))
         || (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE && haveDifferentPowerLevel(state.get(PowerstoneWireBlock.POWER_B), b))
         || (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW && haveDifferentPowerLevel(state.get(POWER), g))
         || (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW && haveDifferentPowerLevel(state.get(PowerstoneWireBlock.POWER_B), y))) {
            if (world.getBlockState(pos) == state) {
                if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE && haveDifferentPowerLevel(state.get(POWER), r))
                    world.setBlockState(pos, (BlockState)state.with(POWER, r), Block.NOTIFY_LISTENERS);
                if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE && haveDifferentPowerLevel(state.get(PowerstoneWireBlock.POWER_B), b))
                    world.setBlockState(pos, (BlockState)state.with(PowerstoneWireBlock.POWER_B, b), Block.NOTIFY_LISTENERS);
                if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW && haveDifferentPowerLevel(state.get(POWER), g))
                    world.setBlockState(pos, (BlockState)state.with(POWER, g), Block.NOTIFY_LISTENERS);
                if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW && haveDifferentPowerLevel(state.get(PowerstoneWireBlock.POWER_B), y))
                    world.setBlockState(pos, (BlockState)state.with(PowerstoneWireBlock.POWER_B, y), Block.NOTIFY_LISTENERS);
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

    @Overwrite
    private int getReceivedRedstonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = world.getReceivedRedstonePower(pos);
        this.wiresGivePower = true;
        int j = 1;
        if (i < 16) {
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
        return Math.max(i, j - 1);
    }
    /*@ModifyConstant(method = "getReceivedRedstonePower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", constant = @Constant(intValue = 0))
    private int getReceivedRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "getReceivedRedstonePower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", constant = @Constant(intValue = 15))
    private int getReceivedRedstonePowerMaxPower(int oldMaxPower) {
        return 16;
    }*/
    
    private int getReceivedBluestonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = ((WorldInterface) world).getReceivedBluestonePower(pos);
        this.wiresGivePower = true;
        int j = 1;
        if (i < 16) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increasePowerBlue(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increasePowerBlue(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.increasePowerBlue(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j - 1);
    }
    
    private int getReceivedGreenstonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = ((WorldInterface) world).getReceivedGreenstonePower(pos);
        this.wiresGivePower = true;
        int j = 1;
        if (i < 16) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increasePowerGreen(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increasePowerGreen(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.increasePowerGreen(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j - 1);
    }
    
    private int getReceivedYellowstonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = ((WorldInterface) world).getReceivedYellowstonePower(pos);
        this.wiresGivePower = true;
        int j = 1;
        if (i < 16) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increasePowerYellow(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increasePowerYellow(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.increasePowerYellow(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j - 1);
    }

    @Inject(method = "increasePower(Lnet/minecraft/block/BlockState;)I", at = @At("HEAD"), cancellable = true)
    private void increasePower(BlockState state, CallbackInfoReturnable<Integer> callbackInfo) {
        if (state.isOf(this) && state.get(PowerstoneWireBlock.POWER_PAIR) != PowerPair.RED_BLUE) {
            callbackInfo.setReturnValue(0);
        }
    }

    private int increasePowerBlue(BlockState state) {
        return (state.isOf(this) && state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE) ? state.get(PowerstoneWireBlock.POWER_B) : 0;
    }

    private int increasePowerGreen(BlockState state) {
        return (state.isOf(this) && state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW) ? state.get(POWER) : 0;
    }

    private int increasePowerYellow(BlockState state) {
        return (state.isOf(this) && state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW) ? state.get(PowerstoneWireBlock.POWER_B) : 0;
    }

    @ModifyVariable(method = "getWireColor(I)I", at = @At("HEAD"))
    private static int getWireColor(int powerLevel) {
        return powerLevel - 1;
    }

    private Vec3d getPowerStoneColor(BlockState state, Random random) {
        List<Vec3d[]> colorsList = new ArrayList<>();
        List<Integer> powerList = new ArrayList<>();
        if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE) {
            if (state.get(POWER) > 1) {
                colorsList.add(COLORS);
                powerList.add(state.get(POWER));
            }
            if (state.get(PowerstoneWireBlock.POWER_B) > 1) {
                colorsList.add(PowerstoneWireBlock.BLUE_COLORS);
                powerList.add(state.get(PowerstoneWireBlock.POWER_B));
            }
        }
        if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            if (state.get(POWER) > 1) {
                colorsList.add(PowerstoneWireBlock.GREEN_COLORS);
                powerList.add(state.get(POWER));
            }
            if (state.get(PowerstoneWireBlock.POWER_B) > 1) {
                colorsList.add(PowerstoneWireBlock.YELLOW_COLORS);
                powerList.add(state.get(PowerstoneWireBlock.POWER_B));
            }
        }
        int i = random.nextInt(colorsList.size());
        int power = powerList.get(i) - 1;
        return colorsList.get(i)[power];
    }

    @Overwrite
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(POWER) < 2 && state.get(PowerstoneWireBlock.POWER_B) < 2) {
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
    /*@Inject(method = "randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", at = @At("HEAD"))
    private void randomDisplayTickPowerCondition(BlockState state, World world, BlockPos pos, Random random, CallbackInfo callbackInfo) {
        if (state.get(POWER) < 2 && state.get(PowerstoneWireBlock.POWER_B) < 2) {
            callbackInfo.cancel();
        }
    }

    //@ModifyArg(method = "randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;addPoweredParticles(Lnet/minecraft/world/World;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/Direction;FF)V"), index = 3)
    @ModifyVariable(method = "randomDisplayTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", at = @At(value = "FIELD", target = "Lnet/minecraft/block/RedstoneWireBlock;COLORS:[Lnet/minecraft/util/math/Vec3d;", opcode = Opcodes.GETSTATIC))
    private Vec3d[] randomDisplayTickGetColors(Vec3d[] oldColors, BlockState state, World world, BlockPos pos, Random random) {
        return Util.make(new Vec3d[1], colors -> {
            colors[0] = this.getPowerStoneColor(state, random);
        });
    }*/

    private void updateAll(BlockState state, World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            world.updateNeighborsAlways(pos.offset(direction), this);
        }
        state = this.getPlacementState(world, state, pos);
        this.update(world, pos, state);
        this.updateOffsetNeighbors(world, pos);
        state = this.getPlacementState(world, world.getBlockState(pos), pos);
        world.setBlockState(pos, (BlockState)(state.with(POWER, state.get(POWER))).with(PowerstoneWireBlock.POWER_B, state.get(PowerstoneWireBlock.POWER_B)),  Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
    }

    private void placeOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, IntProperty powerProperty) {
        BlockSoundGroup blockSoundGroup = state.getSoundGroup();
        world.playSound(player, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0f) / 2.0f, blockSoundGroup.getPitch() * 0.8f);
        if (player == null || !player.getAbilities().creativeMode) {
            player.getMainHandStack().decrement(1);
        }
        world.setBlockState(pos, state.with(powerProperty, 1),  Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        state = world.getBlockState(pos);
        this.updateAll(state, world, pos);
    }

    @Inject(method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;isFullyConnected(Lnet/minecraft/block/BlockState;)Z", ordinal = 0), cancellable = true)
    public void onUseAddColor(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> callbackInfo) {
        if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE) {
            if (player.getMainHandStack().isOf(Items.REDSTONE) && state.get(POWER) == 0) {
                this.placeOnUse(state, world, pos, player, POWER);
                callbackInfo.setReturnValue(ActionResult.SUCCESS);
            }
            else if (player.getMainHandStack().isOf(PowerStones.BLUESTONE) && state.get(PowerstoneWireBlock.POWER_B) == 0) {
                this.placeOnUse(state, world, pos, player, PowerstoneWireBlock.POWER_B);
                callbackInfo.setReturnValue(ActionResult.SUCCESS);
            }
        }
        else if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            if (player.getMainHandStack().isOf(PowerStones.GREENSTONE) && state.get(POWER) == 0) {
                this.placeOnUse(state, world, pos, player, POWER);
                callbackInfo.setReturnValue(ActionResult.SUCCESS);
            }
            else if (player.getMainHandStack().isOf(PowerStones.YELLOWSTONE) && state.get(PowerstoneWireBlock.POWER_B) == 0) {
                this.placeOnUse(state, world, pos, player, PowerstoneWireBlock.POWER_B);
                callbackInfo.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }

    @Redirect(method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;getDefaultState()Lnet/minecraft/block/BlockState;"))
    private BlockState onUseDefaultState(RedstoneWireBlock redstoneWireBlock, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(POWER, state.get(POWER))).with(PowerstoneWireBlock.POWER_B, state.get(PowerstoneWireBlock.POWER_B))).with(PowerstoneWireBlock.POWER_PAIR, state.get(PowerstoneWireBlock.POWER_PAIR));
    }

    @Redirect(method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "FIELD", target = "Lnet/minecraft/block/RedstoneWireBlock;dotState:Lnet/minecraft/block/BlockState;", opcode = Opcodes.GETFIELD))
    private BlockState onUseDotState(RedstoneWireBlock redstoneWireBlock, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return (BlockState)((BlockState)((BlockState)this.dotState.with(POWER, state.get(POWER))).with(PowerstoneWireBlock.POWER_B, state.get(PowerstoneWireBlock.POWER_B))).with(PowerstoneWireBlock.POWER_PAIR, state.get(PowerstoneWireBlock.POWER_PAIR));
    }

    @ModifyVariable(method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At("STORE"), ordinal = 1)
    private BlockState onUseSetPower(BlockState blockState, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return (BlockState)((BlockState)blockState.with(PowerstoneWireBlock.POWER_B, state.get(PowerstoneWireBlock.POWER_B))).with(PowerstoneWireBlock.POWER_PAIR, state.get(PowerstoneWireBlock.POWER_PAIR));
    }

    @Inject(method = "appendProperties(Lnet/minecraft/state/StateManager$Builder;)V", at = @At("TAIL"))
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo callbackInfo) {
        builder.add(PowerstoneWireBlock.POWER_B, PowerstoneWireBlock.POWER_PAIR);
    }

    private boolean hasMoreThanOnePowerstone(BlockState state) {
        int i = 0;
        if (state.get(POWER) > 0) {
            i++;
        }
        if (state.get(PowerstoneWireBlock.POWER_B) > 0) {
            i++;
        }
        return i > 1;
    }

    private void breakSingle(BlockState state, World world, BlockPos pos, PlayerEntity player, IntProperty powerProperty, Item item) {
        if (this.hasMoreThanOnePowerstone(state)) {
            BlockSoundGroup blockSoundGroup = state.getSoundGroup();
            world.playSound(player, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0f) / 2.0f, blockSoundGroup.getPitch() * 0.8f);
        }
        world.setBlockState(pos, state.with(powerProperty, 0),  Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        world.spawnEntity(new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(item)));
        state = world.getBlockState(pos);
        this.updateAll(state, world, pos);
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.RED_BLUE) {
            if (player.getMainHandStack().isOf(Items.REDSTONE) && state.get(POWER) > 0) {
                this.breakSingle(state, world, pos, player, POWER, Items.REDSTONE);
            }
            else if (player.getMainHandStack().isOf(PowerStones.BLUESTONE) && state.get(PowerstoneWireBlock.POWER_B) > 0) {
                this.breakSingle(state, world, pos, player, PowerstoneWireBlock.POWER_B, PowerStones.BLUESTONE);
            }
        }
        else if (state.get(PowerstoneWireBlock.POWER_PAIR) == PowerPair.GREEN_YELLOW) {
            if (player.getMainHandStack().isOf(PowerStones.GREENSTONE) && state.get(POWER) > 0) {
                this.breakSingle(state, world, pos, player, POWER, PowerStones.GREENSTONE);
            }
            else if (player.getMainHandStack().isOf(PowerStones.YELLOWSTONE) && state.get(PowerstoneWireBlock.POWER_B) > 0) {
                this.breakSingle(state, world, pos, player, PowerstoneWireBlock.POWER_B, PowerStones.YELLOWSTONE);
            }
        }
    }

    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (state.get(POWER) == 0 && state.get(PowerstoneWireBlock.POWER_B) == 0) {
            return 1.0f;
        }
        if (player.getMainHandStack().isOf(Items.REDSTONE) || player.getMainHandStack().isOf(PowerStones.BLUESTONE) || player.getMainHandStack().isOf(PowerStones.GREENSTONE) || player.getMainHandStack().isOf(PowerStones.YELLOWSTONE)) {
            return 0.0f;
        }
        return 1.0f;
    }

}
