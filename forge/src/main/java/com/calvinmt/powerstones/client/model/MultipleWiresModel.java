package com.calvinmt.powerstones.client.model;

import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlockEntity;
import com.calvinmt.powerstones.block.PowerstoneWireBlockBase;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class MultipleWiresModel implements IUnbakedGeometry<MultipleWiresModel> {

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

    private static final List<ResourceLocation> MODEL_DEPENDENCIES = createModelDependencies();

    private MultipleWiresModel() {}

    private static List<ResourceLocation> createModelDependencies() {
        List<ResourceLocation> dependencies = new ArrayList<>();

        for (String channel : CHANNELS) {
            for (String part : MODEL_PARTS) {
                dependencies.add(modelId(channel, part));
            }
        }

        return Collections.unmodifiableList(dependencies);
    }

    private static ResourceLocation modelId(String channel, String part) {
        return new ResourceLocation(MOD_ID, "block/" + channel + "_dust_" + part);
    }

    @Override
    public void resolveParents(Function<ResourceLocation, UnbakedModel> modelGetter, IGeometryBakingContext context) {
        for (ResourceLocation dependency : MODEL_DEPENDENCIES) {
            UnbakedModel model = modelGetter.apply(dependency);

            if (model != null) {
                model.resolveParents(modelGetter);
            }
        }
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        WireModels channelA = bakeWireModels(baker, spriteGetter, "multiple_a");
        WireModels channelB = bakeWireModels(baker, spriteGetter, "multiple_b");

        return new Baked(channelA, channelB);
    }

    private static WireModels bakeWireModels(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, String channel) {
        return new WireModels(
            // Straight north-south line.
            bakeRequired(baker, spriteGetter, channel, "side0", BlockModelRotation.X0_Y0),
            bakeRequired(baker, spriteGetter, channel, "side_alt0", BlockModelRotation.X0_Y0),

            // Straight east-west line.
            bakeRequired(baker, spriteGetter, channel, "side_alt1", BlockModelRotation.X0_Y270),
            bakeRequired(baker, spriteGetter, channel, "side1", BlockModelRotation.X0_Y270),

            // Vertical wall sections.
            bakeRequired(baker, spriteGetter, channel, "up", BlockModelRotation.X0_Y0),
            bakeRequired(baker, spriteGetter, channel, "up", BlockModelRotation.X0_Y90),
            bakeRequired(baker, spriteGetter, channel, "up", BlockModelRotation.X0_Y180),
            bakeRequired(baker, spriteGetter, channel, "up", BlockModelRotation.X0_Y270),

            // Centre dots.
            bakeRequired(baker, spriteGetter, channel, "dot_duo", BlockModelRotation.X0_Y0),
            bakeRequired(baker, spriteGetter, channel, "dot_duo_line0", BlockModelRotation.X0_Y0),
            bakeRequired(baker, spriteGetter, channel, "dot_duo_line1", BlockModelRotation.X0_Y0),

            // Directional arms.
            bakeRequired(baker, spriteGetter, channel, "side0_duo", BlockModelRotation.X0_Y0),
            bakeRequired(baker, spriteGetter, channel, "side_alt0_duo", BlockModelRotation.X0_Y0),
            bakeRequired(baker, spriteGetter, channel, "side_alt1_duo", BlockModelRotation.X0_Y270),
            bakeRequired(baker, spriteGetter, channel, "side1_duo", BlockModelRotation.X0_Y270)
        );
    }

    private static BakedModel bakeRequired(ModelBaker baker, Function<Material, TextureAtlasSprite> spriteGetter, String channel, String part, ModelState rotation) {
        ResourceLocation id = modelId(channel, part);
        UnbakedModel unbakedModel = baker.getModel(id);
        BakedModel bakedModel = unbakedModel.bake(baker, spriteGetter, rotation, id);

        if (bakedModel == null) {
            throw new IllegalStateException("Could not bake PowerStones model: " + id);
        }

        return bakedModel;
    }

    private record WireModels(
            BakedModel straightNorthSouthFirst,
            BakedModel straightNorthSouthSecond,

            BakedModel straightEastWestFirst,
            BakedModel straightEastWestSecond,

            BakedModel upNorth,
            BakedModel upEast,
            BakedModel upSouth,
            BakedModel upWest,

            BakedModel dot,
            BakedModel dotLine0,
            BakedModel dotLine1,

            BakedModel armNorth,
            BakedModel armSouth,
            BakedModel armEast,
            BakedModel armWest
    ) {}

    private static final class Baked implements IDynamicBakedModel {

        private final WireModels channelA;
        private final WireModels channelB;

        private Baked(WireModels channelA, WireModels channelB) {
            this.channelA = channelA;
            this.channelB = channelB;
        }

        @Nonnull
        @Override
        public ModelData getModelData(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData modelData) {
            // When a single wire is converted, the block-state packet can arrive before the block-entity packet.
            // Use the locally calculated render data during that short interval.
            MultipleWiresBlockEntity.RenderData predictedData = MultipleWiresBlockEntity.getPredictedRenderData(pos);

            if (predictedData != null && predictedData.powerPair() == state.getValue(MultipleWiresBlock.POWER_PAIR)) {
                return ModelData.builder().with(MultipleWiresBlockEntity.RENDER_DATA, predictedData).build();
            }

            return modelData;
        }

        @Nonnull
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, @Nonnull RandomSource random, @Nonnull ModelData extraData, @Nullable RenderType renderType) {
            List<BakedQuad> quads = new ArrayList<>();

            // Fallback for an item renderer that reaches this block model.
            if (state == null) {
                addModelQuads(quads, this.channelA.dot(), null, face, random, renderType);
                addModelQuads(quads, this.channelB.dot(), null, face, random, renderType);
                return quads;
            }

            MultipleWiresBlockEntity.RenderData data = extraData.get(MultipleWiresBlockEntity.RENDER_DATA);

            if (data == null) {
                // Fallback for when the block entity is not available,
                // such as when the block is being rendered in the inventory, or pushed by a piston.
                data = getFallbackRenderData(state);
            }

            WireModels modelsForLogicalA = data.renderChannelsSwapped() ? this.channelB : this.channelA;
            WireModels modelsForLogicalB = data.renderChannelsSwapped() ? this.channelA : this.channelB;

            StraightLineOrientation logicalAOrientation = getStraightLineOrientation(data.northA(), data.eastA(), data.southA(), data.westA());
            StraightLineOrientation logicalBOrientation = getStraightLineOrientation(data.northB(), data.eastB(), data.southB(), data.westB());

            addChannelQuads(quads, state, face, random, renderType, data.northA(), data.eastA(), data.southA(), data.westA(), logicalBOrientation, modelsForLogicalA);
            addChannelQuads(quads, state, face, random, renderType, data.northB(), data.eastB(), data.southB(), data.westB(), logicalAOrientation, modelsForLogicalB);

            return quads;
        }

        private static StraightLineOrientation getStraightLineOrientation(RedstoneSide northConnection, RedstoneSide eastConnection, RedstoneSide southConnection, RedstoneSide westConnection) {
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

        @Nonnull
        @Override
        public ChunkRenderTypeSet getRenderTypes(@Nonnull BlockState state, @Nonnull RandomSource random, @Nonnull ModelData data) {
            return ChunkRenderTypeSet.of(RenderType.cutout());
        }

        private static MultipleWiresBlockEntity.RenderData getFallbackRenderData(BlockState state) {
            RedstoneSide north = state.getValue(PowerstoneWireBlockBase.NORTH);
            RedstoneSide east = state.getValue(PowerstoneWireBlockBase.EAST);
            RedstoneSide south = state.getValue(PowerstoneWireBlockBase.SOUTH);
            RedstoneSide west = state.getValue(PowerstoneWireBlockBase.WEST);

            return new MultipleWiresBlockEntity.RenderData(
                false,

                state.getValue(MultipleWiresBlock.POWER_PAIR),
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

        private static void addChannelQuads(List<BakedQuad> quads, BlockState state, @Nullable Direction face, RandomSource random, RenderType renderType, RedstoneSide northConnection, RedstoneSide eastConnection, RedstoneSide southConnection, RedstoneSide westConnection, StraightLineOrientation otherChannelOrientation, WireModels models) {
            boolean north = northConnection.isConnected();
            boolean east = eastConnection.isConnected();
            boolean south = southConnection.isConnected();
            boolean west = westConnection.isConnected();

            boolean straightNorthSouth = north && !east && south && !west;
            boolean straightEastWest = !north && east && !south && west;
            boolean noConnections = !north && !east && !south && !west;

            // Exact north-south straight line.
            if (straightNorthSouth) {
                addModelQuads(quads, models.straightNorthSouthFirst(), state, face, random, renderType);
                addModelQuads(quads, models.straightNorthSouthSecond(), state, face, random, renderType);
            }

            // Exact east-west straight line.
            if (straightEastWest) {
                addModelQuads(quads, models.straightEastWestFirst(), state, face, random, renderType);
                addModelQuads(quads, models.straightEastWestSecond(), state, face, random, renderType);
            }

            // Sections running up adjacent blocks.
            if (northConnection == RedstoneSide.UP) {
                addModelQuads(quads, models.upNorth(), state, face, random, renderType);
            }

            if (eastConnection == RedstoneSide.UP) {
                addModelQuads(quads, models.upEast(), state, face, random, renderType);
            }

            if (southConnection == RedstoneSide.UP) {
                addModelQuads(quads, models.upSouth(), state, face, random, renderType);
            }

            if (westConnection == RedstoneSide.UP) {
                addModelQuads(quads, models.upWest(), state, face, random, renderType);
            }

            // An unconnected channel uses a direction-specific dot when the
            // other channel is an exact straight line.
            if (noConnections) {
                if (otherChannelOrientation == StraightLineOrientation.NORTH_SOUTH) {
                    addModelQuads(quads, models.dotLine0(), state, face, random, renderType);
                } else if (otherChannelOrientation == StraightLineOrientation.EAST_WEST) {
                    addModelQuads(quads, models.dotLine1(), state, face, random, renderType);
                } else {
                    addModelQuads(quads, models.dot(), state, face, random, renderType);
                }
            }
            else {
                // Corners, T-junctions and crosses use the ordinary centre dot.
                boolean renderDot = (north && east) || (north && west) || (south && east) || (south && west);

                if (renderDot) {
                    addModelQuads(quads, models.dot(), state, face, random, renderType);
                }
            }

            // Render each required arm around the centre.
            if (north && (east || west)) {
                addModelQuads(quads, models.armNorth(), state, face, random, renderType);
            }

            if (south && (east || west)) {
                addModelQuads(quads, models.armSouth(), state, face, random, renderType);
            }

            if (east && (north || south)) {
                addModelQuads(quads, models.armEast(), state, face, random, renderType);
            }

            if (west && (north || south)) {
                addModelQuads(quads, models.armWest(), state, face, random, renderType);
            }
        }

        private static void addModelQuads(List<BakedQuad> quads, BakedModel model, @Nullable BlockState state, @Nullable Direction face, RandomSource random, RenderType renderType) {
            quads.addAll(model.getQuads(state, face, random, ModelData.EMPTY, renderType));
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.channelA.dot().useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return this.channelA.dot().isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return this.channelA.dot().usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return this.channelA.dot().isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return this.channelA.dot().getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            return this.channelA.dot().getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.channelA.dot().getOverrides();
        }
    }

    public enum Loader implements IGeometryLoader<MultipleWiresModel> {
        INSTANCE;

        @Override
        public MultipleWiresModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
            return MultipleWiresModel.INSTANCE;
        }
    }
}
