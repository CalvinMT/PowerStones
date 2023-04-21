package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.WorldInterface;

import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(RedstoneLampBlock.class)
public class RedstoneLampBlockMixin {

    @Redirect(method = "getPlacementState(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean getPlacementStateIsReceivingPower(World world, BlockPos pos) {
        return ((WorldInterface) world).isReceivingPower(pos);
    }

    @Redirect(method = "neighborUpdate(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;Lnet/minecraft/util/math/BlockPos;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean neighborUpdateIsReceivingPower(World world, BlockPos pos) {
        return ((WorldInterface) world).isReceivingPower(pos);
    }

    @Redirect(method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean scheduledTickIsReceivingPower(ServerWorld world, BlockPos pos) {
        return ((WorldInterface) world).isReceivingPower(pos);
    }

}
