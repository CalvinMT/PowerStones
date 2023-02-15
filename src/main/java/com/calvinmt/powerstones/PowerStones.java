package com.calvinmt.powerstones;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.RedstoneBlock;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.calvinmt.powerstones.block.BluestoneWireBlock;
import com.calvinmt.powerstones.block.GreenstoneWireBlock;
import com.calvinmt.powerstones.block.YellowstoneWireBlock;

public class PowerStones implements ModInitializer, ClientModInitializer  {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("PowerStones");

	public static final BluestoneWireBlock BLUESTONE_WIRE = new BluestoneWireBlock(FabricBlockSettings.of(Material.METAL).breakInstantly().noCollision().nonOpaque());
	public static final GreenstoneWireBlock GREENSTONE_WIRE = new GreenstoneWireBlock(FabricBlockSettings.of(Material.METAL).breakInstantly().noCollision().nonOpaque());
	public static final YellowstoneWireBlock YELLOWSTONE_WIRE = new YellowstoneWireBlock(FabricBlockSettings.of(Material.METAL).breakInstantly().noCollision().nonOpaque());

	public static final Block BLUESTONE_BLOCK = new RedstoneBlock(FabricBlockSettings.of(Material.METAL).strength(5.0f).requiresTool());
	public static final Block GREENSTONE_BLOCK = new RedstoneBlock(FabricBlockSettings.of(Material.METAL).strength(5.0f).requiresTool());
	public static final Block YELLOWSTONE_BLOCK = new RedstoneBlock(FabricBlockSettings.of(Material.METAL).strength(5.0f).requiresTool());

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("PowerStones initialising...");

		Registry.register(Registries.ITEM, new Identifier("powerstones", "bluestone"), new BlockItem(BLUESTONE_WIRE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("powerstones", "greenstone"), new BlockItem(GREENSTONE_WIRE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("powerstones", "yellowstone"), new BlockItem(YELLOWSTONE_WIRE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("powerstones", "bluestone_block"), new BlockItem(BLUESTONE_BLOCK, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("powerstones", "greenstone_block"), new BlockItem(GREENSTONE_BLOCK, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier("powerstones", "yellowstone_block"), new BlockItem(YELLOWSTONE_BLOCK, new FabricItemSettings()));

		Registry.register(Registries.BLOCK, new Identifier("powerstones", "bluestone_wire"), BLUESTONE_WIRE);
		Registry.register(Registries.BLOCK, new Identifier("powerstones", "greenstone_wire"), GREENSTONE_WIRE);
		Registry.register(Registries.BLOCK, new Identifier("powerstones", "yellowstone_wire"), YELLOWSTONE_WIRE);
		Registry.register(Registries.BLOCK, new Identifier("powerstones", "bluestone_block"), BLUESTONE_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier("powerstones", "greenstone_block"), GREENSTONE_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier("powerstones", "yellowstone_block"), YELLOWSTONE_BLOCK);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(Items.REDSTONE, BLUESTONE_WIRE);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(BLUESTONE_WIRE, GREENSTONE_WIRE);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(GREENSTONE_WIRE, YELLOWSTONE_WIRE);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(Items.REDSTONE_BLOCK, BLUESTONE_BLOCK);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(BLUESTONE_BLOCK, GREENSTONE_BLOCK);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(GREENSTONE_BLOCK, YELLOWSTONE_BLOCK);
		});

		LOGGER.info("PowerStones initialised");
	}

	@Override
	public void onInitializeClient() {
		BlockRenderLayerMap.INSTANCE.putBlock(BLUESTONE_WIRE, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(GREENSTONE_WIRE, RenderLayer.getTranslucent());
		BlockRenderLayerMap.INSTANCE.putBlock(YELLOWSTONE_WIRE, RenderLayer.getTranslucent());

		ColorProviderRegistry.BLOCK.register(new BlockColorProvider() {
			@Override
			public int getColor(BlockState state, BlockRenderView view, BlockPos pos, int tintIndex) {
				return BLUESTONE_WIRE.getPowerStoneColor(state.get(RedstoneWireBlock.POWER));
			}
		}, BLUESTONE_WIRE);
		ColorProviderRegistry.BLOCK.register(new BlockColorProvider() {
			@Override
			public int getColor(BlockState state, BlockRenderView view, BlockPos pos, int tintIndex) {
				return GREENSTONE_WIRE.getPowerStoneColor(state.get(RedstoneWireBlock.POWER));
			}
		}, GREENSTONE_WIRE);
		ColorProviderRegistry.BLOCK.register(new BlockColorProvider() {
			@Override
			public int getColor(BlockState state, BlockRenderView view, BlockPos pos, int tintIndex) {
				return YELLOWSTONE_WIRE.getPowerStoneColor(state.get(RedstoneWireBlock.POWER));
			}
		}, YELLOWSTONE_WIRE);
    }

}
