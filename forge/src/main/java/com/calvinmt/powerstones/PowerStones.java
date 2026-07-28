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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import org.slf4j.Logger;

@Mod(PowerStones.MODID)
public class PowerStones {

    public static final String MODID = "powerstones";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCKENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

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
    public static final RegistryObject<Block> BLUESTONE_BLOCK = BLOCKS.register("bluestone_block", () -> new BluestoneBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK)));
    public static final RegistryObject<Block> GREENSTONE_BLOCK = BLOCKS.register("greenstone_block", () -> new GreenstoneBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK)));
    public static final RegistryObject<Block> YELLOWSTONE_BLOCK = BLOCKS.register("yellowstone_block", () -> new YellowstoneBlock(BlockBehaviour.Properties.copy(Blocks.REDSTONE_BLOCK)));

    public static final RegistryObject<BlockEntityType<MultipleWiresBlockEntity>> MULTIPLE_WIRES_BE_TYPE = BLOCKENTITIES.register("multiple_wires_be", () -> BlockEntityType.Builder.of(MultipleWiresBlockEntity::new, MULTIPLE_WIRES.get()).build(null));

    public static final RegistryObject<BlockItem> BLUESTONE = ITEMS.register("bluestone", () -> new ItemNameBlockItem(BLUESTONE_WIRE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> GREENSTONE = ITEMS.register("greenstone", () -> new ItemNameBlockItem(GREENSTONE_WIRE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> YELLOWSTONE = ITEMS.register("yellowstone", () -> new ItemNameBlockItem(YELLOWSTONE_WIRE.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> BLUESTONE_TORCH = ITEMS.register("bluestone_torch", () -> new StandingAndWallBlockItem(BLUESTONE_TORCH_BLOCK.get(), BLUESTONE_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<BlockItem> GREENSTONE_TORCH = ITEMS.register("greenstone_torch", () -> new StandingAndWallBlockItem(GREENSTONE_TORCH_BLOCK.get(), GREENSTONE_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<BlockItem> YELLOWSTONE_TORCH = ITEMS.register("yellowstone_torch", () -> new StandingAndWallBlockItem(YELLOWSTONE_TORCH_BLOCK.get(), YELLOWSTONE_WALL_TORCH.get(), new Item.Properties(), Direction.DOWN));
    public static final RegistryObject<BlockItem> BLUESTONE_BLOCK_ITEM = ITEMS.register("bluestone_block", () -> new BlockItem(BLUESTONE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> GREENSTONE_BLOCK_ITEM = ITEMS.register("greenstone_block", () -> new BlockItem(GREENSTONE_BLOCK.get(), new Item.Properties()));
    public static final RegistryObject<BlockItem> YELLOWSTONE_BLOCK_ITEM = ITEMS.register("yellowstone_block", () -> new BlockItem(YELLOWSTONE_BLOCK.get(), new Item.Properties()));

    public PowerStones(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

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
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
        public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
            event.register(
                    "multiple_wires",
                    MultipleWiresModel.Loader.INSTANCE
            );
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeGameEvents {

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
                if (!level.isClientSide) {
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
