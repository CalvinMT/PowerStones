package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.PowerStones;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class MultipleWiresBlockEntity extends BlockEntity {

    private int powerA = 0;
    private int powerB = 0;

    public MultipleWiresBlockEntity(BlockPos pos, BlockState state) {
        super(PowerStones.MULTIPLE_WIRES_BE_TYPE, pos, state);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putInt("a",powerA);
        nbt.putInt("b",powerB);
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        powerA = nbt.getInt("a");
        powerB = nbt.getInt("b");
        if (this.world != null && this.world.isClient) {
            world.scheduleBlockRerenderIfNeeded(pos, null, getCachedState());
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    private void update() {
        if (world != null && !world.isClient) {
            this.markDirty();
            world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
        }
    }

    public void setPowerA(int power){
        if(power==powerA)return;
        powerA = power;
        update();
    }

    public void setPowerB(int power){
        if(power==powerB)return;
        powerB = power;
        update();
    }

    public int getPowerA() {
        return powerA;
    }

    public int getPowerB() {
        return powerB;
    }

}
