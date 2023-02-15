package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import net.minecraft.block.DaylightDetectorBlock;

@Mixin(DaylightDetectorBlock.class)
public class DaylightDetectorBlockMixin {

    @ModifyConstant(method = "<init>(Lnet/minecraft/block/AbstractBlock$Settings;)V", constant = @Constant(intValue = 0))
    public int initialiseMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "updateState(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", constant = @Constant(intValue = 0))
    private static int updateStateMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "updateState(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V", constant = @Constant(intValue = 15, ordinal = 1))
    private static int updateStateMaxPower(int oldMaxPower) {
        return 16;
    }

}
