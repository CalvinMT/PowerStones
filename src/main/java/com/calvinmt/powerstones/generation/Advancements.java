package com.calvinmt.powerstones.generation;

import java.util.function.Consumer;

import com.calvinmt.powerstones.PowerStones;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class Advancements implements Consumer<Consumer<Advancement>> {

    // ./gradlew runDatagenClient

    @Override
    public void accept(Consumer<Advancement> consumer) {
        // TODO - custom advancements - https://fabricmc.net/wiki/tutorial:datagen_advancements
        Advancement redstoneAdvancement = Advancement.Builder.create()
        .display(
                Items.REDSTONE, // Display icon
                Text.literal("Cut the red wire"), // Title
                Text.literal("But there's red, blue, green and yellow?!"), // Description
                new Identifier("textures/gui/advancements/backgrounds/adventure.png"), // Background image
                AdvancementFrame.TASK, // Options: TASK, CHALLENGE, GOAL
                true, // Show toast top right
                true, // Announce to chat
                false // Hidden in the advancement tab
        )
        .criterion("has_redstone_block", InventoryChangedCriterion.Conditions.items(Items.REDSTONE_BLOCK))
        .build(consumer, PowerStones.NAMESPACE + "/redstone");

        Advancement bluestoneAdvancement = Advancement.Builder.create().parent(redstoneAdvancement)
        .display(
                PowerStones.BLUESTONE,
                Text.literal("Cut the blue wire"),
                Text.literal("Da Ba Dee"),
                null,
                AdvancementFrame.TASK,
                true,
                true,
                false
        )
        .criterion("got_bluestone", InventoryChangedCriterion.Conditions.items(PowerStones.BLUESTONE))
        .build(consumer, PowerStones.NAMESPACE + "/bluestone");
    }
    
}
