package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.WorldInterface;

import net.minecraft.block.AbstractRailBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AbstractRailBlock.class)
public class AbstractRailBlockMixin {

    @Redirect(method = "updateBlockState(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isReceivingRedstonePower(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean updateBlockStateIsReceivingPower(World world, BlockPos pos) {
        return ((WorldInterface) world).isReceivingPower(pos);
    }

}
