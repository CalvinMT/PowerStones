package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.WorldInterface;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.PowerstoneWireBlockBase;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

@Mixin(AbstractRedstoneGateBlock.class)
public abstract class AbstractRedstoneGateBlockMixin extends HorizontalFacingBlock {

    protected AbstractRedstoneGateBlockMixin(Settings settings) {
        super(settings);
    }

    @Redirect(method = "getPower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I"))
    private int getPowerGetEmittingPower(World world, BlockPos pos, Direction direction) {
        return ((WorldInterface) world).getMaxPower(pos, direction);
    }

    @Inject(method = "getPower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I", at = @At("TAIL"), cancellable = true)
    public void getPowerReturn(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> callbackInfo) {
        Direction direction = (Direction)state.get(FACING);
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        int power = 0;
        if (blockState.isOf(PowerStones.BLUESTONE_WIRE) || blockState.isOf(PowerStones.GREENSTONE_WIRE) || blockState.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            power = blockState.get(PowerstoneWireBlockBase.POWER);
        }
        else if (blockState.isOf(PowerStones.MULTIPLE_WIRES)) {
            power = Math.max(blockState.get(MultipleWiresBlock.POWER), blockState.get(MultipleWiresBlock.POWER_B));
        }
        callbackInfo.setReturnValue(Math.max(callbackInfo.getReturnValue(), power));
    }

    @Inject(method = "getInputLevel(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractRedstoneGateBlock;isValidInput(Lnet/minecraft/block/BlockState;)Z", shift = At.Shift.AFTER), cancellable = true)
    public void getInputLevelConditions(WorldView world, BlockPos pos, Direction dir, CallbackInfoReturnable<Integer> callbackInfo) {
        BlockState blockState = world.getBlockState(pos);
        if (blockState.isOf(PowerStones.BLUESTONE_BLOCK) || blockState.isOf(PowerStones.GREENSTONE_BLOCK) || blockState.isOf(PowerStones.YELLOWSTONE_BLOCK)) {
            callbackInfo.setReturnValue(15);
        }
        else if (blockState.isOf(PowerStones.BLUESTONE_WIRE) || blockState.isOf(PowerStones.GREENSTONE_WIRE) || blockState.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            callbackInfo.setReturnValue(blockState.get(PowerstoneWireBlockBase.POWER));
        }
        else if (blockState.isOf(PowerStones.MULTIPLE_WIRES)) {
            callbackInfo.setReturnValue(Math.max(blockState.get(MultipleWiresBlock.POWER), blockState.get(MultipleWiresBlock.POWER_B)));
        }
    }

    @Redirect(method = "getInputLevel(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getStrongRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I"))
    public int getInputLevelGetStrongPower(WorldView world, BlockPos pos, Direction direction) {
        return Math.max(world.getStrongRedstonePower(pos, direction), Math.max(world.getStrongBluestonePower(pos, direction), Math.max(world.getStrongGreenstonePower(pos, direction), world.getStrongYellowstonePower(pos, direction))));
    }

}
