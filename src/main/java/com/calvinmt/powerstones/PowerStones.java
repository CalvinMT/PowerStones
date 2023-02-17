package com.calvinmt.powerstones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.calvinmt.powerstones.block.BluestoneBlock;
import com.calvinmt.powerstones.block.GreenstoneBlock;
import com.calvinmt.powerstones.block.YellowstoneBlock;

public class PowerStones implements ModInitializer  {

	public static final Logger LOGGER = LoggerFactory.getLogger("PowerStones");

	public static final String NAMESPACE = "powerstones";

	public static final Item BLUESTONE = (Item)new AliasedBlockItem(Blocks.REDSTONE_WIRE, new FabricItemSettings());
	public static final Block BLUESTONE_BLOCK = new BluestoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.LAPIS_BLUE).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));
	public static final Block GREENSTONE_BLOCK = new GreenstoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.EMERALD_GREEN).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));
	public static final Block YELLOWSTONE_BLOCK = new YellowstoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.PALE_YELLOW).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LOGGER.info("PowerStones initialising...");

		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "bluestone"), BLUESTONE);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "bluestone_block"), new BlockItem(BLUESTONE_BLOCK, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "greenstone_block"), new BlockItem(GREENSTONE_BLOCK, new FabricItemSettings()));
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "yellowstone_block"), new BlockItem(YELLOWSTONE_BLOCK, new FabricItemSettings()));

		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "bluestone_block"), BLUESTONE_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "greenstone_block"), GREENSTONE_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "yellowstone_block"), YELLOWSTONE_BLOCK);

		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
			content.addAfter(Items.REDSTONE, BLUESTONE);
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

        LootTableModifier.modifyLootTables();

		LOGGER.info("PowerStones initialised");
	}

}
