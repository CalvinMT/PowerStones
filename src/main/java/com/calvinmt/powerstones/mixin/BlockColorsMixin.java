package com.calvinmt.powerstones.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.calvinmt.powerstones.block.PowerstoneWireBlock;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

@Mixin(BlockColors.class)
public class BlockColorsMixin {

    @Inject(method = "Lnet/minecraft/client/color/block/BlockColors;method_1688(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockRenderView;Lnet/minecraft/util/math/BlockPos;I)I", at = @At("HEAD"), cancellable = true,
		slice = @Slice(
			from = @At(value = "FIELD", args = "Lnet/minecraft/block/Blocks;WATER_CAULDRON"),
			to = @At(value = "FIELD", args = "Lnet/minecraft/block/Blocks;REDSTONE_WIRE")
		))
    private static void getRedstoneWireColor(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex, CallbackInfoReturnable<Integer> callbackInfo) {
		if (state.get(RedstoneWireBlock.POWER) > 0 && tintIndex == 0) {
			callbackInfo.setReturnValue(RedstoneWireBlock.getWireColor(state.get(RedstoneWireBlock.POWER)));
		}
		else if (state.get(PowerstoneWireBlock.POWER_B) > 0 && tintIndex == 1) {
			callbackInfo.setReturnValue(PowerstoneWireBlock.getWireColorBlue(state.get(PowerstoneWireBlock.POWER_B)));
		}
		else if (state.get(RedstoneWireBlock.POWER) > 0 && tintIndex == 2) {
			callbackInfo.setReturnValue(PowerstoneWireBlock.getWireColorGreen(state.get(RedstoneWireBlock.POWER)));
		}
		else if (state.get(PowerstoneWireBlock.POWER_B) > 0 && tintIndex == 3) {
			callbackInfo.setReturnValue(PowerstoneWireBlock.getWireColorYellow(state.get(PowerstoneWireBlock.POWER_B)));
		}
		else {
			callbackInfo.setReturnValue(PowerstoneWireBlock.getWireColorWhite());
		}
    }

}
