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
        // Redstone drop if POWER >= 1
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && id.equals(REDSTONE_WIRE_LOOT_TABLE_ID)) {
                LootCondition.Builder lootCondition = BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                    .exactMatch(RedstoneWireBlock.POWER, 0)).invert();
                tableBuilder.modifyPools(poolBuilder -> poolBuilder.conditionally(lootCondition));
            }
        });

        // Bluestone drop if POWER_BLUE >= 1
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            if (source.isBuiltin() && id.equals(REDSTONE_WIRE_LOOT_TABLE_ID)) {
                LootPool.Builder poolBuilder = LootPool.builder()
                    .bonusRolls(ConstantLootNumberProvider.create(0.0f))
                    .conditionally(SurvivesExplosionLootCondition.builder())
                    .conditionally(BlockStatePropertyLootCondition.builder(Blocks.REDSTONE_WIRE).properties(StatePredicate.Builder.create()
                        .exactMatch(PowerstoneWireBlock.POWER_BLUE, 0)).invert())
                    .with(ItemEntry.builder(PowerStones.BLUESTONE))
                    .rolls(ConstantLootNumberProvider.create(1.0f));
                tableBuilder.pool(poolBuilder.build());
            }
        });
    }

}
