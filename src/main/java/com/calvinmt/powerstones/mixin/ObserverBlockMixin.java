package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.block.ObserverBlock;

@Mixin(ObserverBlock.class)
public class ObserverBlockMixin {

    @ModifyConstant(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 0))
    private int getWeakRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 15))
    private int getWeakRedstonePowerMaxPower(int oldMaxPower) {
        return 16;
    }

}
