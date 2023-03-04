package com.calvinmt.powerstones;

import com.calvinmt.powerstones.block.PowerstoneWireBlock;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.util.Identifier;

public class LootTableModifier {

    private static final Identifier REDSTONE_WIRE_LOOT_TABLE_ID = Blocks.REDSTONE_WIRE.getLootTableId();

    public static void modifyLootTables() {
        // Redstone drop if POWER < 16
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && id.equals(REDSTONE_WIRE_LOOT_TABLE_ID)) {
                LootCondition.Builder isPairRedBlueCondition = BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                    .exactMatch(PowerstoneWireBlock.POWER_PAIR, PowerPair.RED_BLUE));
                    LootCondition.Builder isPoweredCondition = BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(RedstoneWireBlock.POWER, 16)).invert();
                tableBuilder.modifyPools(poolBuilder -> poolBuilder
                    .conditionally(isPairRedBlueCondition)
                    .conditionally(isPoweredCondition));
            }
        });

        // Bluestone drop if POWER_B < 16
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && id.equals(REDSTONE_WIRE_LOOT_TABLE_ID)) {
                LootPool.Builder poolBuilder = LootPool.builder()
                    .bonusRolls(ConstantLootNumberProvider.create(0.0f))
                    .conditionally(SurvivesExplosionLootCondition.builder())
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_PAIR, PowerPair.RED_BLUE)))
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_B, 16)).invert())
                    .with(ItemEntry.builder(PowerStones.BLUESTONE))
                    .rolls(ConstantLootNumberProvider.create(1.0f));
                tableBuilder.pool(poolBuilder.build());
            }
        });

        // Greenstone drop if POWER < 16
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && id.equals(REDSTONE_WIRE_LOOT_TABLE_ID)) {
                LootPool.Builder poolBuilder = LootPool.builder()
                    .bonusRolls(ConstantLootNumberProvider.create(0.0f))
                    .conditionally(SurvivesExplosionLootCondition.builder())
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_PAIR, PowerPair.GREEN_YELLOW)))
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(RedstoneWireBlock.POWER, 16)).invert())
                    .with(ItemEntry.builder(PowerStones.GREENSTONE))
                    .rolls(ConstantLootNumberProvider.create(1.0f));
                tableBuilder.pool(poolBuilder.build());
            }
        });

        // Yellowstone drop if POWER_B < 16
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && id.equals(REDSTONE_WIRE_LOOT_TABLE_ID)) {
                LootPool.Builder poolBuilder = LootPool.builder()
                    .bonusRolls(ConstantLootNumberProvider.create(0.0f))
                    .conditionally(SurvivesExplosionLootCondition.builder())
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_PAIR, PowerPair.GREEN_YELLOW)))
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_B, 16)).invert())
                    .with(ItemEntry.builder(PowerStones.YELLOWSTONE))
                    .rolls(ConstantLootNumberProvider.create(1.0f));
                tableBuilder.pool(poolBuilder.build());
            }
        });
    }

}
