package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.PowerStoneType;
import com.calvinmt.powerstones.RedstoneWireBlockInterface;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class YellowstoneWireBlock extends PowerstoneWireBlock {

	private static final Vec3d[] COLORS = Util.make(new Vec3d[16], colors -> {
        for (int i = 0; i <= 15; ++i) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f);
            float b = MathHelper.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float c = MathHelper.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = new Vec3d(a, a, (b + c) * 2.0f);
        }
    });

    public YellowstoneWireBlock(Settings settings) {
        super(settings, COLORS);
        ((RedstoneWireBlockInterface)(RedstoneWireBlock) this).setPowerStoneType(PowerStoneType.YELLOW);
    }

}
