package com.calvinmt.powerstones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.item.AliasedBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.util.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.calvinmt.powerstones.block.BluestoneBlock;
import com.calvinmt.powerstones.block.BluestoneTorchBlock;
import com.calvinmt.powerstones.block.BluestoneWireBlock;
import com.calvinmt.powerstones.block.GreenstoneBlock;
import com.calvinmt.powerstones.block.GreenstoneTorchBlock;
import com.calvinmt.powerstones.block.GreenstoneWireBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.calvinmt.powerstones.block.WallBluestoneTorchBlock;
import com.calvinmt.powerstones.block.WallGreenstoneTorchBlock;
import com.calvinmt.powerstones.block.WallYellowstoneTorchBlock;
import com.calvinmt.powerstones.block.YellowstoneBlock;
import com.calvinmt.powerstones.block.YellowstoneTorchBlock;
import com.calvinmt.powerstones.block.YellowstoneWireBlock;

public class PowerStones implements ModInitializer   {

	public static final String NAMESPACE = "powerstones";
	public static final Logger LOGGER = LoggerFactory.getLogger("PowerStones");

    public static final IntProperty POWER_B = IntProperty.of("power_b", 0, 15);
    public static final EnumProperty<PowerPair> POWER_PAIR = EnumProperty.of("power_pair", PowerPair.class);

	public static final Block BLUESTONE_WIRE = new BluestoneWireBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WIRE));
	public static final Block GREENSTONE_WIRE = new GreenstoneWireBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WIRE));
	public static final Block YELLOWSTONE_WIRE = new YellowstoneWireBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WIRE));
	public static final Block MULTIPLE_WIRES = new MultipleWiresBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WIRE));
	public static final Block BLUESTONE_TORCH_BLOCK = new BluestoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_TORCH));
	public static final Block GREENSTONE_TORCH_BLOCK = new GreenstoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_TORCH));
	public static final Block YELLOWSTONE_TORCH_BLOCK = new YellowstoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_TORCH));
	public static final Block BLUESTONE_WALL_TORCH = new WallBluestoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WALL_TORCH));
	public static final Block GREENSTONE_WALL_TORCH = new WallGreenstoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WALL_TORCH));
	public static final Block YELLOWSTONE_WALL_TORCH = new WallYellowstoneTorchBlock(FabricBlockSettings.copy(Blocks.REDSTONE_WALL_TORCH));
	public static final Block BLUESTONE_BLOCK = new BluestoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.LAPIS_BLUE).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));
	public static final Block GREENSTONE_BLOCK = new GreenstoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.EMERALD_GREEN).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));
	public static final Block YELLOWSTONE_BLOCK = new YellowstoneBlock(FabricBlockSettings.of(Material.METAL, MapColor.PALE_YELLOW).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL));

	public static final BlockItem BLUESTONE = new AliasedBlockItem(BLUESTONE_WIRE, new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static final BlockItem GREENSTONE = new AliasedBlockItem(GREENSTONE_WIRE, new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static final BlockItem YELLOWSTONE = new AliasedBlockItem(YELLOWSTONE_WIRE, new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static final Item BLUESTONE_TORCH = new WallStandingBlockItem(BLUESTONE_TORCH_BLOCK, BLUESTONE_WALL_TORCH, new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static final Item GREENSTONE_TORCH = new WallStandingBlockItem(GREENSTONE_TORCH_BLOCK, GREENSTONE_WALL_TORCH, new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static final Item YELLOWSTONE_TORCH = new WallStandingBlockItem(YELLOWSTONE_TORCH_BLOCK, YELLOWSTONE_WALL_TORCH, new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static final BlockItem BLUESTONE_BLOCK_ITEM = new BlockItem(BLUESTONE_BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static final BlockItem GREENSTONE_BLOCK_ITEM = new BlockItem(GREENSTONE_BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE));
	public static final BlockItem YELLOWSTONE_BLOCK_ITEM = new BlockItem(YELLOWSTONE_BLOCK, new FabricItemSettings().group(ItemGroup.REDSTONE));

	@Override
	public void onInitialize() {
		this.registerItems();
		this.registerBlocks();
		registerPlayerEvents();
	}

	private void registerItems() {
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "bluestone"), BLUESTONE);
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "greenstone"), GREENSTONE);
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "yellowstone"), YELLOWSTONE);
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "bluestone_torch"), BLUESTONE_TORCH);
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "greenstone_torch"), GREENSTONE_TORCH);
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "yellowstone_torch"), YELLOWSTONE_TORCH);
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "bluestone_block"), BLUESTONE_BLOCK_ITEM);
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "greenstone_block"), GREENSTONE_BLOCK_ITEM);
		Registry.register(Registry.ITEM, new Identifier(NAMESPACE, "yellowstone_block"), YELLOWSTONE_BLOCK_ITEM);
	}

	private void registerBlocks() {
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "bluestone_wire"), BLUESTONE_WIRE);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "greenstone_wire"), GREENSTONE_WIRE);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "yellowstone_wire"), YELLOWSTONE_WIRE);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "multiple_wires"), MULTIPLE_WIRES);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "bluestone_torch"), BLUESTONE_TORCH_BLOCK);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "greenstone_torch"), GREENSTONE_TORCH_BLOCK);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "yellowstone_torch"), YELLOWSTONE_TORCH_BLOCK);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "bluestone_wall_torch"), BLUESTONE_WALL_TORCH);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "greenstone_wall_torch"), GREENSTONE_WALL_TORCH);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "yellowstone_wall_torch"), YELLOWSTONE_WALL_TORCH);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "bluestone_block"), BLUESTONE_BLOCK);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "greenstone_block"), GREENSTONE_BLOCK);
		Registry.register(Registry.BLOCK, new Identifier(NAMESPACE, "yellowstone_block"), YELLOWSTONE_BLOCK);
	}

    public static void registerPlayerEvents() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (! RedstoneWireBlockInterface.canBreakFromHeldItem(world.getBlockState(pos), player.getMainHandStack())
             || ! PowerstoneWireBlock.canBreakFromHeldItem(world.getBlockState(pos), player.getMainHandStack())
             || ! MultipleWiresBlock.canBreakFromHeldItem(world.getBlockState(pos), player.getMainHandStack())) {
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });

		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
			if (state.isOf(PowerStones.MULTIPLE_WIRES)) {
				((MultipleWiresBlock) state.getBlock()).breakSingle(world, pos, state, player);
			}
		});
    }

}
