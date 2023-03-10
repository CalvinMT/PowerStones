package com.calvinmt.powerstones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.calvinmt.powerstones.block.BluestoneBlock;
import com.calvinmt.powerstones.block.BluestoneTorchBlock;
import com.calvinmt.powerstones.block.GreenstoneBlock;
import com.calvinmt.powerstones.block.GreenstoneTorchBlock;
import com.calvinmt.powerstones.block.WallBluestoneTorchBlock;
import com.calvinmt.powerstones.block.WallGreenstoneTorchBlock;
import com.calvinmt.powerstones.block.WallYellowstoneTorchBlock;
import com.calvinmt.powerstones.block.YellowstoneBlock;
import com.calvinmt.powerstones.block.YellowstoneTorchBlock;

public class PowerStones implements ModInitializer   {

	public static final Logger LOGGER = LoggerFactory.getLogger("PowerStones");

	public static final String NAMESPACE = "powerstones";

	public static final Block BLUESTONE_TORCH_BLOCK = new BluestoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_TORCH));
	public static final Block GREENSTONE_TORCH_BLOCK = new GreenstoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_TORCH));
	public static final Block YELLOWSTONE_TORCH_BLOCK = new YellowstoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_TORCH));
	public static final Block BLUESTONE_WALL_TORCH = new WallBluestoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WALL_TORCH));
	public static final Block GREENSTONE_WALL_TORCH = new WallGreenstoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WALL_TORCH));
	public static final Block YELLOWSTONE_WALL_TORCH = new WallYellowstoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WALL_TORCH));
	public static final Block BLUESTONE_BLOCK = new BluestoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.LAPIS_BLUE).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));
	public static final Block GREENSTONE_BLOCK = new GreenstoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.EMERALD_GREEN).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));
	public static final Block YELLOWSTONE_BLOCK = new YellowstoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.PALE_YELLOW).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));

	public static final BlockItem BLUESTONE = new AliasedBlockItem(Blocks.REDSTONE_WIRE, new FabricItemSettings());
	public static final BlockItem GREENSTONE = new AliasedBlockItem(Blocks.REDSTONE_WIRE, new FabricItemSettings());
	public static final BlockItem YELLOWSTONE = new AliasedBlockItem(Blocks.REDSTONE_WIRE, new FabricItemSettings());
	public static final BlockItem BLUESTONE_TORCH = new VerticallyAttachableBlockItem(BLUESTONE_TORCH_BLOCK, BLUESTONE_WALL_TORCH, new FabricItemSettings(), Direction.DOWN);
	public static final BlockItem GREENSTONE_TORCH = new VerticallyAttachableBlockItem(GREENSTONE_TORCH_BLOCK, GREENSTONE_WALL_TORCH, new FabricItemSettings(), Direction.DOWN);
	public static final BlockItem YELLOWSTONE_TORCH = new VerticallyAttachableBlockItem(YELLOWSTONE_TORCH_BLOCK, YELLOWSTONE_WALL_TORCH, new FabricItemSettings(), Direction.DOWN);
	public static final BlockItem BLUESTONE_BLOCK_ITEM = new BlockItem(BLUESTONE_BLOCK, new FabricItemSettings());
	public static final BlockItem GREENSTONE_BLOCK_ITEM = new BlockItem(GREENSTONE_BLOCK, new FabricItemSettings());
	public static final BlockItem YELLOWSTONE_BLOCK_ITEM = new BlockItem(YELLOWSTONE_BLOCK, new FabricItemSettings());

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "bluestone"), BLUESTONE);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "greenstone"), GREENSTONE);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "yellowstone"), YELLOWSTONE);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "bluestone_torch"), BLUESTONE_TORCH);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "greenstone_torch"), GREENSTONE_TORCH);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "yellowstone_torch"), YELLOWSTONE_TORCH);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "bluestone_block"), BLUESTONE_BLOCK_ITEM);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "greenstone_block"), GREENSTONE_BLOCK_ITEM);
		Registry.register(Registries.ITEM, new Identifier(NAMESPACE, "yellowstone_block"), YELLOWSTONE_BLOCK_ITEM);

		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "bluestone_torch"), BLUESTONE_TORCH_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "greenstone_torch"), GREENSTONE_TORCH_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "yellowstone_torch"), YELLOWSTONE_TORCH_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "bluestone_wall_torch"), BLUESTONE_WALL_TORCH);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "greenstone_wall_torch"), GREENSTONE_WALL_TORCH);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "yellowstone_wall_torch"), YELLOWSTONE_WALL_TORCH);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "bluestone_block"), BLUESTONE_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "greenstone_block"), GREENSTONE_BLOCK);
		Registry.register(Registries.BLOCK, new Identifier(NAMESPACE, "yellowstone_block"), YELLOWSTONE_BLOCK);
	}

}
