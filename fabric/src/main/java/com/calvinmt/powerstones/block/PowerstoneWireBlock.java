package com.calvinmt.powerstones.block;

import java.util.Set;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateManager;
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
import net.minecraft.util.math.Direction.Type;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public abstract class PowerstoneWireBlock extends PowerstoneWireBlockBase {
    
    public static final IntProperty POWER = Properties.POWER;

    private Vec3d[] COLORS;

    public static final Vec3d[] RED_COLORS = Util.make(new Vec3d[16], (colors) -> {
        for(int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0F;
            float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float f2 = MathHelper.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float f3 = MathHelper.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            colors[i] = new Vec3d((double)f1, (double)f2, (double)f3);
        }
    
    });
    public static final Vec3d[] BLUE_COLORS = (Util.make(new Vec3d[16], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = MathHelper.clamp(f * f * 0.35f + 0.15f, 0.0f, 1.0f); // 0.15f - 0.5f
            float c = MathHelper.clamp(f * f * 0.4f + 0.2f, 0.0f, 1.0f); // 0.2f - 0.6f
            colors[i] = new Vec3d(b, c, a);
        }
    }));
    public static final Vec3d[] GREEN_COLORS = (Util.make(new Vec3d[16], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = MathHelper.clamp(f * f * 0.35f + 0.15f, 0.0f, 1.0f); // 0.15f - 0.5f
            float c = MathHelper.clamp(f * f * 0.25f, 0.0f, 1.0f); // 0.0f - 0.25f
            colors[i] = new Vec3d(c, a, b);
        }
    }));
    public static final Vec3d[] YELLOW_COLORS = (Util.make(new Vec3d[16], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = f * 0.5f + (f > 0.0f ? 0.3f : 0.2f); // 0.2f - 0.8f
            float c = MathHelper.clamp(f * f * 0.4f + 0.2f, 0.0f, 1.0f); // 0.2f - 0.6f
            colors[i] = new Vec3d(a, b, c);
        }
    }));

    public PowerstoneWireBlock(Settings settings, Vec3d[] colors) {
        super(settings);
        COLORS = colors;
        this.setDefaultState(this.stateManager.getDefaultState().with(WIRE_CONNECTION_NORTH, WireConnection.NONE).with(WIRE_CONNECTION_EAST, WireConnection.NONE).with(WIRE_CONNECTION_SOUTH, WireConnection.NONE).with(WIRE_CONNECTION_WEST, WireConnection.NONE).with(POWER, Integer.valueOf(0)));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return (VoxelShape)SHAPES.get(state.with(POWER, 0));
    }

    /**
     * Calculates how this wire colour would connect at the given position in the world.
     *
     * The returned state is only a temporary connection state. It must not be
     * placed into the world when the position contains a MultipleWiresBlock.
     */
    public BlockState getConnectionState(BlockView world, BlockPos pos) {
        return this.getPlacementState(world, this.getDefaultState(), pos);
    }

    @Override
    protected BlockState getDefaultBlockStateWithPowerProperties(BlockState state) {
        return this.getDefaultState().with(POWER, (Integer)state.get(POWER));
    }

    @Override
    protected BlockState getCrossStateWithPowerProperties(BlockState state) {
        return (BlockState)((BlockState)this.dotState.with(POWER, (Integer)state.get(POWER)));
    }

    @Override
    protected boolean isOtherConnectablePowerstone(BlockState state) {
        if (state.isOf(PowerStones.MULTIPLE_WIRES) && state.get(PowerStones.POWER_PAIR) == this.getPowerPair()) {
            return true;
        }
        return false;
    }

    protected abstract PowerPair getPowerPair();

    @Override
    protected void updatePowerStrength(World world, BlockPos pos, BlockState state) {
        int i = this.calculateTargetStrength(world, pos);
        if (state.get(POWER) != i) {
            if (world.getBlockState(pos) == state) {
                world.setBlockState(pos, state.with(POWER, Integer.valueOf(i)), 2);
            }

            Set<BlockPos> set = Sets.newHashSet();
            set.add(pos);

            for(Direction direction : Direction.values()) {
                set.add(pos.offset(direction));
            }

            for(BlockPos blockpos : set) {
                world.updateNeighbors(blockpos, this);
            }
        }
    }

    private int calculateTargetStrength(World world, BlockPos pos) {
        wiresGivePower = false;
        ((MultipleWiresBlock)PowerStones.MULTIPLE_WIRES).setShouldSignal(false);
        int i = this.getReceivedPower(world, pos);
        wiresGivePower = true;
        ((MultipleWiresBlock)PowerStones.MULTIPLE_WIRES).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for (Direction direction : Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.getPower(blockState, world, blockPos));
                BlockPos blockPos2 = pos.up();
                if (this.canRunOnTop(world, blockPos, blockState) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    BlockPos blockPosUp = blockPos.up();
                    BlockState blockStateUp = world.getBlockState(blockPosUp);
                    j = Math.max(j, this.getPower(blockStateUp, world, blockPosUp));
                    continue;
                }
                if (this.canRunOnTop(world, blockPos, blockState)) continue;
                BlockPos blockPosDown = blockPos.down();
                BlockState blockStateDown = world.getBlockState(blockPosDown);
                j = Math.max(j, this.getPower(blockStateDown, world, blockPosDown));
            }
        }
        return Math.max(i, j - 1);
    }

    protected abstract int getReceivedPower(World world, BlockPos pos);

    protected abstract int getPower(BlockState state, World world, BlockPos pos);

    @Override
    protected boolean shouldConnectToAbove(BlockView world, BlockPos posAbove, BlockState stateAbove, Direction direction) {
        return this.shouldConnectTo(world, posAbove, stateAbove, null);
    }

    @Override
    protected boolean shouldConnectToBelow(BlockView world, BlockPos posBelow, BlockState stateBelow, Direction direction) {
        return this.shouldConnectTo(world, posBelow, stateBelow, null);
    }

    @Override
    protected boolean shouldConnectTo(BlockView world, BlockPos pos, BlockState state, Direction direction) {
        if (state.isOf(this)) {
            return true;
        }
        else if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return state.get(PowerStones.POWER_PAIR) == this.getPowerPair();
        }
        else if (this.shouldNotConnectTo(state)) {
            return false;
        }
        else {
            return this.connectsTo(state, direction);
        }
    }

    protected abstract boolean shouldNotConnectTo(BlockState state);

    public int getColorForPower(int power) {
        Vec3d vec3d = COLORS[power];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    @Override
    protected boolean hasPowerOn(World world, BlockPos pos) {
        return world.getBlockState(pos).get(POWER) > 0;
    }

    @Override
    protected Vec3d getPowerstoneColor(BlockState state, World world, BlockPos pos, Random random) {
        return COLORS[state.get(POWER)];
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWER);
    }

    protected void placeOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        ((MultipleWiresBlock) PowerStones.MULTIPLE_WIRES).convertFromSingleWire(world, pos, state, player, hand);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
           return ActionResult.PASS;
        }
        else {
            ItemStack heldItemStack = player.getMainHandStack();

            if ((state.isOf(PowerStones.BLUESTONE_WIRE) && heldItemStack.isOf(Items.REDSTONE))
             || (state.isOf(PowerStones.GREENSTONE_WIRE) && heldItemStack.isOf(PowerStones.YELLOWSTONE))
             || (state.isOf(PowerStones.YELLOWSTONE_WIRE) && heldItemStack.isOf(PowerStones.GREENSTONE))) {
                // The player is holding a different dust item.
                // Convert this wire into a MultipleWiresBlock.
                this.placeOnUse(state, world, pos, player, hand);

                return ActionResult.SUCCESS;
            }

            if (isFullyConnected(state) || isNotConnected(state)) {
                BlockState blockstate = isFullyConnected(state) ? this.getDefaultState() : this.dotState;
                blockstate = blockstate.with(POWER, state.get(POWER));
                blockstate = this.getPlacementState(world, blockstate, pos);

                if (blockstate != state) {
                    world.setBlockState(pos, blockstate);
                    this.updateForNewState(world, pos, state, blockstate);

                    return ActionResult.SUCCESS;
                }
            }

            return ActionResult.PASS;
        }
    }

    public static boolean shouldBreakBlock(BlockState state, ItemStack heldItemStack) {
        if ((state.isOf(PowerStones.BLUESTONE_WIRE) && (heldItemStack.isOf(Items.REDSTONE) || heldItemStack.isOf(PowerStones.GREENSTONE) || heldItemStack.isOf(PowerStones.YELLOWSTONE)))
         || (state.isOf(PowerStones.GREENSTONE_WIRE) && (heldItemStack.isOf(Items.REDSTONE) || heldItemStack.isOf(PowerStones.BLUESTONE) || heldItemStack.isOf(PowerStones.YELLOWSTONE)))
         || (state.isOf(PowerStones.YELLOWSTONE_WIRE)) && (heldItemStack.isOf(Items.REDSTONE) || heldItemStack.isOf(PowerStones.BLUESTONE) || heldItemStack.isOf(PowerStones.GREENSTONE))) {
            return false;
        }
        return true;
    }

    public static int getWireColorRed(int powerLevel) {
        Vec3d vec3d = RED_COLORS[powerLevel];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    public static int getWireColorBlue(int powerLevel) {
        Vec3d vec3d = BLUE_COLORS[powerLevel];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    public static int getWireColorGreen(int powerLevel) {
        Vec3d vec3d = GREEN_COLORS[powerLevel];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    public static int getWireColorYellow(int powerLevel) {
        Vec3d vec3d = YELLOW_COLORS[powerLevel];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    public static int getWireColorWhite() {
        Vec3d vec3d = new Vec3d(1, 1, 1);
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

}
