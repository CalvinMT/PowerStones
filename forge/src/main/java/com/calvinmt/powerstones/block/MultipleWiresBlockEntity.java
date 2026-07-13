package com.calvinmt.powerstones.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;

public class MultipleWiresBlockEntity extends BlockEntity {

    public static final ModelProperty<RenderData> RENDER_DATA = new ModelProperty<>();

    private int powerA = 0;
    private int powerB = 0;

    private RedstoneSide northA = RedstoneSide.NONE;
    private RedstoneSide eastA = RedstoneSide.NONE;
    private RedstoneSide southA = RedstoneSide.NONE;
    private RedstoneSide westA = RedstoneSide.NONE;

    private RedstoneSide northB = RedstoneSide.NONE;
    private RedstoneSide eastB = RedstoneSide.NONE;
    private RedstoneSide southB = RedstoneSide.NONE;
    private RedstoneSide westB = RedstoneSide.NONE;

    public record RenderData(
            PowerPair powerPair,
            int powerA,
            int powerB,

            RedstoneSide northA,
            RedstoneSide eastA,
            RedstoneSide southA,
            RedstoneSide westA,

            RedstoneSide northB,
            RedstoneSide eastB,
            RedstoneSide southB,
            RedstoneSide westB
    ) {}

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
    @Nonnull
    public ModelData getModelData() {
        return ModelData.builder()
                .with(RENDER_DATA, createRenderData())
                .build();
    }

    private RenderData createRenderData() {
        return new RenderData(
                this.getBlockState().getValue(MultipleWiresBlock.POWER_PAIR),
                this.powerA,
                this.powerB,

                this.northA,
                this.eastA,
                this.southA,
                this.westA,

                this.northB,
                this.eastB,
                this.southB,
                this.westB
        );
    }

    /**
     * Copies the four directions from each temporary wire state into
     * this block entity.
     */
    public void setConnectionStates(BlockState channelAState, BlockState channelBState) {
        RedstoneSide newNorthA = channelAState.getValue(PowerstoneWireBlockBase.NORTH);
        RedstoneSide newEastA = channelAState.getValue(PowerstoneWireBlockBase.EAST);
        RedstoneSide newSouthA = channelAState.getValue(PowerstoneWireBlockBase.SOUTH);
        RedstoneSide newWestA = channelAState.getValue(PowerstoneWireBlockBase.WEST);

        RedstoneSide newNorthB = channelBState.getValue(PowerstoneWireBlockBase.NORTH);
        RedstoneSide newEastB = channelBState.getValue(PowerstoneWireBlockBase.EAST);
        RedstoneSide newSouthB = channelBState.getValue(PowerstoneWireBlockBase.SOUTH);
        RedstoneSide newWestB = channelBState.getValue(PowerstoneWireBlockBase.WEST);

        boolean unchanged = this.northA == newNorthA && this.eastA == newEastA && this.southA == newSouthA && this.westA == newWestA
                        && this.northB == newNorthB && this.eastB == newEastB && this.southB == newSouthB && this.westB == newWestB;

        if (unchanged) {
            return;
        }

        this.northA = newNorthA;
        this.eastA = newEastA;
        this.southA = newSouthA;
        this.westA = newWestA;

        this.northB = newNorthB;
        this.eastB = newEastB;
        this.southB = newSouthB;
        this.westB = newWestB;

        this.update();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        nbt.putInt("a", powerA);
        nbt.putInt("b", powerB);

        nbt.putByte("north_a", connectionToByte(this.northA));
        nbt.putByte("east_a", connectionToByte(this.eastA));
        nbt.putByte("south_a", connectionToByte(this.southA));
        nbt.putByte("west_a", connectionToByte(this.westA));

        nbt.putByte("north_b", connectionToByte(this.northB));
        nbt.putByte("east_b", connectionToByte(this.eastB));
        nbt.putByte("south_b", connectionToByte(this.southB));
        nbt.putByte("west_b", connectionToByte(this.westB));
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

        powerA = nbt.getInt("a");
        powerB = nbt.getInt("b");

        this.northA = byteToConnection(nbt.getByte("north_a"));
        this.eastA = byteToConnection(nbt.getByte("east_a"));
        this.southA = byteToConnection(nbt.getByte("south_a"));
        this.westA = byteToConnection(nbt.getByte("west_a"));

        this.northB = byteToConnection(nbt.getByte("north_b"));
        this.eastB = byteToConnection(nbt.getByte("east_b"));
        this.southB = byteToConnection(nbt.getByte("south_b"));
        this.westB = byteToConnection(nbt.getByte("west_b"));

        if (this.level != null && this.level.isClientSide) {
            this.requestModelDataUpdate();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void update() {
        this.requestModelDataUpdate();

        if (this.level != null && !this.level.isClientSide) {
            this.setChanged();
            this.level.sendBlockUpdated(
                    this.worldPosition,
                    this.getBlockState(),
                    this.getBlockState(),
                    Block.UPDATE_CLIENTS
            );
        }
    }

    private static byte connectionToByte(RedstoneSide connection) {
        if (connection == RedstoneSide.SIDE) return 1;
        else if (connection == RedstoneSide.UP) return 2;
        else return 0;
    }

    private static RedstoneSide byteToConnection(byte value) {
        if (value == 1) return RedstoneSide.SIDE;
        else if (value == 2) return RedstoneSide.UP;
        else return RedstoneSide.NONE;
    }

    public void setPowerA(int power) {
        if (power == powerA) return;
        powerA = power;
        update();
    }

    public void setPowerB(int power) {
        if (power == powerB) return;
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
