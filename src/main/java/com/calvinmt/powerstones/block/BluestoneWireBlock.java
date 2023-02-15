package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.PowerStoneType;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;

import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BluestoneWireBlock extends PowerstoneWireBlock {

	private static final Vec3d[] COLORS = Util.make(new Vec3d[16], colors -> {
        for (int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f);
            float b = MathHelper.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float c = MathHelper.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = new Vec3d(b, c, a);
        }
    });

    public BluestoneWireBlock(Settings settings) {
        super(settings, COLORS);
        ((RedstoneWireBlockInterface) this).setPowerStoneType(PowerStoneType.BLUE);
    }

}
