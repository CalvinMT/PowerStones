package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.calvinmt.powerstones.PowerColour;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.RedstoneWireEvaluator;

@Mixin(RedstoneWireEvaluator.class)
public abstract class RedstoneControllerMixin {

    @WrapOperation(method = "getBlockSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/RedStoneWireBlock;getBlockSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I"))
    private int calculateTargetStrengthMultipleWiresShouldSignal(RedStoneWireBlock wire, Level level, BlockPos pos, Operation<Integer> original) {
        MultipleWiresBlock multipleWiresBlock = (MultipleWiresBlock) PowerStones.MULTIPLE_WIRES.get();

        multipleWiresBlock.setShouldSignal(false);

        try {
            return original.call(wire, level, pos);
        }
        finally {
            multipleWiresBlock.setShouldSignal(true);
        }
    }

    @WrapOperation(method = "getIncomingWireSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/redstone/RedstoneWireEvaluator;getWireSignal(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)I"))
    private int getWirePowerAt(RedstoneWireEvaluator evaluator, BlockPos wirePos, BlockState wireState, Operation<Integer> original, Level level, BlockPos pos) {
        if (wireState.is(PowerStones.MULTIPLE_WIRES.get())) {
            return MultipleWiresBlock.getPowerForColour(wireState, level, wirePos, PowerColour.RED);
        }

        return original.call(evaluator, wirePos, wireState);
    }

    /*
     * Preserve downward redstone propagation over other-colour power blocks
     * without allowing those blocks to power redstone directly.
     */
    @Inject(method = "getIncomingWireSignal(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)I", at = @At("RETURN"), cancellable = true)
    private void getWirePowerAboveOtherPowerBlocks(Level level, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
        BlockPos posAbove = pos.above();
        if (level.getBlockState(posAbove).isRedstoneConductor(level, posAbove)) {
            return;
        }

        int power = cir.getReturnValue();

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos = pos.relative(direction);
            BlockState blockState = level.getBlockState(blockPos);
            if (this.isOtherPowerBlock(blockState)) {
                BlockPos wirePos = blockPos.above();
                BlockState wireState = level.getBlockState(wirePos);
                int wirePower = 0;

                if (wireState.is(Blocks.REDSTONE_WIRE)) {
                    wirePower = wireState.getValue(RedStoneWireBlock.POWER);
                }
                else if (wireState.is(PowerStones.MULTIPLE_WIRES.get()) && wireState.getValue(PowerStones.POWER_PAIR).hasRed()) {
                    wirePower = MultipleWiresBlock.getPowerForColour(wireState, level, wirePos, PowerColour.RED);
                }

                power = Math.max(power, Math.max(0, wirePower - 1));
            }
        }

        cir.setReturnValue(power);
    }

    private boolean isOtherPowerBlock(BlockState state) {
        return state.is(PowerStones.BLUESTONE_BLOCK.get()) || state.is(PowerStones.GREENSTONE_BLOCK.get()) || state.is(PowerStones.YELLOWSTONE_BLOCK.get());
    }

}
