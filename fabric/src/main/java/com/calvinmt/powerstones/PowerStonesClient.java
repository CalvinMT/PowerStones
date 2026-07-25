package com.calvinmt.powerstones;

import com.calvinmt.powerstones.block.BluestoneWireBlock;
import com.calvinmt.powerstones.block.GreenstoneWireBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlockEntity;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.calvinmt.powerstones.block.YellowstoneWireBlock;
import com.calvinmt.powerstones.client.model.MultipleWiresModel;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class PowerStonesClient implements ClientModInitializer {

    private static final int MULTIPLE_WIRE_TINT_A = 0;
    private static final int MULTIPLE_WIRE_TINT_B = 1;

    @Override
    public void onInitializeClient() {
        this.registerModels();
        this.setBlockRenderLayer();
        this.registerBlockColors();
        PowerStones.registerPlayerEvents();
    }

    private void registerModels() {
        Identifier multipleWiresModelId = new Identifier(PowerStones.NAMESPACE, "block/multiple_wires");

        ModelLoadingRegistry.INSTANCE.registerResourceProvider(resourceManager -> (modelId, context) -> {
            if (modelId.equals(multipleWiresModelId)) {
                return MultipleWiresModel.INSTANCE;
            }

            return null;
        });
    }

    private void setBlockRenderLayer() {
        BlockRenderLayerMap.INSTANCE.putBlocks(
                RenderLayer.getCutout(),

                PowerStones.BLUESTONE_WIRE,
                PowerStones.GREENSTONE_WIRE,
                PowerStones.YELLOWSTONE_WIRE,
                PowerStones.MULTIPLE_WIRES,

                PowerStones.BLUESTONE_TORCH_BLOCK,
                PowerStones.GREENSTONE_TORCH_BLOCK,
                PowerStones.YELLOWSTONE_TORCH_BLOCK,

                PowerStones.BLUESTONE_WALL_TORCH,
                PowerStones.GREENSTONE_WALL_TORCH,
                PowerStones.YELLOWSTONE_WALL_TORCH
        );
    }

    private void registerBlockColors(){
        ColorProviderRegistry.BLOCK.register((state, blockAndTintGetter, pos, tintIndex) -> {
            return ((BluestoneWireBlock)state.getBlock()).getColorForPower(state.get(PowerstoneWireBlock.POWER));
        }, PowerStones.BLUESTONE_WIRE);
        ColorProviderRegistry.BLOCK.register((state, blockAndTintGetter, pos, tintIndex) -> {
            return ((GreenstoneWireBlock)state.getBlock()).getColorForPower(state.get(PowerstoneWireBlock.POWER));
        }, PowerStones.GREENSTONE_WIRE);
        ColorProviderRegistry.BLOCK.register((state, blockAndTintGetter, pos, tintIndex) -> {
            return ((YellowstoneWireBlock)state.getBlock()).getColorForPower(state.get(PowerstoneWireBlock.POWER));
        }, PowerStones.YELLOWSTONE_WIRE);
        ColorProviderRegistry.BLOCK.register(this::getMultipleWiresColour, PowerStones.MULTIPLE_WIRES);
    }

    private int getMultipleWiresColour(BlockState state, @Nullable BlockRenderView blockRenderView, @Nullable BlockPos pos, int tintIndex) {
        PowerPair powerPair = state.get(MultipleWiresBlock.POWER_PAIR);

        MultipleWiresBlockEntity.RenderData renderData = null;

        /*
        * Use the prediction first so the colour mapping changes at exactly the
        * same time as the predicted custom model.
        */
        if (pos != null) {
            MultipleWiresBlockEntity.RenderData predictedData = MultipleWiresBlockEntity.getPredictedRenderData(pos);

            if (predictedData != null && predictedData.powerPair() == powerPair) {
                renderData = predictedData;
            }
        }

        /*
        * Once the authoritative block entity render attachment is available,
        * use it instead of the default zero-power fallback.
        */
        if (renderData == null && blockRenderView instanceof RenderAttachedBlockView && pos != null) {
            RenderAttachedBlockView attachedView = (RenderAttachedBlockView) blockRenderView;

            Object attachment = attachedView.getBlockEntityRenderAttachment(pos);

            if (attachment instanceof MultipleWiresBlockEntity.RenderData) {
                MultipleWiresBlockEntity.RenderData attachedData = (MultipleWiresBlockEntity.RenderData) attachment;

                if (attachedData.powerPair() == powerPair) {
                    renderData = attachedData;
                }
            }
        }

        int powerA = renderData == null ? 0 : renderData.powerA();
        int powerB = renderData == null ? 0 : renderData.powerB();

        boolean renderChannelsSwapped = renderData != null && renderData.renderChannelsSwapped();

        if (tintIndex == MULTIPLE_WIRE_TINT_A) {
            return renderChannelsSwapped 
                ? powerPair.getColourB().getWireColour(powerB)
                : powerPair.getColourA().getWireColour(powerA);
        }

        if (tintIndex == MULTIPLE_WIRE_TINT_B) {
            return renderChannelsSwapped
                ? powerPair.getColourA().getWireColour(powerA)
                : powerPair.getColourB().getWireColour(powerB);
        }

        return PowerColour.WHITE;
    }
}
