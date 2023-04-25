package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.Block;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

@Mixin(AliasedBlockItem.class)
public abstract class AliasedBlockItemMixin extends BlockItem {

    public AliasedBlockItemMixin(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group)) {
            stacks.add(new ItemStack(this));
        }
    }

}
