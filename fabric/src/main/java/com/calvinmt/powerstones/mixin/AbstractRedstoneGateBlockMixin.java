package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.WorldInterface;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneView;
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
            power = blockState.get(PowerstoneWireBlock.POWER);
        }
        else if (blockState.isOf(PowerStones.MULTIPLE_WIRES)) {
            power = Math.max(MultipleWiresBlock.getPowerA(world, blockPos), MultipleWiresBlock.getPowerB(world, blockPos));
        }
        callbackInfo.setReturnValue(Math.max(callbackInfo.getReturnValue(), power));
    }

    @WrapOperation(method = "getMaxInputLevelSides(Lnet/minecraft/world/RedstoneView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I", at = @At( value = "INVOKE", target = "Lnet/minecraft/world/RedstoneView;getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)I" ))
    public int includeColouredSideInput(RedstoneView world, BlockPos pos, Direction dir, boolean onlyFromGate, Operation<Integer> original) {
        int power = original.call(world, pos, dir, onlyFromGate);

        // Repeaters only accept side input from other diode blocks.
        // Do not allow coloured dust or power blocks to bypass that rule.
        if (onlyFromGate) {
            return power;
        }

        BlockState state = world.getBlockState(pos);
        int colouredPower = 0;

        if (state.isOf(PowerStones.BLUESTONE_BLOCK) || state.isOf(PowerStones.GREENSTONE_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_BLOCK)) {
            colouredPower = 15;
        }
        else if (state.isOf(PowerStones.BLUESTONE_WIRE) || state.isOf(PowerStones.GREENSTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            colouredPower = state.get(PowerstoneWireBlock.POWER);
        }
        else if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            WorldView worldView = (WorldView) world;
            colouredPower = Math.max(MultipleWiresBlock.getPowerA(worldView, pos), MultipleWiresBlock.getPowerB(worldView, pos));
        }

        if (world instanceof WorldInterface powerWorld) {
            colouredPower = Math.max(colouredPower, powerWorld.getMaxPower(pos, dir));
        }

        return Math.max(power, colouredPower);
    }

}
