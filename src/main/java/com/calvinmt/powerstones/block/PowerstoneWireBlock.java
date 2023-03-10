package com.calvinmt.powerstones.block;

import com.calvinmt.powerstones.PowerPair;

import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class PowerstoneWireBlock {

    public static final IntProperty POWER_B = IntProperty.of("power_b", 0, 16);
    public static final EnumProperty<PowerPair> POWER_PAIR = EnumProperty.of("power_pair", PowerPair.class);

    public static final Vec3d[] BLUE_COLORS = (Util.make(new Vec3d[16], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = MathHelper.clamp(f * f * 0.35f + 0.15f, 0.0f, 1.0f); // 0.15f - 0.5f
            float c = MathHelper.clamp(f * f * 0.4f + 0.2f, 0.0f, 1.0f); // 0.2f - 0.6f
            colors[i] = new Vec3d(b, c, a);
        }
    }));
    public static final Vec3d[] GREEN_COLORS = (Util.make(new Vec3d[16], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = MathHelper.clamp(f * f * 0.35f + 0.15f, 0.0f, 1.0f); // 0.15f - 0.5f
            float c = MathHelper.clamp(f * f * 0.25f, 0.0f, 1.0f); // 0.0f - 0.25f
            colors[i] = new Vec3d(c, a, b);
        }
    }));
    public static final Vec3d[] YELLOW_COLORS = (Util.make(new Vec3d[16], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 0; i <= 15; i++) {
            float f = (float)i / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f); // 0.3f - 1.0f
            float b = f * 0.5f + (f > 0.0f ? 0.3f : 0.2f); // 0.2f - 0.8f
            float c = MathHelper.clamp(f * f * 0.4f + 0.2f, 0.0f, 1.0f); // 0.2f - 0.6f
            colors[i] = new Vec3d(a, b, c);
        }
    }));

    public static int getWireColorBlue(int powerLevel) {
        Vec3d vec3d = BLUE_COLORS[powerLevel];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    public static int getWireColorGreen(int powerLevel) {
        Vec3d vec3d = GREEN_COLORS[powerLevel];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    public static int getWireColorYellow(int powerLevel) {
        Vec3d vec3d = YELLOW_COLORS[powerLevel];
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

    public static int getWireColorWhite() {
        Vec3d vec3d = new Vec3d(1, 1, 1);
        return MathHelper.packRgb((float)vec3d.getX(), (float)vec3d.getY(), (float)vec3d.getZ());
    }

}
