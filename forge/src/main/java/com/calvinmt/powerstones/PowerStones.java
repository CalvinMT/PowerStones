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
import com.calvinmt.powerstones.block.PowerstoneWireBlock;
import com.calvinmt.powerstones.block.YellowstoneBlock;
import com.calvinmt.powerstones.block.YellowstoneTorchBlock;
import com.calvinmt.powerstones.block.YellowstoneWallTorchBlock;
import com.calvinmt.powerstones.block.YellowstoneWireBlock;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PowerStones.MODID)
public class PowerStones {

    public static final String MODID = "powerstones";
    public static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final IntegerProperty POWER_B = IntegerProperty.create("power_b", 0, 15);
    public static final EnumProperty<PowerPair> POWER_PAIR = EnumProperty.create("power_pair", PowerPair.class);

    public static final RegistryObject<Block> BLUESTONE_WIRE = BLOCKS.register("bluestone_wire", () -> new BluestoneWireBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)));
    public static final RegistryObject<Block> GREENSTONE_WIRE = BLOCKS.register("greenstone_wire", () -> new GreenstoneWireBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)));
    public static final RegistryObject<Block> YELLOWSTONE_WIRE = BLOCKS.register("yellowstone_wire", () -> new YellowstoneWireBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)));
    public static final RegistryObject<Block> MULTIPLE_WIRES = BLOCKS.register("multiple_wires", () -> new MultipleWiresBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WIRE)));
    public static final RegistryObject<Block> BLUESTONE_TORCH_BLOCK = BLOCKS.register("bluestone_torch", () -> new BluestoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH)));
    public static final RegistryObject<Block> GREENSTONE_TORCH_BLOCK = BLOCKS.register("greenstone_torch", () -> new GreenstoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH)));
    public static final RegistryObject<Block> YELLOWSTONE_TORCH_BLOCK = BLOCKS.register("yellowstone_torch", () -> new YellowstoneTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_TORCH)));
    public static final RegistryObject<Block> BLUESTONE_WALL_TORCH = BLOCKS.register("bluestone_wall_torch", () -> new BluestoneWallTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WALL_TORCH)));
    public static final RegistryObject<Block> GREENSTONE_WALL_TORCH = BLOCKS.register("greenstone_wall_torch", () -> new GreenstoneWallTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WALL_TORCH)));
    public static final RegistryObject<Block> YELLOWSTONE_WALL_TORCH = BLOCKS.register("yellowstone_wall_torch", () -> new YellowstoneWallTorchBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_WALL_TORCH)));
    public static final RegistryObject<Block> BLUESTONE_BLOCK = BLOCKS.register("bluestone_block", () -> new BluestoneBlock(BlockBehaviour.Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL)));
    public static final RegistryObject<Block> GREENSTONE_BLOCK = BLOCKS.register("greenstone_block", () -> new GreenstoneBlock(BlockBehaviour.Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL)));
    public static final RegistryObject<Block> YELLOWSTONE_BLOCK = BLOCKS.register("yellowstone_block", () -> new YellowstoneBlock(BlockBehaviour.Properties.of(Material.METAL).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.METAL)));
    
    public static final RegistryObject<BlockItem> BLUESTONE = ITEMS.register("bluestone", () -> new ItemNameBlockItem(BLUESTONE_WIRE.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    public static final RegistryObject<BlockItem> GREENSTONE = ITEMS.register("greenstone", () -> new ItemNameBlockItem(GREENSTONE_WIRE.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    public static final RegistryObject<BlockItem> YELLOWSTONE = ITEMS.register("yellowstone", () -> new ItemNameBlockItem(YELLOWSTONE_WIRE.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    public static final RegistryObject<BlockItem> BLUESTONE_TORCH = ITEMS.register("bluestone_torch", () -> new StandingAndWallBlockItem(BLUESTONE_TORCH_BLOCK.get(), BLUESTONE_WALL_TORCH.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    public static final RegistryObject<BlockItem> GREENSTONE_TORCH = ITEMS.register("greenstone_torch", () -> new StandingAndWallBlockItem(GREENSTONE_TORCH_BLOCK.get(), GREENSTONE_WALL_TORCH.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    public static final RegistryObject<BlockItem> YELLOWSTONE_TORCH = ITEMS.register("yellowstone_torch", () -> new StandingAndWallBlockItem(YELLOWSTONE_TORCH_BLOCK.get(), YELLOWSTONE_WALL_TORCH.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    public static final RegistryObject<BlockItem> BLUESTONE_BLOCK_ITEM = ITEMS.register("bluestone_block", () -> new BlockItem(BLUESTONE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    public static final RegistryObject<BlockItem> GREENSTONE_BLOCK_ITEM = ITEMS.register("greenstone_block", () -> new BlockItem(GREENSTONE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));
    public static final RegistryObject<BlockItem> YELLOWSTONE_BLOCK_ITEM = ITEMS.register("yellowstone_block", () -> new BlockItem(YELLOWSTONE_BLOCK.get(), new Item.Properties().tab(CreativeModeTab.TAB_REDSTONE)));

    public PowerStones() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MinecraftForge.EVENT_BUS.register(this);

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Do nothing
    }

    // Automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ItemBlockRenderTypes.setRenderLayer(BLUESTONE_WIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GREENSTONE_WIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(YELLOWSTONE_WIRE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(MULTIPLE_WIRES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BLUESTONE_TORCH_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GREENSTONE_TORCH_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(YELLOWSTONE_TORCH_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(BLUESTONE_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(GREENSTONE_WALL_TORCH.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(YELLOWSTONE_WALL_TORCH.get(), RenderType.cutout());
        }
        
        @SubscribeEvent
        public static void registerBlockColors(ColorHandlerEvent.Block event){
            event.getBlockColors().register((state, blockAndTintGetter, pos, tintIndex) -> {
                return ((BluestoneWireBlock)state.getBlock()).getColorForPower(state.getValue(PowerstoneWireBlock.POWER));
            }, BLUESTONE_WIRE.get());
            event.getBlockColors().register((state, blockAndTintGetter, pos, tintIndex) -> {
                return ((GreenstoneWireBlock)state.getBlock()).getColorForPower(state.getValue(PowerstoneWireBlock.POWER));
            }, GREENSTONE_WIRE.get());
            event.getBlockColors().register((state, blockAndTintGetter, pos, tintIndex) -> {
                return ((YellowstoneWireBlock)state.getBlock()).getColorForPower(state.getValue(PowerstoneWireBlock.POWER));
            }, YELLOWSTONE_WIRE.get());
            event.getBlockColors().register((state, blockAndTintGetter, pos, tintIndex) -> {
                return MultipleWiresBlock.getColorForTintIndex(state, tintIndex);
            }, MULTIPLE_WIRES.get());
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class GameEvents {
        @SubscribeEvent
        public static void breakEvent(PlayerEvent.BreakSpeed event) {
            if (! RedstoneWireBlockInterface.canBreakFromHeldItem(event.getState(), event.getPlayer().getMainHandItem())
             || ! PowerstoneWireBlock.canBreakFromHeldItem(event.getState(), event.getPlayer().getMainHandItem())
             || ! MultipleWiresBlock.canBreakFromHeldItem(event.getState(), event.getPlayer().getMainHandItem())) {
                event.setCanceled(true);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeGameEvents {
        @SubscribeEvent
        public static void breakEvent(BlockEvent.BreakEvent event) {
            if (! RedstoneWireBlockInterface.canBreakFromHeldItem(event.getState(), event.getPlayer().getMainHandItem())
             || ! PowerstoneWireBlock.canBreakFromHeldItem(event.getState(), event.getPlayer().getMainHandItem())
             || ! MultipleWiresBlock.canBreakFromHeldItem(event.getState(), event.getPlayer().getMainHandItem())) {
                event.setCanceled(true);
            }
        }
    }

}
