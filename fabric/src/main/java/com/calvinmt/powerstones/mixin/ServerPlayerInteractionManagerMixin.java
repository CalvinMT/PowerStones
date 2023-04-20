package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.llamalad7.mixinextras.injector.ModifyReceiver;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(ServerPlayerInteractionManager.class)
public abstract class ServerPlayerInteractionManagerMixin {

    @Shadow
    protected ServerWorld world;
    @Shadow
    protected @Final ServerPlayerEntity player;

    @Shadow
    public abstract void finishMining(BlockPos pos, int sequence, String reason);

    @Redirect(method = "processBlockBreakingAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/Direction;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerInteractionManager;finishMining(Lnet/minecraft/util/math/BlockPos;ILjava/lang/String;)V"))
    private void processBlockBreakingActionCreativeFinishMining(ServerPlayerInteractionManager serverPlayerInteractionManager, BlockPos pos, int sequence, String reason) {
        BlockState blockState = this.world.getBlockState(pos);
        if (blockState.isOf(Blocks.REDSTONE_WIRE)) {
            blockState.onBlockBreakStart(this.world, pos, this.player);
            blockState = this.world.getBlockState(pos);
            if (blockState.calcBlockBreakingDelta(this.player, this.player.world, pos) >= 1.0f) {
                this.finishMining(pos, sequence, "creative destroy");
            }
        }
        else {
            this.finishMining(pos, sequence, "creative destroy");
        }
    }

    @ModifyReceiver(method = "processBlockBreakingAction(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/network/packet/c2s/play/PlayerActionC2SPacket$Action;Lnet/minecraft/util/math/Direction;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;calcBlockBreakingDelta(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
    private BlockState processBlockBreakingActionUpdateBlockState(BlockState blockState, PlayerEntity player, BlockView world, BlockPos pos) {
        return this.world.getBlockState(pos);
    }

}
