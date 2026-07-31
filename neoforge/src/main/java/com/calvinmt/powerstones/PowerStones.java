package com.calvinmt.powerstones;

import com.calvinmt.powerstones.block.BluestoneBlock;
import com.calvinmt.powerstones.block.BluestoneTorchBlock;
import com.calvinmt.powerstones.block.BluestoneWallTorchBlock;
import com.calvinmt.powerstones.block.BluestoneWireBlock;
import com.calvinmt.powerstones.block.GreenstoneBlock;
import com.calvinmt.powerstones.block.GreenstoneTorchBlock;
import com.calvinmt.powerstones.block.GreenstoneWallTorchBlock;
import com.calvinmt.powerstones.block.GreenstoneWireBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlock;
import com.calvinmt.powerstones.block.MultipleWiresBlockEntity;
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.calvinmt.powerstones.block.YellowstoneBlock;
import com.calvinmt.powerstones.block.YellowstoneTorchBlock;
import com.calvinmt.powerstones.block.YellowstoneWallTorchBlock;
import com.calvinmt.powerstones.block.YellowstoneWireBlock;
import com.calvinmt.powerstones.client.model.MultipleWiresModel;
import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import org.slf4j.Logger;

@Mod(PowerStones.MODID)
public class PowerStones {

    public static final String MODID = "powerstones";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final EnumProperty<PowerPair> POWER_PAIR = EnumProperty.create("power_pair", PowerPair.class);

