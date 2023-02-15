package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.block.SculkSensorBlock;

@Mixin(SculkSensorBlock.class)
public class SculkSensorBlockMixin {

    @ModifyConstant(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;I)V", constant = @Constant(intValue = 0))
    public int initialiseMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "onBlockAdded(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V", constant = @Constant(intValue = 0))
    private int onBlockAddedMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "setCooldown(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", constant = @Constant(intValue = 0))
    private static int setCooldownMinPower(int oldMinPower) {
        return 1;
    }

}
