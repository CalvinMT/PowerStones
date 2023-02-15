package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.entity.SculkSensorBlockEntity;

@Mixin(SculkSensorBlockEntity.class)
public class SculkSensorBlockEntityMixin {

    @Inject(method = "getPower(FI)I", at = @At("RETURN"), cancellable = true)
    private static void getPowerReturn(float distance, int range, CallbackInfoReturnable<Integer> callbackInfo) {
        callbackInfo.setReturnValue(2);
    }

}
