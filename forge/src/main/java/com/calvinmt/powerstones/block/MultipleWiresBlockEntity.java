package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.PowerStones;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MultipleWiresBlockEntity extends BlockEntity {

    private int powerA = 0;
    private int powerB = 0;

    public MultipleWiresBlockEntity(BlockPos pos, BlockState state) {
        super(PowerStones.MULTIPLE_WIRES_BE_TYPE.get(), pos, state);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        nbt.putInt("a", powerA);
        nbt.putInt("b", powerB);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        powerA = nbt.getInt("a");
        powerB = nbt.getInt("b");
        if (this.level != null && this.level.isClientSide) {
            level.sendBlockUpdated(worldPosition, null, getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void update() {
        if (level != null && !level.isClientSide) {
            this.setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
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
