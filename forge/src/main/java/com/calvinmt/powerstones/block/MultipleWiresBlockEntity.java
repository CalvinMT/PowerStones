package com.calvinmt.powerstones.block;

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

import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MultipleWiresBlockEntity extends BlockEntity {

    public static final ModelProperty<RenderData> RENDER_DATA = new ModelProperty<>();

    /*
    * Holds the expected render data between the player's interaction and the
    * arrival of the real block-entity update packet.
    *
    * Chunk rebuilding can run on worker threads, so this must be thread-safe.
    */
    private static final ConcurrentMap<Long, RenderData> PREDICTED_RENDER_DATA = new ConcurrentHashMap<>();

    /*
    * Marks positions whose block entities are being created by a single-wire
    * conversion. Block creation and its callbacks are synchronous, so a
    * thread-local set lets the constructor know that it must suppress update
    * packets until all power and connection data has been initialised.
    */
    private static final ThreadLocal<Map<Long, RenderData>> CONVERSION_INITIAL_DATA  = new ThreadLocal<>();
    private boolean suppressUpdatePackets;

    /*
    * Preserves the visual position of the original single wire when it is
    * converted into a multiple wire.
    *
    * The logical A/B channels are unchanged. This only swaps which channel
    * is rendered using the A/B model family.
    */
    private boolean renderChannelsSwapped = false;

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
            boolean renderChannelsSwapped,

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

        // On the server, this data is registered immediately before
        // setBlockState. Seed the new block entity before any block-added
        // or neighbour callbacks run.
        RenderData conversionData = getBeginningConversionData(pos);

        this.suppressUpdatePackets = conversionData != null;

        if (conversionData != null && conversionData.powerPair() == state.getValue(MultipleWiresBlock.POWER_PAIR)) {
            this.applyRenderData(conversionData);
            return;
        }

        // On the client, the block-state packet can create the block entity
        // before its authoritative block-entity packet arrives.
        RenderData predictedData = getPredictedRenderData(pos);

        if (predictedData != null && predictedData.powerPair() == state.getValue(MultipleWiresBlock.POWER_PAIR)) {
            this.applyRenderData(predictedData);
        }
    }

    /**
     * Supplies the complete conversion data to the block-entity constructor
     * that runs synchronously inside World#setBlockState.
     */
    public static void beginConversionInitialisation(BlockPos pos, RenderData data) {
        Map<Long, RenderData> pendingData = CONVERSION_INITIAL_DATA.get();

        if (pendingData == null) {
            pendingData = new HashMap<>();
            CONVERSION_INITIAL_DATA.set(pendingData);
        }

        pendingData.put(pos.asLong(), data);
    }

    public static void endConversionInitialisation(BlockPos pos) {
        Map<Long, RenderData> pendingData = CONVERSION_INITIAL_DATA.get();

        if (pendingData == null) {
            return;
        }

        pendingData.remove(pos.asLong());

        if (pendingData.isEmpty()) {
            CONVERSION_INITIAL_DATA.remove();
        }
    }

    @Nullable
    private static RenderData getBeginningConversionData(BlockPos pos) {
        Map<Long, RenderData> pendingData = CONVERSION_INITIAL_DATA.get();

        return pendingData == null ? null : pendingData.get(pos.asLong());
    }

    private void applyRenderData(RenderData data) {
        this.renderChannelsSwapped = data.renderChannelsSwapped();

        this.powerA = data.powerA();
        this.powerB = data.powerB();

        this.northA = data.northA();
        this.eastA = data.eastA();
        this.southA = data.southA();
        this.westA = data.westA();

        this.northB = data.northB();
        this.eastB = data.eastB();
        this.southB = data.southB();
        this.westB = data.westB();
    }

    public static void setPredictedRenderData(BlockPos pos, RenderData data) {
        PREDICTED_RENDER_DATA.put(pos.asLong(), data);
    }

    @Nullable
    public static RenderData getPredictedRenderData(BlockPos pos) {
        return PREDICTED_RENDER_DATA.get(pos.asLong());
    }

    public static void clearPredictedRenderData(BlockPos pos) {
        PREDICTED_RENDER_DATA.remove(pos.asLong());
    }

    public static RenderData createRenderData(boolean renderChannelsSwapped, PowerPair powerPair, int powerA, int powerB, BlockState channelAState, BlockState channelBState) {
        return new RenderData(
            renderChannelsSwapped,

            powerPair,
            powerA,
            powerB,

            channelAState.getValue(PowerstoneWireBlockBase.NORTH),
            channelAState.getValue(PowerstoneWireBlockBase.EAST),
            channelAState.getValue(PowerstoneWireBlockBase.SOUTH),
            channelAState.getValue(PowerstoneWireBlockBase.WEST),

            channelBState.getValue(PowerstoneWireBlockBase.NORTH),
            channelBState.getValue(PowerstoneWireBlockBase.EAST),
            channelBState.getValue(PowerstoneWireBlockBase.SOUTH),
            channelBState.getValue(PowerstoneWireBlockBase.WEST)
        );
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = new CompoundTag();
        saveAdditional(nbt);
        return nbt;
    }

    @Override
    @Nonnull
    public IModelData getModelData() {
        return new ModelDataMap.Builder()
                .withInitial(RENDER_DATA, getRenderData())
                .build();
    }

    public RenderData getRenderData() {
        return new RenderData(
            this.renderChannelsSwapped,

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

        nbt.putBoolean("render_channels_swapped", this.renderChannelsSwapped);

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

        boolean hasCompleteRenderData =
            nbt.contains("render_channels_swapped")
            && nbt.contains("a")
            && nbt.contains("b")
            && nbt.contains("north_a")
            && nbt.contains("east_a")
            && nbt.contains("south_a")
            && nbt.contains("west_a")
            && nbt.contains("north_b")
            && nbt.contains("east_b")
            && nbt.contains("south_b")
            && nbt.contains("west_b");

        // Do not overwrite a correct prediction with implicit zero values
        // from an incomplete client-side NBT compound.
        if (!hasCompleteRenderData && this.level != null && this.level.isClientSide) {
            RenderData predictedData = getPredictedRenderData(this.worldPosition);

            if (predictedData != null && predictedData.powerPair() == this.getBlockState().getValue(MultipleWiresBlock.POWER_PAIR)) {
                this.applyRenderData(predictedData);

                this.level.setBlocksDirty(this.worldPosition, null, this.getBlockState());

                return;
            }
        }

        this.renderChannelsSwapped = nbt.getBoolean("render_channels_swapped");

        this.powerA = nbt.getInt("a");
        this.powerB = nbt.getInt("b");

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

            if (hasCompleteRenderData) {
                clearPredictedRenderData(this.worldPosition);
            }

            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Sets all initial render and power data without sending several incomplete
     * updates to the client.
     */
    public void setInitialData(boolean renderChannelsSwapped, int powerA, int powerB, BlockState channelAState, BlockState channelBState) {
        this.renderChannelsSwapped = renderChannelsSwapped;

        this.powerA = powerA;
        this.powerB = powerB;

        this.northA = channelAState.getValue(PowerstoneWireBlockBase.NORTH);
        this.eastA = channelAState.getValue(PowerstoneWireBlockBase.EAST);
        this.southA = channelAState.getValue(PowerstoneWireBlockBase.SOUTH);
        this.westA = channelAState.getValue(PowerstoneWireBlockBase.WEST);

        this.northB = channelBState.getValue(PowerstoneWireBlockBase.NORTH);
        this.eastB = channelBState.getValue(PowerstoneWireBlockBase.EAST);
        this.southB = channelBState.getValue(PowerstoneWireBlockBase.SOUTH);
        this.westB = channelBState.getValue(PowerstoneWireBlockBase.WEST);

        this.setChanged();
    }

    /**
     * Ends the atomic initialisation period for a converted wire.
     * The caller sends one completed update after this method returns.
     */
    public void finishConversionInitialisation() {
        this.suppressUpdatePackets = false;
        this.setChanged();
    }

    private void update() {
        this.requestModelDataUpdate();

        if (this.level != null && !this.level.isClientSide) {
            this.setChanged();

            if (!this.suppressUpdatePackets) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            }
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
