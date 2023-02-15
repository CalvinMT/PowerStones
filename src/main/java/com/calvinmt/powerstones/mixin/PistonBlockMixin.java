package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.calvinmt.powerstones.WorldInterface;

import net.minecraft.block.PistonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(PistonBlock.class)
public class PistonBlockMixin {

    @Redirect(method = "shouldExtend(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isEmittingRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", ordinal = 0))
    private boolean shouldExtendIsEmittingPower0(World world, BlockPos pos, Direction direction) {
        return ((WorldInterface) world).isEmittingPower(pos, direction);
    }

    @Redirect(method = "shouldExtend(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isEmittingRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", ordinal = 1))
    private boolean shouldExtendIsEmittingPower1(World world, BlockPos pos, Direction direction) {
        return ((WorldInterface) world).isEmittingPower(pos, direction);
    }

    @Redirect(method = "shouldExtend(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isEmittingRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z", ordinal = 2))
    private boolean shouldExtendIsEmittingPower2(World world, BlockPos pos, Direction direction) {
        return ((WorldInterface) world).isEmittingPower(pos, direction);
    }

}
