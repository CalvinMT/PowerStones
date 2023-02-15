package com.calvinmt.powerstones.block;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class PowerstoneWireBlock extends RedstoneWireBlock {

    public static final int MAX_MULTIPLE_WIRE_TYPES = 4;
    
    //public static final IntProperty RED_WIRE = IntProperty.of("red_wire", 0, 2);
    public static final IntProperty BLUE_WIRE = IntProperty.of("blue_wire", 0, 2);
    public static final IntProperty GREEN_WIRE = IntProperty.of("green_wire", 0, 2);
    public static final IntProperty YELLOW_WIRE = IntProperty.of("yellow_wire", 0, 2);

    public static final Vec3d[] RED_COLORS = (Util.make(new Vec3d[3], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 1; i <= 2; i++) {
            int j = (i == 1 ? 0 : 15);
            float f = (float)j / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f);
            float b = MathHelper.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float c = MathHelper.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = new Vec3d(a, b, c);
        }
    }));
    public static final Vec3d[] BLUE_COLORS = (Util.make(new Vec3d[3], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 1; i <= 2; i++) {
            int j = (i == 1 ? 0 : 15);
            float f = (float)j / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f);
            float b = MathHelper.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float c = MathHelper.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = new Vec3d(b, c, a);
        }
    }));
    public static final Vec3d[] GREEN_COLORS = (Util.make(new Vec3d[3], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 1; i <= 2; i++) {
            int j = (i == 1 ? 0 : 15);
            float f = (float)j / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f);
            float b = MathHelper.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float c = MathHelper.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = new Vec3d(c, a, b);
        }
    }));
    public static final Vec3d[] YELLOW_COLORS = (Util.make(new Vec3d[3], colors -> {
        colors[0] = new Vec3d(0, 0, 0);
        for (int i = 1; i <= 2; i++) {
            int j = (i == 1 ? 0 : 15);
            float f = (float)j / 15.0f;
            float a = f * 0.6f + (f > 0.0f ? 0.4f : 0.3f);
            float b = MathHelper.clamp(f * f * 0.7f - 0.5f, 0.0f, 1.0f);
            float c = MathHelper.clamp(f * f * 0.6f - 0.7f, 0.0f, 1.0f);
            colors[i] = new Vec3d(a, a, (b + c) * 2.0f);
        }
    }));

    public PowerstoneWireBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(POWER, 0));
        setDefaultState(getDefaultState().with(BLUE_WIRE, 0));
        setDefaultState(getDefaultState().with(GREEN_WIRE, 0));
        setDefaultState(getDefaultState().with(YELLOW_WIRE, 0));
    }

    public PowerstoneWireBlock hasRed() {
        setDefaultState(getDefaultState().with(POWER, 1));
        return this;
    }

    public PowerstoneWireBlock hasBlue() {
        setDefaultState(getDefaultState().with(BLUE_WIRE, 1));
        return this;
    }

    public PowerstoneWireBlock hasGreen() {
        setDefaultState(getDefaultState().with(GREEN_WIRE, 1));
        return this;
    }

    public PowerstoneWireBlock hasYellow() {
        setDefaultState(getDefaultState().with(YELLOW_WIRE, 1));
        return this;
    }

}
