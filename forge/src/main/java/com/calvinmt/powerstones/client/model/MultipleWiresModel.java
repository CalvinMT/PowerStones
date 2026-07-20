package com.calvinmt.powerstones.client.model;

import com.calvinmt.powerstones.PowerPair;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlockEntity;
import com.calvinmt.powerstones.block.PowerstoneWireBlockBase;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public final class MultipleWiresModel implements IUnbakedGeometry<MultipleWiresModel> {

    public static final MultipleWiresModel INSTANCE = new MultipleWiresModel();

    private static final String MOD_ID = "powerstones";

    private static final List<String> COLOURS = List.of(
            "redstone",
            "bluestone",
            "greenstone",
            "yellowstone"
    );

    private static final List<String> MODEL_PARTS = List.of(
            "side0",
            "side_alt0",
            "side_alt1",
            "side1",
            "up",
            "dot_duo",
            "side0_duo",
            "side_alt0_duo",
            "side_alt1_duo",
            "side1_duo"
    );

    private static final List<ResourceLocation> MODEL_DEPENDENCIES = createModelDependencies();

    private MultipleWiresModel() {}

    private static List<ResourceLocation> createModelDependencies() {
        List<ResourceLocation> dependencies = new ArrayList<>();

        for (String colour : COLOURS) {
            for (String part : MODEL_PARTS) {
                dependencies.add(modelId(colour, part));
            }
        }

        return Collections.unmodifiableList(dependencies);
    }

    private static ResourceLocation modelId(String colour, String part) {
        return new ResourceLocation(MOD_ID, "block/" + colour + "_dust_" + part);
    }

    @Override
    public Collection<Material> getMaterials(IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
        Set<Material> textures = new HashSet<>();

        for (ResourceLocation dependency : MODEL_DEPENDENCIES) {
            UnbakedModel model = modelGetter.apply(dependency);

            if (model != null) {
                textures.addAll(model.getMaterials(modelGetter, missingTextureErrors));
            }
        }

        return textures;
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation) {
        WireModels red = bakeWireModels(bakery, spriteGetter, "redstone");
        WireModels blue = bakeWireModels(bakery, spriteGetter, "bluestone");
        WireModels green = bakeWireModels(bakery, spriteGetter, "greenstone");
        WireModels yellow = bakeWireModels(bakery, spriteGetter, "yellowstone");

        return new Baked(red, blue, green, yellow, overrides);
    }

    private static WireModels bakeWireModels(ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, String colour) {
        return new WireModels(
                // Straight north-south line.
                bakeRequired(bakery, spriteGetter, colour, "side0", BlockModelRotation.X0_Y0),
                bakeRequired(bakery, spriteGetter, colour, "side_alt0", BlockModelRotation.X0_Y0),

                // Straight east-west line.
                bakeRequired(bakery, spriteGetter, colour, "side_alt1", BlockModelRotation.X0_Y270),
                bakeRequired(bakery, spriteGetter, colour, "side1", BlockModelRotation.X0_Y270),

                // Vertical wall sections.
                bakeRequired(bakery, spriteGetter, colour, "up", BlockModelRotation.X0_Y0),
                bakeRequired(bakery, spriteGetter, colour, "up", BlockModelRotation.X0_Y90),
                bakeRequired(bakery, spriteGetter, colour, "up", BlockModelRotation.X0_Y180),
                bakeRequired(bakery, spriteGetter, colour, "up", BlockModelRotation.X0_Y270),

                // Centre and directional arms.
                bakeRequired(bakery, spriteGetter, colour, "dot_duo", BlockModelRotation.X0_Y0),
                bakeRequired(bakery, spriteGetter, colour, "side0_duo", BlockModelRotation.X0_Y0),
                bakeRequired(bakery, spriteGetter, colour, "side_alt0_duo", BlockModelRotation.X0_Y0),
                bakeRequired(bakery, spriteGetter, colour, "side_alt1_duo", BlockModelRotation.X0_Y270),
                bakeRequired(bakery, spriteGetter, colour, "side1_duo", BlockModelRotation.X0_Y270)
        );
    }

    private static BakedModel bakeRequired(ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, String colour, String part, ModelState rotation) {
        ResourceLocation id = modelId(colour, part);
        UnbakedModel unbakedModel = bakery.getModel(id);
        BakedModel bakedModel = unbakedModel.bake(bakery, spriteGetter, rotation, id);

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

            BakedModel armNorth,
            BakedModel armSouth,
            BakedModel armEast,
            BakedModel armWest
    ) {}

    private static final class Baked implements IDynamicBakedModel {

        private final WireModels red;
        private final WireModels blue;
        private final WireModels green;
        private final WireModels yellow;
        private final ItemOverrides overrides;

        private Baked(WireModels red, WireModels blue, WireModels green, WireModels yellow, ItemOverrides overrides) {
            this.red = red;
            this.blue = blue;
            this.green = green;
            this.yellow = yellow;
            this.overrides = overrides;
        }

        @Nonnull
        @Override
        public ModelData getModelData(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData modelData) {
            // When a single wire is converted, the block-state packet can arrive before the block-entity packet.
            // Use the locally calculated render data during that short interval.
            MultipleWiresBlockEntity.RenderData predictedData = MultipleWiresBlockEntity.getPredictedRenderData(pos);

            if (predictedData != null) {
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
                addModelQuads(quads, this.red.dot(), null, face, random, renderType);
                addModelQuads(quads, this.blue.dot(), null, face, random, renderType);
                return quads;
            }

            MultipleWiresBlockEntity.RenderData data = extraData.get(MultipleWiresBlockEntity.RENDER_DATA);

            if (data == null) {
                // Fallback for when the block entity is not available,
                // such as when the block is being rendered in the inventory, or pushed by a piston.
                data = getFallbackRenderData(state);
            }

            if (data.powerPair() == PowerPair.RED_BLUE) {
                addChannelQuads(quads, state, face, random, renderType, data.northA(), data.eastA(), data.southA(), data.westA(), this.red);
                addChannelQuads(quads, state, face, random, renderType, data.northB(), data.eastB(), data.southB(), data.westB(), this.blue);
            }
            else if (data.powerPair() == PowerPair.GREEN_YELLOW) {
                addChannelQuads(quads, state, face, random, renderType, data.northA(), data.eastA(), data.southA(), data.westA(), this.green);
                addChannelQuads(quads, state, face, random, renderType, data.northB(), data.eastB(), data.southB(), data.westB(), this.yellow);
            }

            return quads;
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

        private static void addChannelQuads(List<BakedQuad> quads, BlockState state, @Nullable Direction face, RandomSource random, RenderType renderType, RedstoneSide northConnection, RedstoneSide eastConnection, RedstoneSide southConnection, RedstoneSide westConnection, WireModels models) {
            boolean north = northConnection.isConnected();
            boolean east = eastConnection.isConnected();
            boolean south = southConnection.isConnected();
            boolean west = westConnection.isConnected();

            // Exact north-south straight line.
            if (north && !east && south && !west) {
                addModelQuads(quads, models.straightNorthSouthFirst(), state, face, random, renderType);
                addModelQuads(quads, models.straightNorthSouthSecond(), state, face, random, renderType);
            }

            // Exact east-west straight line.
            if (!north && east && !south && west) {
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

            // Render a centre dot for:
            // - an unconnected wire;
            // - corners;
            // - T-junctions;
            // - crosses.
            boolean renderDot = (!north && !east && !south && !west)
                    || (north && east)
                    || (north && west)
                    || (south && east)
                    || (south && west);

            if (renderDot) {
                addModelQuads(quads, models.dot(), state, face, random, renderType);
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
            return this.red.dot().useAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return this.red.dot().isGui3d();
        }

        @Override
        public boolean usesBlockLight() {
            return this.red.dot().usesBlockLight();
        }

        @Override
        public boolean isCustomRenderer() {
            return this.red.dot().isCustomRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return this.red.dot().getParticleIcon();
        }

        @Override
        public ItemTransforms getTransforms() {
            return this.red.dot().getTransforms();
        }

        @Override
        public ItemOverrides getOverrides() {
            return this.overrides;
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
