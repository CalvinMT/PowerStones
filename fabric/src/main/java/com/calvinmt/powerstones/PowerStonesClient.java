package com.calvinmt.powerstones;

import com.calvinmt.powerstones.block.BluestoneWireBlock;
import com.calvinmt.powerstones.block.GreenstoneWireBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.calvinmt.powerstones.block.YellowstoneWireBlock;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.render.RenderLayer;

public class PowerStonesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
		this.setBlockRenderLayer();
        this.registerBlockColors();
        PowerStones.registerPlayerEvents();
    }

    private void setBlockRenderLayer() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
            PowerStones.BLUESTONE_WIRE, PowerStones.GREENSTONE_WIRE, PowerStones.YELLOWSTONE_WIRE,
            PowerStones.MULTIPLE_WIRES,
            PowerStones.BLUESTONE_TORCH_BLOCK, PowerStones.GREENSTONE_TORCH_BLOCK, PowerStones.YELLOWSTONE_TORCH_BLOCK,
			PowerStones.BLUESTONE_WALL_TORCH, PowerStones.GREENSTONE_WALL_TORCH, PowerStones.YELLOWSTONE_WALL_TORCH);
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
        ColorProviderRegistry.BLOCK.register((state, blockAndTintGetter, pos, tintIndex) -> {
            return MultipleWiresBlock.getColorForTintIndex(state, tintIndex);
        }, PowerStones.MULTIPLE_WIRES);
    }

}
