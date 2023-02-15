package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.block.WeightedPressurePlateBlock;

@Mixin(WeightedPressurePlateBlock.class)
public class WeightedPressurePlateBlockMixin {

    @ModifyConstant(method = "<init>(ILnet/minecraft/block/AbstractBlock$Settings;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundEvent;)V", constant = @Constant(intValue = 0))
    public int initialiseMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyArg(method = "getRedstoneOutput(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;ceil(F)I"))
    private float getRedstoneOutputMaxPower(float oldOutput) {
        return oldOutput + 1.0f;
    }

    @ModifyConstant(method = "getRedstoneOutput(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", constant = @Constant(intValue = 0))
    private int getRedstoneOutputMinPower(int oldMinPower) {
        return 1;
    }

}
