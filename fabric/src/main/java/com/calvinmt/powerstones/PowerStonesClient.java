package com.calvinmt.powerstones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

public class PowerStonesClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(),
            PowerStones.BLUESTONE_TORCH_BLOCK, PowerStones.GREENSTONE_TORCH_BLOCK, PowerStones.YELLOWSTONE_TORCH_BLOCK,
			PowerStones.BLUESTONE_WALL_TORCH, PowerStones.GREENSTONE_WALL_TORCH, PowerStones.YELLOWSTONE_WALL_TORCH);

            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(Items.REDSTONE, PowerStones.BLUESTONE);
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(PowerStones.BLUESTONE, PowerStones.GREENSTONE);
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(PowerStones.GREENSTONE, PowerStones.YELLOWSTONE);
            });
    
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(Items.REDSTONE_TORCH, PowerStones.BLUESTONE_TORCH);
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(PowerStones.BLUESTONE_TORCH, PowerStones.GREENSTONE_TORCH);
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(PowerStones.GREENSTONE_TORCH, PowerStones.YELLOWSTONE_TORCH);
            });
    
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(Items.REDSTONE_BLOCK, PowerStones.BLUESTONE_BLOCK);
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(PowerStones.BLUESTONE_BLOCK, PowerStones.GREENSTONE_BLOCK);
            });
            ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
                content.addAfter(PowerStones.GREENSTONE_BLOCK, PowerStones.YELLOWSTONE_BLOCK);
            });
    
            LootTableModifier.modifyLootTables();
    }
    
}
