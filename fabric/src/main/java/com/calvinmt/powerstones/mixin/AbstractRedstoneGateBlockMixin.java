package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.calvinmt.powerstones.WorldInterface;

import net.minecraft.block.AbstractRedstoneGateBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(AbstractRedstoneGateBlock.class)
public abstract class AbstractRedstoneGateBlockMixin {

    @Redirect(method = "getPower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I"))
    private int getPowerGetEmittingPower(World world, BlockPos pos, Direction direction) {
        return ((WorldInterface) world).getMaxPower(pos, direction);
    }

}
