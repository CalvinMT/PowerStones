package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.google.common.collect.Sets;

import java.util.Random;
import java.util.Set;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class PowerstoneWireBlock extends PowerstoneWireBlockBase {

    private Vec3[] COLORS;

    public static final Vec3[] RED_COLORS = Util.make(new Vec3[16], (colors) -> {
        for(int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0F;
            float f1 = f * 0.6F + (f > 0.0F ? 0.4F : 0.3F);
            float f2 = Mth.clamp(f * f * 0.7F - 0.5F, 0.0F, 1.0F);
            float f3 = Mth.clamp(f * f * 0.6F - 0.7F, 0.0F, 1.0F);
            colors[i] = new Vec3((double)f1, (double)f2, (double)f3);
        }
    
        });
    public static final Vec3[] BLUE_COLORS = (Util.make(new Vec3[16], colors -> {
        colors[0] = new Vec3(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = Mth.clamp(f * f * 0.35f + 0.15f, 0.0f, 1.0f); // 0.15f - 0.5f
            float c = Mth.clamp(f * f * 0.4f + 0.2f, 0.0f, 1.0f); // 0.2f - 0.6f
            colors[i] = new Vec3(b, c, a);
        }
    }));
    public static final Vec3[] GREEN_COLORS = (Util.make(new Vec3[16], colors -> {
        colors[0] = new Vec3(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = Mth.clamp(f * f * 0.35f + 0.15f, 0.0f, 1.0f); // 0.15f - 0.5f
            float c = Mth.clamp(f * f * 0.25f, 0.0f, 1.0f); // 0.0f - 0.25f
            colors[i] = new Vec3(c, a, b);
        }
    }));
    public static final Vec3[] YELLOW_COLORS = (Util.make(new Vec3[16], colors -> {
        colors[0] = new Vec3(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = f * 0.5f + (f > 0.0f ? 0.3f : 0.2f); // 0.2f - 0.8f
            float c = Mth.clamp(f * f * 0.4f + 0.2f, 0.0f, 1.0f); // 0.2f - 0.6f
            colors[i] = new Vec3(a, b, c);
        }
    }));

    public PowerstoneWireBlock(BlockBehaviour.Properties properties, Vec3[] colors) {
        super(properties);
        COLORS = colors;
        this.registerDefaultState(this.stateDefinition.any().setValue(NORTH, RedstoneSide.NONE).setValue(EAST, RedstoneSide.NONE).setValue(SOUTH, RedstoneSide.NONE).setValue(WEST, RedstoneSide.NONE).setValue(POWER, Integer.valueOf(0)));
    }

    @Override
    protected BlockState getDefaultBlockStateWithPowerProperties(BlockState state) {
        return this.defaultBlockState().setValue(POWER, state.getValue(POWER));
    }

    @Override
    protected BlockState getCrossStateWithPowerProperties(BlockState state) {
        return this.crossState.setValue(POWER, state.getValue(POWER));
    }

    @Override
    protected boolean isOtherConnectablePowerstone(BlockState state) {
        if (state.is(PowerStones.MULTIPLE_WIRES.get()) && state.getValue(PowerStones.POWER_PAIR) == this.getPowerPair()) {
            return true;
        }
        return false;
    }

    protected abstract PowerPair getPowerPair();

    @Override
    protected void updatePowerStrength(Level level, BlockPos pos, BlockState state) {
        int i = this.calculateTargetStrength(level, pos);
        if (state.getValue(POWER) != i) {
            if (level.getBlockState(pos) == state) {
                level.setBlock(pos, state.setValue(POWER, Integer.valueOf(i)), 2);
            }

            Set<BlockPos> set = Sets.newHashSet();
            set.add(pos);

            for(Direction direction : Direction.values()) {
                set.add(pos.relative(direction));
            }

            for(BlockPos blockpos : set) {
                level.updateNeighborsAt(blockpos, this);
            }
        }
    }

    private int calculateTargetStrength(Level level, BlockPos pos) {
        shouldSignal = false;
        ((MultipleWiresBlock)PowerStones.MULTIPLE_WIRES.get()).setShouldSignal(false);
        int i = this.getBestNeighborSignal(level, pos);
        shouldSignal = true;
        ((MultipleWiresBlock)PowerStones.MULTIPLE_WIRES.get()).setShouldSignal(true);
        int j = 0;
        if (i < 15) {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos blockPos = pos.relative(direction);
                BlockState blockState = level.getBlockState(blockPos);
                j = Math.max(j, this.getWireSignal(blockState));
                BlockPos blockPos2 = pos.above();
                if (blockState.isRedstoneConductor(level, blockPos) && !level.getBlockState(blockPos2).isRedstoneConductor(level, blockPos2)) {
                    j = Math.max(j, this.getWireSignal(level.getBlockState(blockPos.above())));
                    continue;
                }
                if (blockState.isRedstoneConductor(level, blockPos)) continue;
                j = Math.max(j, this.getWireSignal(level.getBlockState(blockPos.below())));
            }
        }
        return Math.max(i, j - 1);
    }

    protected abstract int getBestNeighborSignal(Level level, BlockPos pos);

    protected abstract int getWireSignal(BlockState state);

    @Override
    protected boolean shouldConnectToAbove(BlockGetter level, BlockPos posAbove, BlockState stateAbove, Direction direction) {
        return this.shouldConnectTo(level, posAbove, stateAbove, direction);
    }

    @Override
    protected boolean shouldConnectToBelow(BlockGetter level, BlockPos posBelow, BlockState stateBelow, Direction direction) {
        return this.shouldConnectTo(level, posBelow, stateBelow, direction);
    }

    @Override
    protected boolean shouldConnectTo(BlockGetter level, BlockPos pos, BlockState state, Direction direction) {
        if (state.is(this)) {
            return true;
        }
        else if (state.is(PowerStones.MULTIPLE_WIRES.get())) {
            return state.getValue(PowerStones.POWER_PAIR) == this.getPowerPair();
        }
        else if (this.shouldNotConnectTo(state)) {
            return false;
        }
        else {
            return state.canRedstoneConnectTo(level, pos, direction);
        }
    }

    protected abstract boolean shouldNotConnectTo(BlockState state);

    public int getColorForPower(int power) {
        Vec3 vec3 = COLORS[power];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    @Override
    protected boolean hasPowerOn(BlockState state) {
        return state.getValue(POWER) > 0;
    }

    @Override
    protected Vec3 getPowerstoneColor(BlockState state, Random random) {
        return COLORS[state.getValue(POWER)];
    }

    protected void placeOnUse(BlockState state, Level level, BlockPos pos, Player player) {
        SoundType soundType = state.getSoundType();
        level.playSound(player, pos, state.getSoundType().getPlaceSound(), SoundSource.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
        if (player == null || !player.getAbilities().instabuild) {
            player.getMainHandItem().shrink(1);
        }
        if (player.getMainHandItem().is(Items.REDSTONE)) {
            state = PowerStones.MULTIPLE_WIRES.get().defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(POWER, 0).setValue(PowerStones.POWER_B, state.getValue(POWER)).setValue(PowerStones.POWER_PAIR, PowerPair.RED_BLUE);
        }
        else if (player.getMainHandItem().is(PowerStones.BLUESTONE.get())) {
            state = PowerStones.MULTIPLE_WIRES.get().defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(POWER, state.getValue(POWER)).setValue(PowerStones.POWER_B, 0).setValue(PowerStones.POWER_PAIR, PowerPair.RED_BLUE);
        }
        else if (player.getMainHandItem().is(PowerStones.GREENSTONE.get())) {
            state = PowerStones.MULTIPLE_WIRES.get().defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(POWER, 0).setValue(PowerStones.POWER_B, state.getValue(POWER)).setValue(PowerStones.POWER_PAIR, PowerPair.GREEN_YELLOW);
        }
        else if (player.getMainHandItem().is(PowerStones.YELLOWSTONE.get())) {
            state = PowerStones.MULTIPLE_WIRES.get().defaultBlockState().setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(SOUTH, state.getValue(SOUTH)).setValue(WEST, state.getValue(WEST)).setValue(POWER, state.getValue(POWER)).setValue(PowerStones.POWER_B, 0).setValue(PowerStones.POWER_PAIR, PowerPair.GREEN_YELLOW);
        }
        level.setBlock(pos, state, Block.UPDATE_ALL | Block.UPDATE_IMMEDIATE);
        ((MultipleWiresBlock)state.getBlock()).updateAll(state, level, pos);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.getAbilities().mayBuild) {
           return InteractionResult.PASS;
        } else {
            if ((state.is(PowerStones.BLUESTONE_WIRE.get()) && player.getMainHandItem().is(Items.REDSTONE))
             || (state.is(PowerStones.GREENSTONE_WIRE.get()) && player.getMainHandItem().is(PowerStones.YELLOWSTONE.get()))
             || (state.is(PowerStones.YELLOWSTONE_WIRE.get()) && player.getMainHandItem().is(PowerStones.GREENSTONE.get()))) {
                this.placeOnUse(state, level, pos, player);
                return InteractionResult.SUCCESS;
            }
            if (isCross(state) || isDot(state)) {
                BlockState blockstate = isCross(state) ? this.defaultBlockState() : this.crossState;
                blockstate = blockstate.setValue(POWER, state.getValue(POWER));
                blockstate = this.getConnectionState(level, blockstate, pos);
                if (blockstate != state) {
                    level.setBlockAndUpdate(pos, blockstate);
                    this.updatesOnShapeChange(level, pos, state, blockstate);
                    return InteractionResult.SUCCESS;
                }
            }

            return InteractionResult.PASS;
        }
    }

    public static boolean canBreakFromHeldItem(BlockState state, ItemStack heldItemStack) {
        if ((state.is(Blocks.REDSTONE_WIRE) && (heldItemStack.is(PowerStones.BLUESTONE.get()) || heldItemStack.is(PowerStones.GREENSTONE.get()) || heldItemStack.is(PowerStones.YELLOWSTONE.get())))
         || (state.is(PowerStones.BLUESTONE_WIRE.get()) && (heldItemStack.is(Items.REDSTONE) || heldItemStack.is(PowerStones.GREENSTONE.get()) || heldItemStack.is(PowerStones.YELLOWSTONE.get())))
         || (state.is(PowerStones.GREENSTONE_WIRE.get()) && (heldItemStack.is(Items.REDSTONE) || heldItemStack.is(PowerStones.BLUESTONE.get()) || heldItemStack.is(PowerStones.YELLOWSTONE.get())))
         || (state.is(PowerStones.YELLOWSTONE_WIRE.get())) && (heldItemStack.is(Items.REDSTONE) || heldItemStack.is(PowerStones.BLUESTONE.get()) || heldItemStack.is(PowerStones.GREENSTONE.get()))) {
            return false;
        }
        return true;
    }

    public static int getWireColorRed(int powerLevel) {
        Vec3 vec3 = PowerstoneWireBlock.RED_COLORS[powerLevel];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    public static int getWireColorBlue(int powerLevel) {
        Vec3 vec3 = PowerstoneWireBlock.BLUE_COLORS[powerLevel];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    public static int getWireColorGreen(int powerLevel) {
        Vec3 vec3 = PowerstoneWireBlock.GREEN_COLORS[powerLevel];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    public static int getWireColorYellow(int powerLevel) {
        Vec3 vec3 = PowerstoneWireBlock.YELLOW_COLORS[powerLevel];
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

    public static int getWireColorWhite() {
        Vec3 vec3 = new Vec3(1, 1, 1);
        return Mth.color((float)vec3.x(), (float)vec3.y(), (float)vec3.z());
    }

}
