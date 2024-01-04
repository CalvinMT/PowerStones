package com.calvinmt.powerstones.mixin;

import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;
import com.calvinmt.powerstones.block.MultipleWiresBlock;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mixin(RedstoneWireBlock.class)
public abstract class RedstoneWireBlockMixin extends Block implements RedstoneWireBlockInterface {

    @Shadow
    public static @Final EnumProperty<WireConnection> WIRE_CONNECTION_NORTH;
    @Shadow
    public static @Final EnumProperty<WireConnection> WIRE_CONNECTION_EAST;
    @Shadow
    public static @Final EnumProperty<WireConnection> WIRE_CONNECTION_SOUTH;
    @Shadow
    public static @Final EnumProperty<WireConnection> WIRE_CONNECTION_WEST;
    @Shadow
    public static @Final IntProperty POWER;
    @Shadow
    public static @Final Map<Direction, EnumProperty<WireConnection>> DIRECTION_TO_WIRE_CONNECTION_PROPERTY;
    @Shadow
    protected static boolean connectsTo(BlockState state, @Nullable Direction dir) { return false; }
    @Shadow
    private void updateOffsetNeighbors(World world, BlockPos pos) {};
    @Shadow
    private void update(World world, BlockPos pos, BlockState state) {};
    @Shadow
    private BlockState getPlacementState(BlockView world, BlockState state, BlockPos pos) { return null; }
    @Shadow
    private boolean wiresGivePower;

    public RedstoneWireBlockMixin(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Overwrite
    public void prepare(BlockState state, WorldAccess world, BlockPos pos, int pFlags, int pRecursionLeft) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for(Direction direction : Type.HORIZONTAL) {
            WireConnection redstoneside = state.get(DIRECTION_TO_WIRE_CONNECTION_PROPERTY.get(direction));
            mutable.set(pos, direction);
            if (redstoneside != WireConnection.NONE
             && (! world.getBlockState(mutable).isOf(this) || ! this.isOtherConnectablePowerstone(world.getBlockState(mutable)))) {
                mutable.move(Direction.DOWN);
                BlockState blockstate = world.getBlockState(mutable);
                if (blockstate.isOf(this) || this.isOtherConnectablePowerstone(blockstate)) {
                    BlockPos blockpos = mutable.offset(direction.getOpposite());
                    BlockState newBlockstate = blockstate.getStateForNeighborUpdate(direction.getOpposite(), world.getBlockState(blockpos), world, mutable, blockpos);
                    replace(blockstate, newBlockstate, world, mutable, pFlags, pRecursionLeft);
                }

                mutable.set(pos, direction).move(Direction.UP);
                BlockState blockstate1 = world.getBlockState(mutable);
                if (blockstate1.isOf(this) || this.isOtherConnectablePowerstone(blockstate1)) {
                    BlockPos blockpos1 = mutable.offset(direction.getOpposite());
                    BlockState newBlockstate1 = blockstate1.getStateForNeighborUpdate(direction.getOpposite(), world.getBlockState(blockpos1), world, mutable, blockpos1);
                    replace(blockstate1, newBlockstate1, world, mutable, pFlags, pRecursionLeft);
                }
            }
        }
    }

    private boolean isOtherConnectablePowerstone(BlockState state) {
        return state.isOf(PowerStones.MULTIPLE_WIRES) && state.get(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE;
    }

    @Redirect(method = "getRenderConnectionType(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Z)Lnet/minecraft/block/enums/WireConnection;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z"))
    private boolean checkConnectsToState(BlockState state, Direction direction) {
        if (state.isOf(Blocks.REDSTONE_WIRE)) {
            return true;
        }
        else if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
            return state.get(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE;
        }
        else if (state.isOf(PowerStones.BLUESTONE_WIRE) || state.isOf(PowerStones.GREENSTONE_WIRE) || state.isOf(PowerStones.YELLOWSTONE_WIRE)
         || state.isOf(PowerStones.BLUESTONE_BLOCK) || state.isOf(PowerStones.GREENSTONE_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_BLOCK)
         || state.isOf(PowerStones.BLUESTONE_TORCH_BLOCK) || state.isOf(PowerStones.BLUESTONE_WALL_TORCH)
         || state.isOf(PowerStones.GREENSTONE_TORCH_BLOCK) || state.isOf(PowerStones.GREENSTONE_WALL_TORCH)
         || state.isOf(PowerStones.YELLOWSTONE_TORCH_BLOCK) || state.isOf(PowerStones.YELLOWSTONE_WALL_TORCH)) {
            return false;
        }
        else {
            return connectsTo(state, direction);
        }
    }

