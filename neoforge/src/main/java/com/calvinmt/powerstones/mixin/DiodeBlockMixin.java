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
import com.calvinmt.powerstones.block.PowerstoneWireBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
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
            power = blockState.getValue(PowerstoneWireBlock.POWER);
        }
        else if (blockState.is(PowerStones.MULTIPLE_WIRES.get())) {
            power = Math.max(MultipleWiresBlock.getPowerA(level, blockPos), MultipleWiresBlock.getPowerB(level, blockPos));
        }
        callbackInfo.setReturnValue(Math.max(callbackInfo.getReturnValue(), power));
    }

    @Redirect(method = "getAlternateSignal(Lnet/minecraft/world/level/SignalGetter;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/SignalGetter;getControlInputSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Z)I"))
    private int powerstones$getControlInputSignal(SignalGetter level, BlockPos pos, Direction direction, boolean sideInputDiodesOnly) {
        int power = level.getControlInputSignal(pos, direction, sideInputDiodesOnly);

        BlockState blockState = level.getBlockState(pos);

        // Repeaters only accept side input from other diode blocks.
        // Do not allow coloured dust or power blocks to bypass that rule.
        if (sideInputDiodesOnly && !(blockState.getBlock() instanceof DiodeBlock)) {
            return power;
        }

        if (blockState.is(PowerStones.BLUESTONE_BLOCK.get()) || blockState.is(PowerStones.GREENSTONE_BLOCK.get()) || blockState.is(PowerStones.YELLOWSTONE_BLOCK.get())) {
            power = Math.max(power, 15);
        }
        else if (blockState.is(PowerStones.BLUESTONE_WIRE.get()) || blockState.is(PowerStones.GREENSTONE_WIRE.get()) || blockState.is(PowerStones.YELLOWSTONE_WIRE.get())) {
            power = Math.max(power, blockState.getValue(PowerstoneWireBlock.POWER));
        }
        else if (blockState.is(PowerStones.MULTIPLE_WIRES.get()) && level instanceof Level actualLevel) {
            power = Math.max(power, Math.max(MultipleWiresBlock.getPowerA(actualLevel, pos), MultipleWiresBlock.getPowerB(actualLevel, pos)));
        }

        LevelReaderInterface powerstoneLevel = (LevelReaderInterface) level;

        power = Math.max(power, powerstoneLevel.getDirectSignalBlue(pos, direction));
        power = Math.max(power, powerstoneLevel.getDirectSignalGreen(pos, direction));
        power = Math.max(power, powerstoneLevel.getDirectSignalYellow(pos, direction));

        return power;
    }

}
