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
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

public class PowerStonesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        this.registerModels();
        this.setBlockRenderLayer();
        this.registerBlockColors();
        PowerStones.registerPlayerEvents();
    }

    private void registerModels() {
        Identifier multipleWiresModelId = new Identifier(PowerStones.NAMESPACE, "block/multiple_wires");

        ModelLoadingPlugin.register(pluginContext ->
            pluginContext.resolveModel().register(context -> {
                if (multipleWiresModelId.equals(context.id())) {
                    return MultipleWiresModel.INSTANCE;
                }

                return null;
            })
        );
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

        int powerA = 0;
        int powerB = 0;

        if (blockRenderView instanceof RenderAttachedBlockView && pos != null) {
            RenderAttachedBlockView attachedView = (RenderAttachedBlockView) blockRenderView;

            Object attachment = attachedView.getBlockEntityRenderAttachment(pos);

            if (attachment instanceof MultipleWiresBlockEntity.RenderData) {
                MultipleWiresBlockEntity.RenderData data = (MultipleWiresBlockEntity.RenderData) attachment;

                powerPair = data.powerPair();
                powerA = data.powerA();
                powerB = data.powerB();
            }
        }

        if (powerPair == PowerPair.RED_BLUE && tintIndex == 0) {
            return PowerstoneWireBlock.getWireColorRed(powerA);
        }

        if (powerPair == PowerPair.RED_BLUE && tintIndex == 1) {
            return PowerstoneWireBlock.getWireColorBlue(powerB);
        }

        if (powerPair == PowerPair.GREEN_YELLOW && tintIndex == 2) {
            return PowerstoneWireBlock.getWireColorGreen(powerA);
        }

        if (powerPair == PowerPair.GREEN_YELLOW && tintIndex == 3) {
            return PowerstoneWireBlock.getWireColorYellow(powerB);
        }

        return PowerstoneWireBlock.getWireColorWhite();
    }

}
