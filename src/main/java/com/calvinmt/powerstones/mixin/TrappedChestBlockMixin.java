package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.TrappedChestBlock;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@Mixin(TrappedChestBlock.class)
public class TrappedChestBlockMixin {

    @Inject(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At("RETURN"), cancellable = true)
    private void getWeakRedstonePowerSetPower(BlockState state, BlockView world, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> callbackInfo) {
        callbackInfo.setReturnValue(ChestBlockEntity.getPlayersLookingInChestCount(world, pos) > 0 ? 2 : 1);
    }

    @ModifyConstant(method = "getStrongRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 0))
    private int getStrongRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

}
