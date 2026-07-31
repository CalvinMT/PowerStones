package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.calvinmt.powerstones.PowerColour;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RedstoneController;
import net.minecraft.world.World;

@Mixin(RedstoneController.class)
public abstract class RedstoneControllerMixin {

    @WrapOperation(method = "getStrongPowerAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;getStrongPower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I"))
    private int calculateTargetStrengthMultipleWiresShouldSignal(RedstoneWireBlock wire, World world, BlockPos pos, Operation<Integer> original) {
        MultipleWiresBlock multipleWiresBlock = (MultipleWiresBlock) PowerStones.MULTIPLE_WIRES;

        multipleWiresBlock.setShouldSignal(false);

        try {
            return original.call(wire, world, pos);
        }
        finally {
            multipleWiresBlock.setShouldSignal(true);
        }
    }

    @WrapOperation(method = "calculateWirePowerAt(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/RedstoneController;getWirePowerAt(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I"))
    private int getWirePowerAt(RedstoneController controller, BlockPos wirePos, BlockState wireState, Operation<Integer> original, World world, BlockPos pos) {
        if (wireState.isOf(PowerStones.MULTIPLE_WIRES)) {
            return MultipleWiresBlock.getPowerForColour(wireState, world, wirePos, PowerColour.RED);
        }

        return original.call(controller, wirePos, wireState);
    }

}
