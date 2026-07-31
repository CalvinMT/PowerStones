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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(RedStoneWireBlock.class)
public abstract class RedstoneWireBlockMixin extends Block implements RedstoneWireBlockInterface {

    @Shadow
    public static @Final EnumProperty<RedstoneSide> NORTH;
    @Shadow
    public static @Final EnumProperty<RedstoneSide> EAST;
    @Shadow
    public static @Final EnumProperty<RedstoneSide> SOUTH;
    @Shadow
    public static @Final EnumProperty<RedstoneSide> WEST;
    @Shadow
    public static @Final Map<Direction, EnumProperty<RedstoneSide>> PROPERTY_BY_DIRECTION;
    @Shadow
    private void updateNeighborsOfNeighboringWires(Level level, BlockPos pos) {}
    @Shadow
    private void updatePowerStrength(Level level, BlockPos pos, BlockState state, @Nullable Orientation orientation, boolean blockAdded) {}
    @Shadow
    private BlockState getConnectionState(BlockGetter level, BlockState state, BlockPos pos) { return null; };
    @Shadow
    private boolean shouldSignal;

    public RedstoneWireBlockMixin(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    public BlockState getPlacementState(BlockGetter level, BlockPos pos) {
        return this.getConnectionState(level, Blocks.REDSTONE_WIRE.defaultBlockState(), pos);
    }

    @Overwrite
    public void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int pFlags, int pRecursionLeft) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for(Direction direction : Direction.Plane.HORIZONTAL) {
            RedstoneSide redstoneside = state.getValue(PROPERTY_BY_DIRECTION.get(direction));
            mutable.setWithOffset(pos, direction);
            if (redstoneside != RedstoneSide.NONE
             && (! level.getBlockState(mutable).is(this) || ! this.isOtherConnectablePowerstone(level.getBlockState(mutable)))) {
                mutable.move(Direction.DOWN);
                BlockState blockstate = level.getBlockState(mutable);
                if (blockstate.is(this) || this.isOtherConnectablePowerstone(blockstate)) {
                    BlockPos blockpos = mutable.relative(direction.getOpposite());
                    BlockState newBlockstate = blockstate.updateShape(level, level, mutable, direction.getOpposite(), blockpos, level.getBlockState(blockpos), level.getRandom());
                    updateOrDestroy(blockstate, newBlockstate, level, mutable, pFlags, pRecursionLeft);
                }

                mutable.setWithOffset(pos, direction).move(Direction.UP);
                BlockState blockstate1 = level.getBlockState(mutable);
                if (blockstate1.is(this) || this.isOtherConnectablePowerstone(blockstate1)) {
                    BlockPos blockpos1 = mutable.relative(direction.getOpposite());
                    BlockState newBlockstate1 = blockstate1.updateShape(level, level, mutable, direction.getOpposite(), blockpos1, level.getBlockState(blockpos1), level.getRandom());
                    updateOrDestroy(blockstate1, newBlockstate1, level, mutable, pFlags, pRecursionLeft);
                }
            }
        }
    }

    private boolean isOtherConnectablePowerstone(BlockState state) {
        return state.is(PowerStones.MULTIPLE_WIRES.get()) && state.getValue(PowerStones.POWER_PAIR).hasRed();
    }

    @Redirect(method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canRedstoneConnectTo(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean checkConnectsToState(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return true;
        }
        else if (state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return state.getValue(PowerStones.POWER_PAIR).hasRed();
        }
        else if (state.is(PowerStones.BLUESTONE_WIRE.get()) || state.is(PowerStones.GREENSTONE_WIRE.get()) || state.is(PowerStones.YELLOWSTONE_WIRE.get())
         || state.is(PowerStones.BLUESTONE_BLOCK.get()) || state.is(PowerStones.GREENSTONE_BLOCK.get()) || state.is(PowerStones.YELLOWSTONE_BLOCK.get())
         || state.is(PowerStones.BLUESTONE_TORCH_BLOCK.get()) || state.is(PowerStones.BLUESTONE_WALL_TORCH.get())
         || state.is(PowerStones.GREENSTONE_TORCH_BLOCK.get()) || state.is(PowerStones.GREENSTONE_WALL_TORCH.get())
         || state.is(PowerStones.YELLOWSTONE_TORCH_BLOCK.get()) || state.is(PowerStones.YELLOWSTONE_WALL_TORCH.get())) {
            return false;
        }
        else {
            return state.canRedstoneConnectTo(level, pos, direction);
        }
    }

    public void setShouldSignal(boolean shouldSignal) {
        this.shouldSignal = shouldSignal;
    }

    @ModifyVariable(method = "getSignal(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", at = @At(value = "STORE"), ordinal = 0)
    public int modifyWeakRedstonePower(int original, BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return MultipleWiresBlock.getPowerForColour(state, level, pos, PowerColour.RED);
        }

        return original;
    }

    public int getDirectSignalBlue(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    public int getDirectSignalGreen(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    public int getDirectSignalYellow(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return 0;
    }

    public int getSignalBlue(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getSignalGreen(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getSignalYellow(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public void updateAll(BlockState state, Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            level.updateNeighborsAt(pos.relative(direction), (RedStoneWireBlock)(Object)this, null);
        }
        state = this.getConnectionState(level, state, pos);
        this.updatePowerStrength(level, pos, state, null, false);
        this.updateNeighborsOfNeighboringWires(level, pos);
        state = this.getConnectionState(level, level.getBlockState(pos), pos);
        level.setBlock(pos, state,  Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
    }

    private void placeOnUse(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand) {
        ((MultipleWiresBlock) PowerStones.MULTIPLE_WIRES.get()).convertFromSingleWire(level, pos, state, player, hand);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(PowerStones.BLUESTONE.get()) || stack.is(PowerStones.GREENSTONE.get()) || stack.is(PowerStones.YELLOWSTONE.get())) {
            this.placeOnUse(state, level, pos, player, hand);

            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, ItemStack stack, boolean willHarvest, FluidState fluid) {
        if (! RedstoneWireBlockInterface.shouldBreakBlock(state, stack)) {
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, stack, willHarvest, fluid);
    }

}
