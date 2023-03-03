package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.PowerStones;
import com.llamalad7.mixinextras.injector.ModifyReceiver;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    private @Final MinecraftClient client;

    @Shadow
    public abstract boolean breakBlock(BlockPos pos);

    @Redirect(method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;method_41936(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;I)Lnet/minecraft/network/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakBlock(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean attackBlockCreativeBreakBlock(ClientPlayerInteractionManager clientPlayerInteractionManager, BlockPos pos) {
        BlockState blockState = this.client.world.getBlockState(pos);
        PowerStones.LOGGER.info("" + (blockState.isOf(Blocks.REDSTONE_WIRE)));
        if (blockState.isOf(Blocks.REDSTONE_WIRE)) {
            blockState.onBlockBreakStart(this.client.world, pos, this.client.player);
            blockState = this.client.world.getBlockState(pos);
            if (blockState.calcBlockBreakingDelta(this.client.player, this.client.player.world, pos) < 1.0f) {
                return false;
            }
        }
        return this.breakBlock(pos);
    }

    @ModifyReceiver(method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;method_41930(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;I)Lnet/minecraft/network/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
    private BlockState attackBlockUpdateBlockState(BlockState blockState, PlayerEntity player, BlockView world, BlockPos pos) {
        return this.client.world.getBlockState(pos);
    }

}
