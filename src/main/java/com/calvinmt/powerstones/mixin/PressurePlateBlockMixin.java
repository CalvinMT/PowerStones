package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import net.minecraft.block.PressurePlateBlock;

@Mixin(PressurePlateBlock.class)
public class PressurePlateBlockMixin {

    @ModifyConstant(method = "getRedstoneOutput(Lnet/minecraft/block/BlockState;)I", constant = @Constant(intValue = 0))
    private int getRedstoneOutputMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "getRedstoneOutput(Lnet/minecraft/block/BlockState;)I", constant = @Constant(intValue = 15))
    private int getRedstoneOutputMaxPower(int oldMaxPower) {
        return 2;
    }

}
