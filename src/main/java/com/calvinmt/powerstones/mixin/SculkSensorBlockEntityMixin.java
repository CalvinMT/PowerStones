package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.block.entity.SculkSensorBlockEntity;

@Mixin(SculkSensorBlockEntity.class)
public class SculkSensorBlockEntityMixin {

    @ModifyConstant(method = "getPower(FI)I", constant = @Constant(intValue = 1))
    private static int getPowerMinPower(int oldMinPower) {
        return 2;
    }

    @ModifyConstant(method = "getPower(FI)I", constant = @Constant(intValue = 15))
    private static int getPowerMaxPower(int oldMaxPower) {
        return 16;
    }

}
