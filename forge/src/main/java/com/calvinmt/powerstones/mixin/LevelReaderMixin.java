package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;

import com.calvinmt.powerstones.BlockStateBaseInterface;
import com.calvinmt.powerstones.LevelReaderInterface;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

@Mixin(LevelReader.class)
public interface LevelReaderMixin extends BlockGetter, LevelReaderInterface {

    @Override
    default public int getDirectSignalBlue(BlockPos pos, Direction direction) {
        return ((BlockStateBaseInterface) this.getBlockState(pos)).getDirectSignalBlue(this, pos, direction);
    }

    @Override
    default public int getDirectSignalGreen(BlockPos pos, Direction direction) {
        return ((BlockStateBaseInterface) this.getBlockState(pos)).getDirectSignalGreen(this, pos, direction);
    }

    @Override
    default public int getDirectSignalYellow(BlockPos pos, Direction direction) {
        return ((BlockStateBaseInterface) this.getBlockState(pos)).getDirectSignalYellow(this, pos, direction);
    }

}