    public static final DeferredBlock<BluestoneWireBlock> BLUESTONE_WIRE = BLOCKS.registerBlock("bluestone_wire", BluestoneWireBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE));
    public static final DeferredBlock<GreenstoneWireBlock> GREENSTONE_WIRE = BLOCKS.registerBlock("greenstone_wire", GreenstoneWireBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE));
    public static final DeferredBlock<YellowstoneWireBlock> YELLOWSTONE_WIRE = BLOCKS.registerBlock("yellowstone_wire", YellowstoneWireBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE));
    public static final DeferredBlock<MultipleWiresBlock> MULTIPLE_WIRES = BLOCKS.registerBlock("multiple_wires", MultipleWiresBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WIRE));
    public static final DeferredBlock<BluestoneTorchBlock> BLUESTONE_TORCH_BLOCK = BLOCKS.registerBlock("bluestone_torch", BluestoneTorchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_TORCH));
    public static final DeferredBlock<GreenstoneTorchBlock> GREENSTONE_TORCH_BLOCK = BLOCKS.registerBlock("greenstone_torch", GreenstoneTorchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_TORCH));
    public static final DeferredBlock<YellowstoneTorchBlock> YELLOWSTONE_TORCH_BLOCK = BLOCKS.registerBlock("yellowstone_torch", YellowstoneTorchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_TORCH));
    public static final DeferredBlock<BluestoneWallTorchBlock> BLUESTONE_WALL_TORCH = BLOCKS.registerBlock("bluestone_wall_torch", BluestoneWallTorchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WALL_TORCH));
    public static final DeferredBlock<GreenstoneWallTorchBlock> GREENSTONE_WALL_TORCH = BLOCKS.registerBlock("greenstone_wall_torch", GreenstoneWallTorchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WALL_TORCH));
    public static final DeferredBlock<YellowstoneWallTorchBlock> YELLOWSTONE_WALL_TORCH = BLOCKS.registerBlock("yellowstone_wall_torch", YellowstoneWallTorchBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_WALL_TORCH));
    public static final DeferredBlock<BluestoneBlock> BLUESTONE_BLOCK = BLOCKS.registerBlock("bluestone_block", BluestoneBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_BLOCK));
    public static final DeferredBlock<GreenstoneBlock> GREENSTONE_BLOCK = BLOCKS.registerBlock("greenstone_block", GreenstoneBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_BLOCK));
    public static final DeferredBlock<YellowstoneBlock> YELLOWSTONE_BLOCK = BLOCKS.registerBlock("yellowstone_block", YellowstoneBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_BLOCK));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MultipleWiresBlockEntity>> MULTIPLE_WIRES_BE_TYPE = BLOCKENTITIES.register("multiple_wires_be", () -> new BlockEntityType<>(MultipleWiresBlockEntity::new, false, MULTIPLE_WIRES.get()));

    public static final DeferredItem<BlockItem> BLUESTONE = ITEMS.registerItem("bluestone", properties -> new BlockItem(BLUESTONE_WIRE.get(), properties.overrideDescription(BLUESTONE_WIRE.get().getDescriptionId())));
    public static final DeferredItem<BlockItem> GREENSTONE = ITEMS.registerItem("greenstone", properties -> new BlockItem(GREENSTONE_WIRE.get(), properties.overrideDescription(GREENSTONE_WIRE.get().getDescriptionId())));
    public static final DeferredItem<BlockItem> YELLOWSTONE = ITEMS.registerItem("yellowstone", properties -> new BlockItem(YELLOWSTONE_WIRE.get(), properties.overrideDescription(YELLOWSTONE_WIRE.get().getDescriptionId())));
    public static final DeferredItem<BlockItem> BLUESTONE_TORCH = ITEMS.registerItem("bluestone_torch", properties -> new StandingAndWallBlockItem(BLUESTONE_TORCH_BLOCK.get(), BLUESTONE_WALL_TORCH.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> GREENSTONE_TORCH = ITEMS.registerItem("greenstone_torch", properties -> new StandingAndWallBlockItem(GREENSTONE_TORCH_BLOCK.get(), GREENSTONE_WALL_TORCH.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> YELLOWSTONE_TORCH = ITEMS.registerItem("yellowstone_torch", properties -> new StandingAndWallBlockItem(YELLOWSTONE_TORCH_BLOCK.get(), YELLOWSTONE_WALL_TORCH.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> BLUESTONE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(BLUESTONE_BLOCK);
    public static final DeferredItem<BlockItem> GREENSTONE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(GREENSTONE_BLOCK);
    public static final DeferredItem<BlockItem> YELLOWSTONE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem(YELLOWSTONE_BLOCK);

    public PowerStones(IEventBus modEventBus) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);

        BLOCKS.register(modEventBus);
        BLOCKENTITIES.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Do nothing
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.REDSTONE_BLOCKS) {
            event.accept(BLUESTONE);
            event.accept(GREENSTONE);
            event.accept(YELLOWSTONE);
            event.accept(BLUESTONE_TORCH);
            event.accept(GREENSTONE_TORCH);
            event.accept(YELLOWSTONE_TORCH);
            event.accept(BLUESTONE_BLOCK_ITEM);
            event.accept(GREENSTONE_BLOCK_ITEM);
            event.accept(YELLOWSTONE_BLOCK_ITEM);
        }
    }

    // Automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Do nothing
        }

        @SubscribeEvent
        public static void registerBlockColors(RegisterColorHandlersEvent.Block event){
            event.register((state, blockAndTintGetter, pos, tintIndex) -> {
                return ((BluestoneWireBlock)state.getBlock()).getColorForPower(state.getValue(PowerstoneWireBlock.POWER));
            }, BLUESTONE_WIRE.get());
            event.register((state, blockAndTintGetter, pos, tintIndex) -> {
                return ((GreenstoneWireBlock)state.getBlock()).getColorForPower(state.getValue(PowerstoneWireBlock.POWER));
            }, GREENSTONE_WIRE.get());
            event.register((state, blockAndTintGetter, pos, tintIndex) -> {
                return ((YellowstoneWireBlock)state.getBlock()).getColorForPower(state.getValue(PowerstoneWireBlock.POWER));
            }, YELLOWSTONE_WIRE.get());
            event.register((state, blockAndTintGetter, pos, tintIndex) -> {
                return MultipleWiresBlock.getColorForTintIndex(state, blockAndTintGetter, pos, tintIndex);
            }, MULTIPLE_WIRES.get());
        }

        @SubscribeEvent
        public static void registerBlockStateModels(RegisterBlockStateModels event) {
            event.registerModel(MultipleWiresModel.ID, MultipleWiresModel.CODEC);
        }
    }

    @EventBusSubscriber(modid = MODID)
    public static class NeoForgeGameEvents {

        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            Player player = event.getEntity();

            if (player.isSpectator()) {
                return;
            }

            Level level = event.getLevel();
            BlockPos pos = event.getPos();

            BlockState state = level.getBlockState(pos);
            ItemStack heldItemStack = player.getMainHandItem();

            if (MultipleWiresBlock.shouldBreakIntoSingle(state, heldItemStack)) {
                if (!level.isClientSide()) {
					// Vanilla breaking will be cancelled, so produce the selected wire's normal loot manually.
					// 'multiple_wires' loot table can still inspect the held dust item.
                    if (!player.getAbilities().instabuild) {
                        Block.dropResources(state, level, pos, level.getBlockEntity(pos), player, heldItemStack);
                    }

                    ((MultipleWiresBlock) state.getBlock()).breakSingle(level, pos, state, player);
                }

				// Prevents vanilla from breaking the replacement wire.
                event.setCanceled(true);
                return;
            }

            if (!RedstoneWireBlockInterface.shouldBreakBlock(state, heldItemStack)
             || !PowerstoneWireBlock.shouldBreakBlock(state, heldItemStack)
             || !MultipleWiresBlock.shouldBreakBlock(state, heldItemStack)) {
                event.setCanceled(true);
                return;
            }
        }
    }

}
