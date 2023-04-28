package com.calvinmt.powerstones;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface RedstoneWireBlockInterface {

    default void updateAll(BlockState state, Level level, BlockPos pos) {}

    default void setShouldSignal(boolean shouldSignal) {}

    static boolean canBreakFromHeldItem(BlockState state, ItemStack heldItemStack) {
        if (state.is(Blocks.REDSTONE_WIRE) && (heldItemStack.is(PowerStones.BLUESTONE.get()) || heldItemStack.is(PowerStones.GREENSTONE.get()) || heldItemStack.is(PowerStones.YELLOWSTONE.get()))) {
            return false;
        }
        return true;
    }

}
