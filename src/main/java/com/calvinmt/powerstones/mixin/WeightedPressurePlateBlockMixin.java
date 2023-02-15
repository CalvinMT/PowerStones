package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.WeightedPressurePlateBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(WeightedPressurePlateBlock.class)
public class WeightedPressurePlateBlockMixin {

    @ModifyConstant(method = "<init>(ILnet/minecraft/block/AbstractBlock$Settings;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundEvent;)V", constant = @Constant(intValue = 0))
    public int initialiseMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "getRedstoneOutput(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", constant = @Constant(intValue = 0))
    private int getRedstoneOutputMinPower(int oldMinPower) {
        return 1;
    }

    @Inject(method = "getRedstoneOutput(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
    private void getRedstoneOutputMaxPower(World world, BlockPos pos, CallbackInfoReturnable<Integer> callbackInfo) {
        callbackInfo.setReturnValue(2);
    }

}