    @Redirect(method = "getReceivedRedstonePower(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)I", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getReceivedRedstonePower(Lnet/minecraft/util/math/BlockPos;)I"))
    private int calculateTargetStrengthMultipleWiresShouldSignal(World world, BlockPos pos) {
        int result = 0;
        ((MultipleWiresBlock)PowerStones.MULTIPLE_WIRES).setShouldSignal(false);
        result = world.getReceivedRedstonePower(pos);
        ((MultipleWiresBlock)PowerStones.MULTIPLE_WIRES).setShouldSignal(true);
        return result;
    }

    @ModifyConstant(method = "increasePower(Lnet/minecraft/block/BlockState;)I", constant = @Constant(intValue = 0))
    private int getWireSignalMultipleWires(int oldResult, BlockState state) {
        if (state.isOf(PowerStones.MULTIPLE_WIRES) && state.get(PowerStones.POWER_PAIR) == PowerPair.RED_BLUE) {
            return state.get(POWER);
        }
        return oldResult;
    }

    @Inject(method = "onUse(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/util/hit/BlockHitResult;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/RedstoneWireBlock;isFullyConnected(Lnet/minecraft/block/BlockState;)Z", ordinal = 0), cancellable = true)
    public void useAddColor(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> callbackInfo) {
        if (player.getMainHandStack().isOf(PowerStones.BLUESTONE)) {
            this.placeOnUse(state, world, pos, player);
            callbackInfo.setReturnValue(ActionResult.SUCCESS);
        }
    }

    public void setShouldSignal(boolean wiresGivePower) {
        this.wiresGivePower = wiresGivePower;
    }

    public int getWeakBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getWeakGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getWeakYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongBluestonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongGreenstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    public int getStrongYellowstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public void updateAll(BlockState state, World world, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            world.updateNeighborsAlways(pos.offset(direction), (RedstoneWireBlock)(Object)this);
        }
        state = this.getPlacementState(world, state, pos);
        this.update(world, pos, state);
        this.updateOffsetNeighbors(world, pos);
        state = this.getPlacementState(world, world.getBlockState(pos), pos);
        world.setBlockState(pos, state,  Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
    }

    private void placeOnUse(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        BlockSoundGroup soundType = state.getSoundGroup();
        world.playSound(player, pos, state.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1.0f) / 2.0f, soundType.getPitch() * 0.8f);
        if (player == null || !player.getAbilities().creativeMode) {
            player.getMainHandStack().decrement(1);
        }
        state = PowerStones.MULTIPLE_WIRES.getDefaultState().with(WIRE_CONNECTION_NORTH, state.get(WIRE_CONNECTION_NORTH)).with(WIRE_CONNECTION_EAST, state.get(WIRE_CONNECTION_EAST)).with(WIRE_CONNECTION_SOUTH, state.get(WIRE_CONNECTION_SOUTH)).with(WIRE_CONNECTION_WEST, state.get(WIRE_CONNECTION_WEST)).with(POWER, state.get(POWER)).with(PowerStones.POWER_B, 0).with(PowerStones.POWER_PAIR, PowerPair.RED_BLUE);
        world.setBlockState(pos, state, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
        ((MultipleWiresBlock)state.getBlock()).updateAll(state, world, pos);
    }

}
