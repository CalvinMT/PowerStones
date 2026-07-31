package com.calvinmt.powerstones.client.model;

import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlockEntity;
import com.calvinmt.powerstones.block.PowerstoneWireBlockBase;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBlockModelPart;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.ResolvableModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Environment(EnvType.CLIENT)
public final class MultipleWiresModel implements BlockStateModel.Unbaked {

    public static final MultipleWiresModel INSTANCE = new MultipleWiresModel();

    private static final String MOD_ID = "powerstones";

    private enum StraightLineOrientation {
        NONE,
        NORTH_SOUTH,
        EAST_WEST
    }

    private static final List<String> CHANNELS = List.of(
        "multiple_a",
        "multiple_b"
    );

    private static final List<String> MODEL_PARTS = List.of(
            "side0",
            "side_alt0",
            "side_alt1",
            "side1",
            "up",
            "dot_duo",
            "dot_duo_line0",
            "dot_duo_line1",
            "side0_duo",
            "side_alt0_duo",
            "side_alt1_duo",
            "side1_duo"
    );

    private static final List<Identifier> MODEL_DEPENDENCIES = createModelDependencies();

    private MultipleWiresModel() {}

    private static List<Identifier> createModelDependencies() {
        List<Identifier> dependencies = new ArrayList<>();

        for (String channel : CHANNELS) {
            for (String part : MODEL_PARTS) {
                dependencies.add(modelId(channel, part));
            }
        }

        return Collections.unmodifiableList(dependencies);
    }

    private static Identifier modelId(String channel, String part) {
        return Identifier.of(MOD_ID, "block/" + channel + "_dust_" + part);
    }

    @Override
    public void resolve(ResolvableModel.Resolver resolver) {
        for (Identifier dependency : MODEL_DEPENDENCIES) {
            resolver.markDependency(dependency);
        }
    }

    @Override
    public BlockStateModel bake(Baker baker) {
        WireModels channelA = bakeWireModels(baker, "multiple_a");
        WireModels channelB = bakeWireModels(baker, "multiple_b");

        return new Baked(channelA, channelB);
    }

    private static WireModels bakeWireModels(Baker baker, String channel) {
        return new WireModels(
            // Straight north-south line.
            bakeRequired(baker, channel, "side0", ModelRotation.IDENTITY),
            bakeRequired(baker, channel, "side_alt0", ModelRotation.IDENTITY),

            // Straight east-west line.
            bakeRequired(baker, channel, "side_alt1", rotationY270()),
            bakeRequired(baker, channel, "side1", rotationY270()),

            // Vertical wall sections.
            bakeRequired(baker, channel, "up", ModelRotation.IDENTITY),
            bakeRequired(baker, channel, "up", rotationY90()),
            bakeRequired(baker, channel, "up", rotationY180()),
            bakeRequired(baker, channel, "up", rotationY270()),

            // Centre dots.
            bakeRequired(baker, channel, "dot_duo", ModelRotation.IDENTITY),
            bakeRequired(baker, channel, "dot_duo_line0", ModelRotation.IDENTITY),
            bakeRequired(baker, channel, "dot_duo_line1", ModelRotation.IDENTITY),

            // Directional arms.
            bakeRequired(baker, channel, "side0_duo", ModelRotation.IDENTITY),
            bakeRequired(baker, channel, "side_alt0_duo", ModelRotation.IDENTITY),
            bakeRequired(baker, channel, "side_alt1_duo", rotationY270()),
            bakeRequired(baker, channel, "side1_duo", rotationY270())
        );
    }

    private static ModelBakeSettings rotationY90() {
        return ModelRotation.fromDirectionTransformation(DirectionTransformation.ROT_90_Y_NEG);
    }

    private static ModelBakeSettings rotationY180() {
        return ModelRotation.fromDirectionTransformation(DirectionTransformation.ROT_180_FACE_XZ);
    }

    private static ModelBakeSettings rotationY270() {
        return ModelRotation.fromDirectionTransformation(DirectionTransformation.ROT_90_Y_POS);
    }

    private static BlockModelPart bakeRequired(Baker baker, String channel, String part, ModelBakeSettings rotation) {
        Identifier id = modelId(channel, part);
        return GeometryBakedModel.create(baker, id, rotation);
    }

    private record WireModels(
            BlockModelPart straightNorthSouthFirst,
            BlockModelPart straightNorthSouthSecond,

            BlockModelPart straightEastWestFirst,
            BlockModelPart straightEastWestSecond,

            BlockModelPart upNorth,
            BlockModelPart upEast,
            BlockModelPart upSouth,
            BlockModelPart upWest,

            BlockModelPart dot,
            BlockModelPart dotLine0,
            BlockModelPart dotLine1,

            BlockModelPart armNorth,
            BlockModelPart armSouth,
            BlockModelPart armEast,
            BlockModelPart armWest
    ) {}

    private static final class Baked implements BlockStateModel {

        private final WireModels channelA;
        private final WireModels channelB;

        private Baked(WireModels channelA, WireModels channelB) {
            this.channelA = channelA;
            this.channelB = channelB;
        }

        @Override
        public void addParts(Random random, List<BlockModelPart> parts) {
            // Dynamic block geometry is supplied through emitQuads().

            // Keep the vanilla fallback empty, matching the 1.21.1 model's getQuads().
        }

        @Override
        public Sprite particleSprite() {
            return this.channelA.dot().particleSprite();
        }

        @Override
        public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
            Consumer<BlockModelPart> emit = model -> ((FabricBlockModelPart) model).emitQuads(emitter, cullTest);

            MultipleWiresBlockEntity.RenderData data = getRenderData(blockView, state, pos);

