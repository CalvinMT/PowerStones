package com.calvinmt.powerstones.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.state.property.Properties;

@Mixin(Properties.class)
public class PropertiesMixin {
    
    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 15, ordinal = 0),
        slice = @Slice(
            from = @At(value = "CONSTANT", args = "stringValue=power")
        )
    )
    private static int powerMax(int oldMax) {
        return 16;
    }

}
