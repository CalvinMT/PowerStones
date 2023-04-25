package com.calvinmt.powerstones;

import com.calvinmt.powerstones.block.PowerstoneWireBlock;

import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.util.Identifier;

public class LootTableModifier {

    private static final Identifier REDSTONE_WIRE_LOOT_TABLE_ID = Blocks.REDSTONE_WIRE.getLootTableId();

    public static void modifyLootTables() {
        LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (id.equals(REDSTONE_WIRE_LOOT_TABLE_ID)) {
                // Redstone drop if POWER < 16
                LootPool.Builder poolBuilderRedstone = LootPool.builder()
                    .bonusRolls(ConstantLootNumberProvider.create(0.0f))
                    .conditionally(SurvivesExplosionLootCondition.builder())
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_PAIR, PowerPair.RED_BLUE)))
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(RedstoneWireBlock.POWER, 16)).invert())
                    .with(ItemEntry.builder(Items.REDSTONE))
                    .rolls(ConstantLootNumberProvider.create(1.0f));

                // Bluestone drop if POWER_B < 16
                LootPool.Builder poolBuilderBluestone = LootPool.builder()
                    .bonusRolls(ConstantLootNumberProvider.create(0.0f))
                    .conditionally(SurvivesExplosionLootCondition.builder())
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_PAIR, PowerPair.RED_BLUE)))
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_B, 16)).invert())
                    .with(ItemEntry.builder(PowerStones.BLUESTONE))
                    .rolls(ConstantLootNumberProvider.create(1.0f));

                // Greenstone drop if POWER < 16
                LootPool.Builder poolBuilderGreenstone = LootPool.builder()
                    .bonusRolls(ConstantLootNumberProvider.create(0.0f))
                    .conditionally(SurvivesExplosionLootCondition.builder())
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_PAIR, PowerPair.GREEN_YELLOW)))
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(RedstoneWireBlock.POWER, 16)).invert())
                    .with(ItemEntry.builder(PowerStones.GREENSTONE))
                    .rolls(ConstantLootNumberProvider.create(1.0f));

                // Yellowstone drop if POWER_B < 16
                LootPool.Builder poolBuilderYellowstone = LootPool.builder()
                    .bonusRolls(ConstantLootNumberProvider.create(0.0f))
                    .conditionally(SurvivesExplosionLootCondition.builder())
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_PAIR, PowerPair.GREEN_YELLOW)))
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_B, 16)).invert())
                    .with(ItemEntry.builder(PowerStones.YELLOWSTONE))
                    .rolls(ConstantLootNumberProvider.create(1.0f));

                LootTable.Builder newLootTable = LootTable.builder()
                    .pool(poolBuilderRedstone)
                    .pool(poolBuilderBluestone)
                    .pool(poolBuilderGreenstone)
                    .pool(poolBuilderYellowstone);
                source.set(newLootTable.build());
            }
        });
    }

}
