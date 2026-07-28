package com.calvinmt.powerstones.block;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jetbrains.annotations.Nullable;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.PowerStones;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;

public class MultipleWiresBlockEntity extends BlockEntity {

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

    private WireConnection northA = WireConnection.NONE;
    private WireConnection eastA = WireConnection.NONE;
    private WireConnection southA = WireConnection.NONE;
    private WireConnection westA = WireConnection.NONE;

    private WireConnection northB = WireConnection.NONE;
    private WireConnection eastB = WireConnection.NONE;
    private WireConnection southB = WireConnection.NONE;
    private WireConnection westB = WireConnection.NONE;

    public record RenderData(
            boolean renderChannelsSwapped,

            PowerPair powerPair,
            int powerA,
            int powerB,

            WireConnection northA,
            WireConnection eastA,
            WireConnection southA,
            WireConnection westA,

            WireConnection northB,
            WireConnection eastB,
            WireConnection southB,
            WireConnection westB
    ) {}

    public MultipleWiresBlockEntity(BlockPos pos, BlockState state) {
        super(PowerStones.MULTIPLE_WIRES_BE_TYPE, pos, state);

        // On the server, this data is registered immediately before
        // setBlockState. Seed the new block entity before any block-added
        // or neighbour callbacks run.
        RenderData conversionData = getBeginningConversionData(pos);

        this.suppressUpdatePackets = conversionData != null;

        if (conversionData != null && conversionData.powerPair() == state.get(MultipleWiresBlock.POWER_PAIR)) {
            this.applyRenderData(conversionData);
            return;
        }

        // On the client, the block-state packet can create the block entity
        // before its authoritative block-entity packet arrives.
        RenderData predictedData = getPredictedRenderData(pos);

        if (predictedData != null && predictedData.powerPair() == state.get(MultipleWiresBlock.POWER_PAIR)) {
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

            channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_NORTH),
            channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_EAST),
            channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_SOUTH),
            channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_WEST),

            channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_NORTH),
            channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_EAST),
            channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_SOUTH),
            channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_WEST)
        );
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        writeNbt(nbt);
        return nbt;
    }

    @Override
    public @Nullable Object getRenderData() {
        return this.createRenderData();
    }

    private RenderData createRenderData() {
        return new RenderData(
            this.renderChannelsSwapped,

            this.getCachedState().get(MultipleWiresBlock.POWER_PAIR),
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
        WireConnection newNorthA = channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_NORTH);
        WireConnection newEastA = channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_EAST);
        WireConnection newSouthA = channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_SOUTH);
        WireConnection newWestA = channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_WEST);

        WireConnection newNorthB = channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_NORTH);
        WireConnection newEastB = channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_EAST);
        WireConnection newSouthB = channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_SOUTH);
        WireConnection newWestB = channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_WEST);

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
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

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
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

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
        if (!hasCompleteRenderData && this.world != null && this.world.isClient) {
            RenderData predictedData = getPredictedRenderData(this.pos);

            if (predictedData != null && predictedData.powerPair() == this.getCachedState().get(MultipleWiresBlock.POWER_PAIR)) {
                this.applyRenderData(predictedData);

                this.world.scheduleBlockRerenderIfNeeded(this.pos, null, this.getCachedState());

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

        if (this.world != null && this.world.isClient) {
            clearPredictedRenderData(this.pos);
            this.world.scheduleBlockRerenderIfNeeded(this.pos, null, this.getCachedState());
        }
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /**
     * Sets all initial render and power data without sending several incomplete
     * updates to the client.
     */
    public void setInitialData(boolean renderChannelsSwapped, int powerA, int powerB, BlockState channelAState, BlockState channelBState) {
        this.renderChannelsSwapped = renderChannelsSwapped;

        this.powerA = powerA;
        this.powerB = powerB;

        this.northA = channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_NORTH);
        this.eastA = channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_EAST);
        this.southA = channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_SOUTH);
        this.westA = channelAState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_WEST);

        this.northB = channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_NORTH);
        this.eastB = channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_EAST);
        this.southB = channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_SOUTH);
        this.westB = channelBState.get(PowerstoneWireBlockBase.WIRE_CONNECTION_WEST);

        this.markDirty();
    }

    /**
     * Ends the atomic initialisation period for a converted wire.
     * The caller sends one completed update after this method returns.
     */
    public void finishConversionInitialisation() {
        this.suppressUpdatePackets = false;
        this.markDirty();
    }

    private void update() {
        if (world != null && !world.isClient) {
            this.markDirty();

            if (!this.suppressUpdatePackets) {
                world.updateListeners(pos, getCachedState(), getCachedState(), Block.NOTIFY_LISTENERS);
            }
        }
    }

    private static byte connectionToByte(WireConnection connection) {
        if (connection == WireConnection.SIDE) return 1;
        else if (connection == WireConnection.UP) return 2;
        else return 0;
    }

    private static WireConnection byteToConnection(byte value) {
        if (value == 1) return WireConnection.SIDE;
        else if (value == 2) return WireConnection.UP;
        else return WireConnection.NONE;
    }

    public void setPowerA(int power){
        if (power==powerA) return;
        powerA = power;
        update();
    }

    public void setPowerB(int power){
        if (power==powerB) return;
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
