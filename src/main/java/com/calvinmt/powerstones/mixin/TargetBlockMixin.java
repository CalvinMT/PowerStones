package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import net.minecraft.block.TargetBlock;

@Mixin(TargetBlock.class)
public class TargetBlockMixin {

    @ModifyConstant(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V", constant = @Constant(intValue = 0))
    public int initialisePowerStoneProperties(int oldMinPower) {
        return 1;
    }

    @ModifyVariable(method = "trigger(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/block/BlockState;Lnet/minecraft/util/hit/BlockHitResult;Lnet/minecraft/entity/Entity;)I", at = @At("STORE"))
    private static int triggerSetHitPower(int oldHitPower) {
        return 2;
    }

    @ModifyConstant(method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", constant = @Constant(intValue = 0))
    private int scheduledTickMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "onBlockAdded(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V", constant = @Constant(intValue = 0))
    private int onBlockAddedMinPower(int oldMinPower) {
        return 1;
    }

}
