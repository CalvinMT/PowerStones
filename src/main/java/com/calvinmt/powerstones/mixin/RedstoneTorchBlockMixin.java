package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@Mixin(RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin {

    @ModifyConstant(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 0))
    private int getWeakRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 15))
    private int getWeakRedstonePowerMaxPower(int oldMaxPower) {
        return 16;
    }

    @Redirect(method = "shouldUnpower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;isEmittingRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean shouldUnpowerIsEmittingPower(World world, BlockPos pos, Direction direction) {
        return world.isEmittingPower(pos, direction);
    }

    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 1;
    }

    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 1;
    }

    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 1;
    }

}