            WireModels modelsForLogicalA = data.renderChannelsSwapped() ? this.channelB : this.channelA;
            WireModels modelsForLogicalB = data.renderChannelsSwapped() ? this.channelA : this.channelB;

            StraightLineOrientation logicalAOrientation = getStraightLineOrientation(data.northA(), data.eastA(), data.southA(), data.westA());
            StraightLineOrientation logicalBOrientation = getStraightLineOrientation(data.northB(), data.eastB(), data.southB(), data.westB());

            emitChannel(data.northA(), data.eastA(), data.southA(), data.westA(), logicalBOrientation, modelsForLogicalA, emit);
            emitChannel(data.northB(), data.eastB(), data.southB(), data.westB(), logicalAOrientation, modelsForLogicalB, emit);
        }

        private static StraightLineOrientation getStraightLineOrientation(WireConnection northConnection, WireConnection eastConnection, WireConnection southConnection, WireConnection westConnection) {
            boolean north = northConnection.isConnected();
            boolean east = eastConnection.isConnected();
            boolean south = southConnection.isConnected();
            boolean west = westConnection.isConnected();

            if (north && !east && south && !west) {
                return StraightLineOrientation.NORTH_SOUTH;
            }

            if (!north && east && !south && west) {
                return StraightLineOrientation.EAST_WEST;
            }

            return StraightLineOrientation.NONE;
        }

        private static MultipleWiresBlockEntity.RenderData getRenderData(BlockRenderView blockView, BlockState state, BlockPos pos) {
            // When a single wire is converted, the block-state packet can arrive before the block-entity packet.


            // Use the locally calculated render data during that short interval.


            MultipleWiresBlockEntity.RenderData predictedData = MultipleWiresBlockEntity.getPredictedRenderData(pos);

            if (predictedData != null && predictedData.powerPair() == state.get(MultipleWiresBlock.POWER_PAIR)) {
                return predictedData;
            }

            if (blockView instanceof FabricBlockView) {
                FabricBlockView attachedView = (FabricBlockView) blockView;

                Object attachment = attachedView.getBlockEntityRenderData(pos);

                if (attachment instanceof MultipleWiresBlockEntity.RenderData) {
                    return (MultipleWiresBlockEntity.RenderData) attachment;
                }
            }

            // Fallback for when the block entity is not available,


            // such as when the block is being rendered in the inventory, or pushed by a piston.


            return getFallbackRenderData(state);
        }

        private static MultipleWiresBlockEntity.RenderData getFallbackRenderData(BlockState state) {
            WireConnection north = state.get(PowerstoneWireBlockBase.WIRE_CONNECTION_NORTH);
            WireConnection east = state.get(PowerstoneWireBlockBase.WIRE_CONNECTION_EAST);
            WireConnection south = state.get(PowerstoneWireBlockBase.WIRE_CONNECTION_SOUTH);
            WireConnection west = state.get(PowerstoneWireBlockBase.WIRE_CONNECTION_WEST);

            return new MultipleWiresBlockEntity.RenderData(
                false,
                state.get(MultipleWiresBlock.POWER_PAIR),
                0,
                0,

                north,
                east,
                south,
                west,

                north,
                east,
                south,
                west
            );
        }

        private static void emitChannel(WireConnection northConnection, WireConnection eastConnection, WireConnection southConnection, WireConnection westConnection, StraightLineOrientation otherChannelOrientation, WireModels models, java.util.function.Consumer<BlockModelPart> emit) {
            boolean north = northConnection.isConnected();
            boolean east = eastConnection.isConnected();
            boolean south = southConnection.isConnected();
            boolean west = westConnection.isConnected();

            boolean straightNorthSouth = north && !east && south && !west;
            boolean straightEastWest = !north && east && !south && west;
            boolean noConnections = !north && !east && !south && !west;

            // Exact north-south straight line.
            if (straightNorthSouth) {
                emit.accept(models.straightNorthSouthFirst());
                emit.accept(models.straightNorthSouthSecond());
            }

            // Exact east-west straight line.
            if (straightEastWest) {
                emit.accept(models.straightEastWestFirst());
                emit.accept(models.straightEastWestSecond());
            }

            // Sections running up adjacent blocks.
            if (northConnection == WireConnection.UP) {
                emit.accept(models.upNorth());
            }

            if (eastConnection == WireConnection.UP) {
                emit.accept(models.upEast());
            }

            if (southConnection == WireConnection.UP) {
                emit.accept(models.upSouth());
            }

            if (westConnection == WireConnection.UP) {
                emit.accept(models.upWest());
            }

            // An unconnected channel uses a direction-specific dot when the
            // other channel is an exact straight line.
            if (noConnections) {
                if (otherChannelOrientation == StraightLineOrientation.NORTH_SOUTH) {
                    emit.accept(models.dotLine0());
                } else if (otherChannelOrientation == StraightLineOrientation.EAST_WEST) {
                    emit.accept(models.dotLine1());
                } else {
                    emit.accept(models.dot());
                }
            }
            else {
                // Corners, T-junctions and crosses use the ordinary centre dot.
                boolean renderDot = (north && east) || (north && west) || (south && east) || (south && west);

                if (renderDot) {
                    emit.accept(models.dot());
                }
            }

            // Render each required arm around the centre.
            if (north && (east || west)) {
                emit.accept(models.armNorth());
            }

            if (south && (east || west)) {
                emit.accept(models.armSouth());
            }

            if (east && (north || south)) {
                emit.accept(models.armEast());
            }

            if (west && (north || south)) {
                emit.accept(models.armWest());
            }
        }

    }
}
