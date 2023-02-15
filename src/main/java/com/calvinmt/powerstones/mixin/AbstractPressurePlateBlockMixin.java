package com.calvinmt.powerstones.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant.Condition;

import net.minecraft.block.AbstractPressurePlateBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AbstractPressurePlateBlock.class)
public abstract class AbstractPressurePlateBlockMixin {

    @Shadow
    protected abstract int getRedstoneOutput(BlockState var1);

    @Shadow
    protected void updatePlateState(@Nullable Entity entity, World world, BlockPos pos, BlockState state, int output) { return; };

    @ModifyConstant(method = "getOutlineShape(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;", constant = @Constant(expandZeroConditions = Condition.GREATER_THAN_ZERO))
    private int getOutlineShapeMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "scheduledTick(Lnet/minecraft/block/BlockState;Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/random/Random;)V", constant = @Constant(expandZeroConditions = Condition.GREATER_THAN_ZERO))
    private int scheduledTickMinPower(int oldMinPower) {
        return 1;
    }

    /*@ModifyConstant(method = "onEntityCollision(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V", constant = @Constant(intValue = 0))
    private int onEntityCollisionMinPower(int oldMinPower) {
        return 1;
    }*/
    @Overwrite
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient) {
            return;
        }
        int i = this.getRedstoneOutput(state);
        if (i == 1) {
            this.updatePlateState(entity, world, pos, state, i);
        }
    }

    @ModifyConstant(method = "updatePlateState(Lnet/minecraft/entity/Entity;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)V", constant = @Constant(expandZeroConditions = Condition.GREATER_THAN_ZERO))
    private int updatePlateStateMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "onStateReplaced(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Z)V", constant = @Constant(expandZeroConditions = Condition.GREATER_THAN_ZERO))
    private int onStateReplacedMinPower(int oldMinPower) {
        return 1;
    }

    @ModifyConstant(method = "getStrongRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", constant = @Constant(intValue = 0))
    private int getStrongRedstonePowerMinPower(int oldMinPower) {
        return 1;
    }

}
