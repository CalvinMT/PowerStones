package com.calvinmt.powerstones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class PowerStonesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
            PowerStones.BLUESTONE_TORCH_BLOCK, PowerStones.GREENSTONE_TORCH_BLOCK, PowerStones.YELLOWSTONE_TORCH_BLOCK,
			PowerStones.BLUESTONE_WALL_TORCH, PowerStones.GREENSTONE_WALL_TORCH, PowerStones.YELLOWSTONE_WALL_TORCH);
    
            LootTableModifier.modifyLootTables();
    }
    
}
