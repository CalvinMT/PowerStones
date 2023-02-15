package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.calvinmt.powerstones.PowerStoneType;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ObserverBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin implements RedstoneWireBlockInterface {

    private PowerStoneType powerStoneType = PowerStoneType.RED;

    @Shadow
    private boolean wiresGivePower;

    @Shadow
    private int increasePower(BlockState state) { return 0; }

    private boolean isSamePowerStoneType (BlockState state) {
        boolean result = false;
        if ((state.isOf(Blocks.REDSTONE_WIRE) && powerStoneType.equals(PowerStoneType.RED))
         || (state.isOf(PowerStones.BLUESTONE_WIRE) && powerStoneType.equals(PowerStoneType.BLUE))
         || (state.isOf(PowerStones.GREENSTONE_WIRE) && powerStoneType.equals(PowerStoneType.GREEN))
         || (state.isOf(PowerStones.YELLOWSTONE_WIRE) && powerStoneType.equals(PowerStoneType.YELLOW))) {
            result = true;
        }
        return result;
    }

    private boolean isPowerStoneWire (BlockState state) {
        boolean result = false;
        if (state.isOf(Blocks.REDSTONE_WIRE)
         || state.isOf(PowerStones.BLUESTONE_WIRE)
         || state.isOf(PowerStones.GREENSTONE_WIRE)
         || state.isOf(PowerStones.YELLOWSTONE_WIRE)) {
            result = true;
        }
        return result;
    }

    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;)Z"))
    protected boolean connectsTo(BlockState state) {
        return this.connectsTo(state, null);
    }

    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z"))
    protected boolean connectsTo(BlockState state, Direction dir) {
        boolean result = false;
        if (isSamePowerStoneType(state)) {
            result = true;
        }
        else if (state.isOf(Blocks.REPEATER)) {
            Direction direction = state.get(RepeaterBlock.FACING);
            result = direction == dir || direction.getOpposite() == dir;
        }
        else if (state.isOf(Blocks.OBSERVER)) {
            result = dir == state.get(ObserverBlock.FACING);
        }
        else if (! isPowerStoneWire(state)) {
            result = state.emitsRedstonePower() && dir != null;
        }
        return result;
    }

    //@Inject(method = "getWeakRedstonePower(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At("HEAD"), cancellable = true)
    /*@Overwrite
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (! this.wiresGivePower || direction == Direction.DOWN) {
            return 0;
        }
        int i = state.get(RedstoneWireBlock.POWER);
        if (i == 0) {
            return 0;
        }
        if (direction == Direction.UP || ((WireConnection)this.getPlacementState(world, state, pos).get(RedstoneWireBlock.DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction.getOpposite()))).isConnected()) {
            return i;
        }
        return 0;
    }*/

    public int getReceivedRedstonePowerFromWorld(World world, BlockPos pos) {
        int i = 0;
        for (Direction direction : Direction.values()) {
            BlockState offsetBlockState = world.getBlockState(pos.offset(direction));
            if (isPowerStoneWire(offsetBlockState) && ! isSamePowerStoneType(offsetBlockState)) {
                continue;
            }
            int j = world.getEmittedRedstonePower(pos.offset(direction), direction);
            if (j >= 15) {
                return 15;
            }
            if (j <= i) continue;
            i = j;
        }
        return i;
    }

    @Overwrite
    private int getReceivedRedstonePower(World world, BlockPos pos) {
        this.wiresGivePower = false;
        int i = getReceivedRedstonePowerFromWorld(world, pos);
        this.wiresGivePower = true;
        int j = 0;
        if (i < 15) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                j = Math.max(j, this.increasePower(blockState));
                BlockPos blockPos2 = pos.up();
                if (blockState.isSolidBlock(world, blockPos) && !world.getBlockState(blockPos2).isSolidBlock(world, blockPos2)) {
                    j = Math.max(j, this.increasePower(world.getBlockState(blockPos.up())));
                    continue;
                }
                if (blockState.isSolidBlock(world, blockPos)) continue;
                j = Math.max(j, this.increasePower(world.getBlockState(blockPos.down())));
            }
        }
        return Math.max(i, j - 1);
    }

    @Override
    public void setPowerStoneType(PowerStoneType type) {
        powerStoneType = type;
    }

    @Override
    public PowerStoneType getPowerStoneType() {
        return powerStoneType;
    }

}
