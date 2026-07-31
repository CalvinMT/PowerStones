package com.calvinmt.powerstones;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.calvinmt.powerstones.block.BluestoneBlock;
import com.calvinmt.powerstones.block.BluestoneTorchBlock;
import com.calvinmt.powerstones.block.BluestoneWireBlock;
import com.calvinmt.powerstones.block.GreenstoneBlock;
import com.calvinmt.powerstones.block.GreenstoneTorchBlock;
import com.calvinmt.powerstones.block.GreenstoneWireBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlockEntity;
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

    public static final EnumProperty<PowerPair> POWER_PAIR = EnumProperty.of("power_pair", PowerPair.class);

	public static final Block BLUESTONE_WIRE = new BluestoneWireBlock(createBlockSettings("bluestone_wire", AbstractBlock.Settings.copy(Blocks.REDSTONE_WIRE)));
	public static final Block GREENSTONE_WIRE = new GreenstoneWireBlock(createBlockSettings("greenstone_wire", AbstractBlock.Settings.copy(Blocks.REDSTONE_WIRE)));
	public static final Block YELLOWSTONE_WIRE = new YellowstoneWireBlock(createBlockSettings("yellowstone_wire", AbstractBlock.Settings.copy(Blocks.REDSTONE_WIRE)));
	public static final Block MULTIPLE_WIRES = new MultipleWiresBlock(createBlockSettings("multiple_wires", AbstractBlock.Settings.copy(Blocks.REDSTONE_WIRE)));
	public static final Block BLUESTONE_TORCH_BLOCK = new BluestoneTorchBlock(createBlockSettings("bluestone_torch", AbstractBlock.Settings.copy(Blocks.REDSTONE_TORCH)));
	public static final Block GREENSTONE_TORCH_BLOCK = new GreenstoneTorchBlock(createBlockSettings("greenstone_torch", AbstractBlock.Settings.copy(Blocks.REDSTONE_TORCH)));
	public static final Block YELLOWSTONE_TORCH_BLOCK = new YellowstoneTorchBlock(createBlockSettings("yellowstone_torch", AbstractBlock.Settings.copy(Blocks.REDSTONE_TORCH)));
	public static final Block BLUESTONE_WALL_TORCH = new WallBluestoneTorchBlock(createBlockSettings("bluestone_wall_torch", AbstractBlock.Settings.copy(Blocks.REDSTONE_WALL_TORCH)));
	public static final Block GREENSTONE_WALL_TORCH = new WallGreenstoneTorchBlock(createBlockSettings("greenstone_wall_torch", AbstractBlock.Settings.copy(Blocks.REDSTONE_WALL_TORCH)));
	public static final Block YELLOWSTONE_WALL_TORCH = new WallYellowstoneTorchBlock(createBlockSettings("yellowstone_wall_torch", AbstractBlock.Settings.copy(Blocks.REDSTONE_WALL_TORCH)));
	public static final Block BLUESTONE_BLOCK = new BluestoneBlock(createBlockSettings("bluestone_block", AbstractBlock.Settings.create().mapColor(MapColor.LAPIS_BLUE).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL)));
	public static final Block GREENSTONE_BLOCK = new GreenstoneBlock(createBlockSettings("greenstone_block", AbstractBlock.Settings.create().mapColor(MapColor.EMERALD_GREEN).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL)));
	public static final Block YELLOWSTONE_BLOCK = new YellowstoneBlock(createBlockSettings("yellowstone_block", AbstractBlock.Settings.create().mapColor(MapColor.PALE_YELLOW).requiresTool().strength(5.0f, 6.0f).sounds(BlockSoundGroup.METAL)));

	public static final BlockEntityType<MultipleWiresBlockEntity> MULTIPLE_WIRES_BE_TYPE = FabricBlockEntityTypeBuilder.create(MultipleWiresBlockEntity::new, MULTIPLE_WIRES).build();

	public static final BlockItem BLUESTONE = new BlockItem(BLUESTONE_WIRE, createItemSettings("bluestone"));
	public static final BlockItem GREENSTONE = new BlockItem(GREENSTONE_WIRE, createItemSettings("greenstone"));
	public static final BlockItem YELLOWSTONE = new BlockItem(YELLOWSTONE_WIRE, createItemSettings("yellowstone"));
	public static final BlockItem BLUESTONE_TORCH = new VerticallyAttachableBlockItem(BLUESTONE_TORCH_BLOCK, BLUESTONE_WALL_TORCH, Direction.DOWN, createItemSettings("bluestone_torch").useBlockPrefixedTranslationKey());
	public static final BlockItem GREENSTONE_TORCH = new VerticallyAttachableBlockItem(GREENSTONE_TORCH_BLOCK, GREENSTONE_WALL_TORCH, Direction.DOWN, createItemSettings("greenstone_torch").useBlockPrefixedTranslationKey());
	public static final BlockItem YELLOWSTONE_TORCH = new VerticallyAttachableBlockItem(YELLOWSTONE_TORCH_BLOCK, YELLOWSTONE_WALL_TORCH, Direction.DOWN, createItemSettings("yellowstone_torch").useBlockPrefixedTranslationKey());
	public static final BlockItem BLUESTONE_BLOCK_ITEM = new BlockItem(BLUESTONE_BLOCK, createItemSettings("bluestone_block"));
	public static final BlockItem GREENSTONE_BLOCK_ITEM = new BlockItem(GREENSTONE_BLOCK, createItemSettings("greenstone_block"));
	public static final BlockItem YELLOWSTONE_BLOCK_ITEM = new BlockItem(YELLOWSTONE_BLOCK, createItemSettings("yellowstone_block"));

	private static AbstractBlock.Settings createBlockSettings(String name, AbstractBlock.Settings settings) {
		return settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(NAMESPACE, name)));
	}

	private static Item.Settings createItemSettings(String name) {
		return new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(NAMESPACE, name)));
	}

	@Override
	public void onInitialize() {
		this.registerItems();
		this.registerBlockEntities();
		this.registerBlocks();
		this.registerToGroups();
		registerPlayerEvents();
	}

	private void registerItems() {
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "bluestone"), BLUESTONE);
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "greenstone"), GREENSTONE);
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "yellowstone"), YELLOWSTONE);
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "bluestone_torch"), BLUESTONE_TORCH);
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "greenstone_torch"), GREENSTONE_TORCH);
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "yellowstone_torch"), YELLOWSTONE_TORCH);
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "bluestone_block"), BLUESTONE_BLOCK_ITEM);
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "greenstone_block"), GREENSTONE_BLOCK_ITEM);
		Registry.register(Registries.ITEM, Identifier.of(NAMESPACE, "yellowstone_block"), YELLOWSTONE_BLOCK_ITEM);
	}

	private void registerBlockEntities() {
		Registry.register(Registries.BLOCK_ENTITY_TYPE, Identifier.of(NAMESPACE, "multiple_wires_be"), MULTIPLE_WIRES_BE_TYPE);
	}

	private void registerBlocks() {
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "bluestone_wire"), BLUESTONE_WIRE);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "greenstone_wire"), GREENSTONE_WIRE);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "yellowstone_wire"), YELLOWSTONE_WIRE);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "multiple_wires"), MULTIPLE_WIRES);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "bluestone_torch"), BLUESTONE_TORCH_BLOCK);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "greenstone_torch"), GREENSTONE_TORCH_BLOCK);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "yellowstone_torch"), YELLOWSTONE_TORCH_BLOCK);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "bluestone_wall_torch"), BLUESTONE_WALL_TORCH);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "greenstone_wall_torch"), GREENSTONE_WALL_TORCH);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "yellowstone_wall_torch"), YELLOWSTONE_WALL_TORCH);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "bluestone_block"), BLUESTONE_BLOCK);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "greenstone_block"), GREENSTONE_BLOCK);
		Registry.register(Registries.BLOCK, Identifier.of(NAMESPACE, "yellowstone_block"), YELLOWSTONE_BLOCK);
	}

	private void registerToGroups() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.addAfter(Items.REDSTONE, BLUESTONE, GREENSTONE, YELLOWSTONE));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.addAfter(Items.REDSTONE_TORCH, BLUESTONE_TORCH, GREENSTONE_TORCH, YELLOWSTONE_TORCH));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> entries.addAfter(Items.REDSTONE_BLOCK, BLUESTONE_BLOCK, GREENSTONE_BLOCK, YELLOWSTONE_BLOCK));
	}

    public static void registerPlayerEvents() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
			if (player.isSpectator()) {
				return ActionResult.PASS;
			}

			BlockState state = world.getBlockState(pos);
        	ItemStack heldItemStack = player.getMainHandStack();

			if (MultipleWiresBlock.shouldBreakIntoSingle(state, heldItemStack)) {
				if (!world.isClient()) {
					// Vanilla breaking will be cancelled, so produce the selected wire's normal loot manually.

					// 'multiple_wires' loot table can still inspect the held dust item.

					if (!player.getAbilities().creativeMode) {
						Block.dropStacks(state, world, pos, world.getBlockEntity(pos), player, heldItemStack);
					}

					((MultipleWiresBlock) state.getBlock()).breakSingle(world, pos, state, player);
				}
				// Prevents vanilla from breaking the replacement wire.

				return ActionResult.SUCCESS;
			}

            if (!RedstoneWireBlockInterface.shouldBreakBlock(state, heldItemStack)
             || !PowerstoneWireBlock.shouldBreakBlock(state, heldItemStack)
			 || !MultipleWiresBlock.shouldBreakBlock(state, heldItemStack)) {
                return ActionResult.FAIL;
            }

            return ActionResult.PASS;
        });
	}

}
