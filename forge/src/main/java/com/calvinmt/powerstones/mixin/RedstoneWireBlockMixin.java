package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;
import com.calvinmt.powerstones.block.MultipleWiresBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.material.FluidState;
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
    public static @Final IntegerProperty POWER;
    @Shadow
    private void updateNeighborsOfNeighboringWires(Level level, BlockPos pos) {}
    @Shadow
    private void updatePowerStrength(Level pLevel, BlockPos pPos, BlockState pState) {}
    @Shadow
    private BlockState getConnectionState(BlockGetter level, BlockState state, BlockPos pos) { return null; };
    @Shadow
    private boolean shouldSignal;

    public RedstoneWireBlockMixin(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Redirect(method = "updateIndirectNeighbourShapes(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"))
    private boolean updateIndirectNeighbourShapesIs(BlockState state, Block block) {
        return state.is(block) || (state.is(PowerStones.MULTIPLE_WIRES.get()) && state.getValue(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE);
    }

    @Redirect(method = "getConnectingSide(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)Lnet/minecraft/world/level/block/state/properties/RedstoneSide;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;canRedstoneConnectTo(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"))
    private boolean checkConnectsToState(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return true;
        }
        else if (state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return state.getValue(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE;
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

    @Redirect(method = "calculateTargetStrength(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBestNeighborSignal(Lnet/minecraft/core/BlockPos;)I"))
    private int calculateTargetStrengthMultipleWiresShouldSignal(Level level, BlockPos pos) {
        int result = 0;
        ((MultipleWiresBlock)PowerStones.MULTIPLE_WIRES.get()).setShouldSignal(false);
        result = level.getBestNeighborSignal(pos);
        ((MultipleWiresBlock)PowerStones.MULTIPLE_WIRES.get()).setShouldSignal(true);
        return result;
    }

    @ModifyConstant(method = "getWireSignal(Lnet/minecraft/world/level/block/state/BlockState;)I", constant = @Constant(intValue = 0))
    private int getWireSignalMultipleWires(int oldResult, BlockState state) {
        if (state.is(PowerStones.MULTIPLE_WIRES.get()) && state.getValue(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE) {
            return state.getValue(POWER);
        }
        return oldResult;
    }

    @Inject(method = "use(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/BlockHitResult;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;isCross(Lnet/minecraft/world/level/block/state/BlockState;)Z", ordinal = 0), cancellable = true)
    public void useAddColor(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> callbackInfo) {
        if (player.getMainHandItem().is(PowerStones.BLUESTONE.get())) {
            this.placeOnUse(state, level, pos, player);
            callbackInfo.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    public void setShouldSignal(boolean shouldSignal) {
        this.shouldSignal = shouldSignal;
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
            level.updateNeighborsAt(pos.relative(direction), (RedStoneWireBlock)(Object)this);
        }
        state = this.getConnectionState(level, state, pos);
        this.updatePowerStrength(level, pos, state);
        this.updateNeighborsOfNeighboringWires(level, pos);
        state = this.getConnectionState(level, level.getBlockState(pos), pos);
        level.setBlock(pos, state,  Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
    }

    private void placeOnUse(BlockState state, Level level, BlockPos pos, Player player) {
        SoundType soundType = state.getSoundType();
        level.playSound(player, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
        if (player == null || !player.getAbilities().instabuild) {
            player.getMainHandItem().shrink(1);
        }
        state = PowerStones.MULTIPLE_WIRES.get().defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(POWER, state.getValue(POWER)).setValue(PowerStones.POWER_B, 0).setValue(PowerStones.POWER_PAIR, PowerPair.RED_BLUE);
        level.setBlock(pos, state, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
        ((MultipleWiresBlock)state.getBlock()).updateAll(state, level, pos);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (! RedstoneWireBlockInterface.canBreakFromHeldItem(state, player.getMainHandItem())) {
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

}
