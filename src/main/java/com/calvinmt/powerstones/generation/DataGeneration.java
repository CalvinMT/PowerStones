package com.calvinmt.powerstones.generation;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class DataGeneration implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator generator) {
        // TODO - https://fabricmc.net/wiki/tutorial:datagen_setup
        // TODO - https://discord.com/channels/507304429255393322/566276937035546624/820288185103286276
        generator.createPack().addProvider(AdvancementsProvider::new);
    }
    
}
