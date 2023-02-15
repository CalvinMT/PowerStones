package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Constant.Condition;
import com.calvinmt.powerstones.WorldInterface;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(AbstractRedstoneGateBlock.class)
public abstract class AbstractRedstoneGateBlockMixin {

    @ModifyConstant(method = "hasPower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", constant = @Constant(expandZeroConditions = Condition.GREATER_THAN_ZERO))
    private int hasPowerMinPower(int oldMinPower) {
        return 1;
    }

    @Redirect(method = "getPower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I"))
    private int getPowerGetEmittingPower(World world, BlockPos pos, Direction direction) {
        return ((WorldInterface) world).isEmittingPower(pos, direction) ? 2 : 1;
    }

    @ModifyArg(method = "getPower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"), index = 1)
    private int getPowerArg1(int oldValue) {
        return 1;
    }

    @ModifyConstant(method = "getInputLevel(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 15))
    private int getInputLevelMaxPower(int oldMaxPower) {
        return 2;
    }

    @ModifyConstant(method = "getOutputLevel(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I", constant = @Constant(intValue = 15))
    private int getOutputLevelMaxPower(int oldMaxPower) {
        return 2;
    }

}
