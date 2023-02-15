package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.calvinmt.powerstones.AbstractBlockInterface;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

@Mixin(AbstractBlock.class)
public abstract class AbstractBlockMixin implements AbstractBlockInterface {

    @Shadow
    public abstract int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction);
    @Shadow
    public abstract int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction);

    @ModifyConstant(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 0))
    private int getWeakRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "getStrongRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 0))
    private int getStrongRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

    @Override
    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getWeakRedstonePower(state, world, pos, direction);
    }

    @Override
    public int getStrongBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return getStrongRedstonePower(state, world, pos, direction);
    }

}
