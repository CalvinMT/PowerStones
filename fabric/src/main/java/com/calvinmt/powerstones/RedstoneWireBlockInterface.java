package com.calvinmt.powerstones;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface RedstoneWireBlockInterface {

    default void updateAll(BlockState state, World world, BlockPos pos) {}

    default void setShouldSignal(boolean shouldSignal) {}

    static boolean canBreakFromHeldItem(BlockState state, ItemStack heldItemStack) {
        if (state.isOf(Blocks.REDSTONE_WIRE) && (heldItemStack.isOf(PowerStones.BLUESTONE) || heldItemStack.isOf(PowerStones.GREENSTONE) || heldItemStack.isOf(PowerStones.YELLOWSTONE))) {
            return false;
        }
        return true;
    }

}
