package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.calvinmt.powerstones.LevelInterface;
import com.calvinmt.powerstones.LevelReaderInterface;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.PowerstoneWireBlockBase;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(DiodeBlock.class)
public abstract class DiodeBlockMixin extends HorizontalDirectionalBlock {

    protected DiodeBlockMixin(Properties properties) {
        super(properties);
    }

    @Redirect(method = "getInputSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I"))
    private int getInputSignalGetEmittingSignal(Level level, BlockPos pos, Direction direction) {
        return ((LevelInterface) level).getMaxSignal(pos, direction);
    }

    @Inject(method = "getInputSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At("TAIL"), cancellable = true)
    public void getPowerReturn(Level level, BlockPos pos, BlockState state, CallbackInfoReturnable<Integer> callbackInfo) {
        Direction direction = (Direction)state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction);
        BlockState blockState = level.getBlockState(blockPos);
        int power = 0;
        if (blockState.is(PowerStones.BLUESTONE_WIRE.get()) || blockState.is(PowerStones.GREENSTONE_WIRE.get()) || blockState.is(PowerStones.YELLOWSTONE_WIRE.get())) {
            power = blockState.getValue(PowerstoneWireBlockBase.POWER);
        }
        else if (blockState.is(PowerStones.MULTIPLE_WIRES.get())) {
            power = Math.max(blockState.getValue(MultipleWiresBlock.POWER), blockState.getValue(MultipleWiresBlock.POWER_B));
        }
        callbackInfo.setReturnValue(Math.max(callbackInfo.getReturnValue(), power));
    }

    @Inject(method = "getAlternateSignalAt(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/DiodeBlock;isAlternateInput(Lnet/minecraft/world/level/block/state/BlockState;)Z", shift = At.Shift.AFTER), cancellable = true)
    public void getAlternateSignalAtConditions(LevelReader level, BlockPos pos, Direction dir, CallbackInfoReturnable<Integer> callbackInfo) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.is(PowerStones.BLUESTONE_BLOCK.get()) || blockState.is(PowerStones.GREENSTONE_BLOCK.get()) || blockState.is(PowerStones.YELLOWSTONE_BLOCK.get())) {
            callbackInfo.setReturnValue(15);
        }
        else if (blockState.is(PowerStones.BLUESTONE_WIRE.get()) || blockState.is(PowerStones.GREENSTONE_WIRE.get()) || blockState.is(PowerStones.YELLOWSTONE_WIRE.get())) {
            callbackInfo.setReturnValue(blockState.getValue(PowerstoneWireBlockBase.POWER));
        }
        else if (blockState.is(PowerStones.MULTIPLE_WIRES.get())) {
            callbackInfo.setReturnValue(Math.max(blockState.getValue(MultipleWiresBlock.POWER), blockState.getValue(MultipleWiresBlock.POWER_B)));
        }
    }

    @Redirect(method = "getAlternateSignalAt(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/LevelReader;getDirectSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)I"))
    public int getAlternateSignalAtGetStrongPower(LevelReader level, BlockPos pos, Direction direction) {
        return Math.max(level.getDirectSignal(pos, direction), Math.max(((LevelReaderInterface) level).getDirectSignalBlue(pos, direction), Math.max(((LevelReaderInterface) level).getDirectSignalGreen(pos, direction), ((LevelReaderInterface) level).getDirectSignalYellow(pos, direction))));
    }

}
