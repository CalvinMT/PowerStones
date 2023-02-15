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
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockRenderView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.calvinmt.powerstones.block.PowerstoneWireBlock;

public class PowerStones implements ModInitializer, ClientModInitializer  {

	public static final Logger LOGGER = LoggerFactory.getLogger("PowerStones");

	public static final String NAMESPACE = "powerstones";

	public static final PowerstoneWireBlock BLUESTONE_WIRE = new PowerstoneWireBlock(FabricBlockSettings.of(Material.METAL).breakInstantly().noCollision().nonOpaque()).hasBlue();
	public static final PowerstoneWireBlock GREENSTONE_WIRE = new PowerstoneWireBlock(FabricBlockSettings.of(Material.METAL).breakInstantly().noCollision().nonOpaque()).hasGreen();
	public static final PowerstoneWireBlock YELLOWSTONE_WIRE = new PowerstoneWireBlock(FabricBlockSettings.of(Material.METAL).breakInstantly().noCollision().nonOpaque()).hasYellow();

	public static final Block BLUESTONE_BLOCK = new RedstoneBlock(FabricBlockSettings.of(Material.METAL).strength(5.0f).requiresTool());
	public static final Block GREENSTONE_BLOCK = new RedstoneBlock(FabricBlockSettings.of(Material.METAL).strength(5.0f).requiresTool());
	public static final Block YELLOWSTONE_BLOCK = new RedstoneBlock(FabricBlockSettings.of(Material.METAL).strength(5.0f).requiresTool());

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("PowerStones initialising...");

		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "bluestone"), new BlockItem(BLUESTONE_WIRE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "greenstone"), new BlockItem(GREENSTONE_WIRE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "yellowstone"), new BlockItem(YELLOWSTONE_WIRE, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "bluestone_block"), new BlockItem(BLUESTONE_BLOCK, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "greenstone_block"), new BlockItem(GREENSTONE_BLOCK, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "yellowstone_block"), new BlockItem(YELLOWSTONE_BLOCK, new FabricItemSettings()));

		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "bluestone_wire"), BLUESTONE_WIRE);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "greenstone_wire"), GREENSTONE_WIRE);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "yellowstone_wire"), YELLOWSTONE_WIRE);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "bluestone_block"), BLUESTONE_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "greenstone_block"), GREENSTONE_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "yellowstone_block"), YELLOWSTONE_BLOCK);

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

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(Items.REDSTONE, BLUESTONE_WIRE);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(BLUESTONE_WIRE, GREENSTONE_WIRE);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(GREENSTONE_WIRE, YELLOWSTONE_WIRE);
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
				Vec3d vec3d = PowerstoneWireBlock.BLUE_COLORS[state.get(PowerstoneWireBlock.BLUE_WIRE)];
				return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
			}
		}, BLUESTONE_WIRE);
		ColorProviderRegistry.BLOCK.register(new BlockColorProvider() {
			@Override
			public int getColor(BlockState state, BlockRenderView view, BlockPos pos, int tintIndex) {
				Vec3d vec3d = PowerstoneWireBlock.GREEN_COLORS[state.get(PowerstoneWireBlock.GREEN_WIRE)];
				return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
			}
		}, GREENSTONE_WIRE);
		ColorProviderRegistry.BLOCK.register(new BlockColorProvider() {
			@Override
			public int getColor(BlockState state, BlockRenderView view, BlockPos pos, int tintIndex) {
				Vec3d vec3d = PowerstoneWireBlock.YELLOW_COLORS[state.get(PowerstoneWireBlock.YELLOW_WIRE)];
				return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
			}
		}, YELLOWSTONE_WIRE);
    }

}
