package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.LevelInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.PoweredRailBlock;

@Mixin(PoweredRailBlock.class)
public class PoweredRailBlockMixin {

    @Redirect(method = "isSameRailWithPower(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ZILnet/minecraft/world/level/block/state/properties/RailShape;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasNeighborSignal(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean isSameRailWithPowerIsReceivingSignal(Level level, BlockPos pos) {
        return ((LevelInterface) level).isReceivingSignal(pos);
    }

    @Redirect(method = "updateState(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;hasNeighborSignal(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean updateBlockStateIsReceivingSignal(Level level, BlockPos pos) {
        return ((LevelInterface) level).isReceivingSignal(pos);
    }

}
