package com.calvinmt.powerstones.mixin;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.PowerColour;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;
import com.calvinmt.powerstones.block.MultipleWiresBlock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.block.WireOrientation;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin extends Block implements RedstoneWireBlockInterface {

    @Shadow
    public static @Final EnumProperty<WireConnection> WIRE_CONNECTION_NORTH;
    @Shadow
    public static @Final EnumProperty<WireConnection> WIRE_CONNECTION_EAST;
    @Shadow
    public static @Final EnumProperty<WireConnection> WIRE_CONNECTION_SOUTH;
    @Shadow
    public static @Final EnumProperty<WireConnection> WIRE_CONNECTION_WEST;
    @Shadow
    public static @Final Map<Direction, EnumProperty<WireConnection>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY;
    @Shadow
    protected static boolean connectsTo(BlockState state, @Nullable Direction dir) { return false; }
    @Shadow
    private void updateOffsetNeighbors(World world, BlockPos pos) {};
    @Shadow
    private void update(World world, BlockPos pos, BlockState state, @Nullable WireOrientation orientation, boolean blockAdded) {};
    @Shadow
    private BlockState getPlacementState(BlockView world, BlockState state, BlockPos pos) { return null; }
    @Shadow
    private boolean wiresGivePower;

    public RedstoneWireBlockMixin(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public BlockState getConnectionState(BlockView world, BlockPos pos) {
        return this.getPlacementState(world, Blocks.REDSTONE_WIRE.getDefaultState(), pos);
    }

    @Overwrite
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int pFlags, int pRecursionLeft) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(Direction direction : Type.HORIZONTAL) {
            WireConnection redstoneside = state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
            mutable.set(pos, direction);
            if (redstoneside != WireConnection.NONE
             && (! world.getBlockState(mutable).isOf(this) || ! this.isOtherConnectablePowerstone(world.getBlockState(mutable)))) {
                mutable.move(Direction.DOWN);
                BlockState blockstate = world.getBlockState(mutable);
                if (blockstate.isOf(this) || this.isOtherConnectablePowerstone(blockstate)) {
                    BlockPos blockpos = mutable.offset(direction.getOpposite());
                    BlockState newBlockstate = blockstate.getStateForNeighborUpdate(world, world, mutable, direction.getOpposite(), blockpos, world.getBlockState(blockpos), world.getRandom());
                    replace(blockstate, newBlockstate, world, mutable, pFlags, pRecursionLeft);
                }

                mutable.set(pos, direction).move(Direction.UP);
                BlockState blockstate1 = world.getBlockState(mutable);
                if (blockstate1.isOf(this) || this.isOtherConnectablePowerstone(blockstate1)) {
                    BlockPos blockpos1 = mutable.offset(direction.getOpposite());
                    BlockState newBlockstate1 = blockstate1.getStateForNeighborUpdate(world, world, mutable, direction.getOpposite(), blockpos1, world.getBlockState(blockpos1), world.getRandom());
                    replace(blockstate1, newBlockstate1, world, mutable, pFlags, pRecursionLeft);
                }
            }
        }
    }

    private boolean isOtherConnectablePowerstone(BlockState state) {
        return state.isOf(PowerStones.MULTIPLE_WIRES) && state.get(PowerStones.POWER_PAIR).hasRed();
    }

    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean checkConnectsToState(BlockState state, Direction direction) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) {
            return true;
        }
        else if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return state.get(PowerStones.POWER_PAIR).hasRed();
        }
        else if (state.isOf(PowerStones.BLUESTONE_WIRE) || state.isOf(PowerStones.GREENSTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)
         || state.isOf(PowerStones.BLUESTONE_BLOCK) || state.isOf(PowerStones.GREENSTONE_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_BLOCK)
         || state.isOf(PowerStones.BLUESTONE_TORCH_BLOCK) || state.isOf(PowerStones.BLUESTONE_WALL_TORCH)
         || state.isOf(PowerStones.GREENSTONE_TORCH_BLOCK) || state.isOf(PowerStones.GREENSTONE_WALL_TORCH)
         || state.isOf(PowerStones.YELLOWSTONE_TORCH_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_WALL_TORCH)) {
            return false;
        }
        else {
            return connectsTo(state, direction);
        }
    }

    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;)Z"))
    private boolean checkConnectsToStateWithoutDirection(BlockState state) {
        return this.checkConnectsToState(state, null);
    }

    public void setShouldSignal(boolean wiresGivePower) {
        this.wiresGivePower = wiresGivePower;
    }

    @ModifyVariable(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At(value = "STORE"), ordinal = 0)
    public int modifyWeakRedstonePower(int original, BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return MultipleWiresBlock.getPowerForColour(state, world, pos, PowerColour.RED);
        }

        return original;
    }

    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public void updateAll(BlockState state, World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            world.updateNeighborsAlways(pos.offset(direction), (RedstoneWireBlock)(Object)this, null);
        }
        state = this.getPlacementState(world, state, pos);
        this.update(world, pos, state, null, false);
        this.updateOffsetNeighbors(world, pos);
        state = this.getPlacementState(world, world.getBlockState(pos), pos);
        world.setBlockState(pos, state,  Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
    }

    private void placeOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        ((MultipleWiresBlock) PowerStones.MULTIPLE_WIRES).convertFromSingleWire(world, pos, state, player, hand);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isOf(PowerStones.BLUESTONE) || stack.isOf(PowerStones.GREENSTONE) || stack.isOf(PowerStones.YELLOWSTONE)) {
            this.placeOnUse(state, world, pos, player, hand);

            return ActionResult.SUCCESS;
        }

        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

}
