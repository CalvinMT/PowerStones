package com.calvinmt.powerstones.client.model;

import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlockEntity;
import com.calvinmt.powerstones.block.PowerstoneWireBlockBase;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public final class MultipleWiresModel implements UnbakedModel {

    public static final MultipleWiresModel INSTANCE = new MultipleWiresModel();

    private static final String MOD_ID = "powerstones";

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
        return new Identifier(MOD_ID, "block/" + channel + "_dust_" + part);
    }

    @Override
    public Collection<Identifier> getModelDependencies() {
        return MODEL_DEPENDENCIES;
    }

    @Override
    public Collection<SpriteIdentifier> getTextureDependencies(Function<Identifier, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences) {
        Set<SpriteIdentifier> textures = new HashSet<>();

        for (Identifier dependency : MODEL_DEPENDENCIES) {
            UnbakedModel model = unbakedModelGetter.apply(dependency);

            if (model != null) {
                textures.addAll(model.getTextureDependencies(unbakedModelGetter, unresolvedTextureReferences));
            }
        }

        return textures;
    }

    @Override
    @Nullable
    public BakedModel bake(ModelLoader loader, Function<SpriteIdentifier, Sprite> textureGetter, ModelBakeSettings rotationContainer, Identifier modelId) {
        WireModels channelA = bakeWireModels(loader, "multiple_a");
        WireModels channelB = bakeWireModels(loader, "multiple_b");

        return new Baked(channelA, channelB);
    }

    private static WireModels bakeWireModels(ModelLoader loader, String channel) {
        return new WireModels(
            // Straight north-south line.
            bakeRequired(loader, channel, "side0", ModelRotation.X0_Y0),
            bakeRequired(loader, channel, "side_alt0", ModelRotation.X0_Y0),

            // Straight east-west line.
            bakeRequired(loader, channel, "side_alt1", ModelRotation.X0_Y270),
            bakeRequired(loader, channel, "side1", ModelRotation.X0_Y270),

            // Vertical wall sections.
            bakeRequired(loader, channel, "up", ModelRotation.X0_Y0),
            bakeRequired(loader, channel, "up", ModelRotation.X0_Y90),
            bakeRequired(loader, channel, "up", ModelRotation.X0_Y180),
            bakeRequired(loader, channel, "up", ModelRotation.X0_Y270),

            // Centre and directional arms.
            bakeRequired(loader, channel, "dot_duo", ModelRotation.X0_Y0),
            bakeRequired(loader, channel, "side0_duo", ModelRotation.X0_Y0),
            bakeRequired(loader, channel, "side_alt0_duo", ModelRotation.X0_Y0),
            bakeRequired(loader, channel, "side_alt1_duo", ModelRotation.X0_Y270),
            bakeRequired(loader, channel, "side1_duo", ModelRotation.X0_Y270)
        );
    }

    private static BakedModel bakeRequired(ModelLoader loader, String channel, String part, ModelRotation rotation) {
        Identifier id = modelId(channel, part);

        BakedModel model = loader.bake(id, rotation);

        if (model == null) {
            throw new IllegalStateException("Could not bake PowerStones model: " + id);
        }

        return model;
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

    private static final class Baked implements BakedModel, FabricBakedModel {

        private final WireModels channelA;
        private final WireModels channelB;

        private Baked(WireModels channelA, WireModels channelB) {
            this.channelA = channelA;
            this.channelB = channelB;
        }

        @Override
        public boolean isVanillaAdapter() {
            return false;
        }

        @Override
        public void emitBlockQuads(BlockRenderView blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
            MultipleWiresBlockEntity.RenderData data = getRenderData(blockView, state, pos);

            emitChannel(data.northA(), data.eastA(), data.southA(), data.westA(), this.channelA, context);
            emitChannel(data.northB(), data.eastB(), data.southB(), data.westB(), this.channelB, context);
        }

        private static MultipleWiresBlockEntity.RenderData getRenderData(BlockRenderView blockView, BlockState state, BlockPos pos) {
            // When a single wire is converted, the block-state packet can arrive before the block-entity packet.
            // Use the locally calculated render data during that short interval.
            MultipleWiresBlockEntity.RenderData predictedData = MultipleWiresBlockEntity.getPredictedRenderData(pos);

            if (predictedData != null) {
                return predictedData;
            }

            if (blockView instanceof RenderAttachedBlockView) {
                RenderAttachedBlockView attachedView = (RenderAttachedBlockView) blockView;

                Object attachment = attachedView.getBlockEntityRenderAttachment(pos);

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

        private static void emitChannel(WireConnection northConnection, WireConnection eastConnection, WireConnection southConnection, WireConnection westConnection, WireModels models, RenderContext context) {
            boolean north = northConnection.isConnected();
            boolean east = eastConnection.isConnected();
            boolean south = southConnection.isConnected();
            boolean west = westConnection.isConnected();

            // Exact north-south straight line.
            if (north && !east && south && !west) {
                emit(context, models.straightNorthSouthFirst());
                emit(context, models.straightNorthSouthSecond());
            }

            // Exact east-west straight line.
            if (!north && east && !south && west) {
                emit(context, models.straightEastWestFirst());
                emit(context, models.straightEastWestSecond());
            }

            // Sections running up adjacent blocks.
            if (northConnection == WireConnection.UP) {
                emit(context, models.upNorth());
            }

            if (eastConnection == WireConnection.UP) {
                emit(context, models.upEast());
            }

            if (southConnection == WireConnection.UP) {
                emit(context, models.upSouth());
            }

            if (westConnection == WireConnection.UP) {
                emit(context, models.upWest());
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
                emit(context, models.dot());
            }

            // Render each required arm around the centre.
            if (north && (east || west)) {
                emit(context, models.armNorth());
            }

            if (south && (east || west)) {
                emit(context, models.armSouth());
            }

            if (east && (north || south)) {
                emit(context, models.armEast());
            }

            if (west && (north || south)) {
                emit(context, models.armWest());
            }
        }

        private static void emit(RenderContext context, BakedModel model) {
            context.fallbackConsumer().accept(model);
        }

        /**
         * The custom block output is supplied through emitBlockQuads,
         * so vanilla getQuads must not emit another copy.
         */
        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.channelA.dot().useAmbientOcclusion();
        }

        @Override
        public boolean hasDepth() {
            return this.channelA.dot().hasDepth();
        }

        @Override
        public boolean isSideLit() {
            return this.channelA.dot().isSideLit();
        }

        @Override
        public boolean isBuiltin() {
            return this.channelA.dot().isBuiltin();
        }

        @Override
        public Sprite getParticleSprite() {
            return this.channelA.dot().getParticleSprite();
        }

        @Override
        public ModelTransformation getTransformation() {
            return this.channelA.dot().getTransformation();
        }

        @Override
        public ModelOverrideList getOverrides() {
            return this.channelA.dot().getOverrides();
        }

        /**
         * The inventory variant is not replaced by this model.
         * This fallback exists in case another renderer calls it.
         */
        @Override
        public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {
            emit(context, this.channelA.dot());
            emit(context, this.channelB.dot());
        }
    }
}
