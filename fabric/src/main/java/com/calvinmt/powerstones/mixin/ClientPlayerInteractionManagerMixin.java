package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.llamalad7.mixinextras.injector.ModifyReceiver;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

    @Shadow
    private @Final MinecraftClient client;

    @Shadow
    public abstract boolean breakBlock(BlockPos pos);

    @Redirect(method = "breakBlock(Lnet/minecraft/util/math/BlockPos;)Z", at = @At(value = "INVOKE", target = "net.minecraft.block.Block.onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"))
    private void breakBlockOnBreakRedstoneWireBlock(Block block, World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (! state.isOf(Blocks.REDSTONE_WIRE)) {
            block.onBreak(world, pos, state, this.client.player);
        }
        else if (state.isOf(Blocks.REDSTONE_WIRE) && (state.get(RedstoneWireBlock.POWER) < 16 || state.get(PowerstoneWireBlock.POWER_B) < 16) && ! (player.getMainHandStack().isOf(Items.REDSTONE) || player.getMainHandStack().isOf(PowerStones.BLUESTONE) || player.getMainHandStack().isOf(PowerStones.GREENSTONE) || player.getMainHandStack().isOf(PowerStones.YELLOWSTONE))) {
            block.onBreak(world, pos, state, this.client.player);
        }
    }

    @Redirect(method = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;method_41936(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;I)Lnet/minecraft/network/Packet;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;breakBlock(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean attackBlockCreativeBreakBlock(ClientPlayerInteractionManager clientPlayerInteractionManager, BlockPos pos) {
        BlockState blockState = this.client.world.getBlockState(pos);
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
