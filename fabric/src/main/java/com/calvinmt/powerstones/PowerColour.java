package com.calvinmt.powerstones;

import java.util.function.IntFunction;

import org.joml.Vector3f;

import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public enum PowerColour {
    RED(0, PowerColour::createRedColour),
    BLUE(1, PowerColour::createBlueColour),
    GREEN(2, PowerColour::createGreenColour),
    YELLOW(3, PowerColour::createYellowColour);

    private static final int MIN_POWER = 0;
    private static final int MAX_POWER = 15;

    private final int tintIndex;
    private final Vec3d[] colours;

    public static final int WHITE = ColorHelper.fromFloats(1.0F, 1.0F, 1.0F, 1.0F);

    PowerColour(int tintIndex, IntFunction<Vec3d> colourFactory) {
        this.tintIndex = tintIndex;
        this.colours = createColourPalette(colourFactory);
    }

    public int getTintIndex() {
        return this.tintIndex;
    }

    public Vector3f getVectorColour(int powerLevel) {
        int clampedPower = MathHelper.clamp(powerLevel, MIN_POWER, MAX_POWER);

        Vec3d colour = this.colours[clampedPower];

        return new Vector3f((float) colour.getX(), (float) colour.getY(), (float) colour.getZ());
    }

    public int getWireColour(int powerLevel) {
        int clampedPower = MathHelper.clamp(powerLevel, MIN_POWER, MAX_POWER);

        Vec3d colour = this.colours[clampedPower];

        return ColorHelper.fromFloats(1.0F, (float) colour.getX(), (float) colour.getY(), (float) colour.getZ());
    }

    public Vec3d getColour(int powerLevel) {
        int clampedPower = MathHelper.clamp(powerLevel, 0, 15);
        return this.colours[clampedPower];
    }

    public Vec3d[] getColours() {
        return this.colours;
    }

    private static Vec3d[] createColourPalette(IntFunction<Vec3d> colourFactory) {
        Vec3d[] colours = new Vec3d[MAX_POWER + 1];

        for (int power = MIN_POWER; power <= MAX_POWER; power++) {
            colours[power] = colourFactory.apply(power);
        }

        return colours;
    }

    private static Vec3d createRedColour(int power) {
        float strength = (float) power / MAX_POWER;

        float red = strength * 0.6F + (strength > 0.0F ? 0.4F : 0.3F);
        float green = MathHelper.clamp(strength * strength * 0.7F - 0.5F, 0.0F, 1.0F);
        float blue = MathHelper.clamp(strength * strength * 0.6F - 0.7F, 0.0F, 1.0F);

        return new Vec3d(red, green, blue);
    }

    private static Vec3d createBlueColour(int power) {
        float strength = (float) power / MAX_POWER;

        float blue = strength * 0.6F + 0.4F;
        float red = MathHelper.clamp(strength * strength * 0.35F + 0.15F, 0.0F, 1.0F);
        float green = MathHelper.clamp(strength * strength * 0.4F + 0.2F, 0.0F, 1.0F);

        return new Vec3d(red, green, blue);
    }

    private static Vec3d createGreenColour(int power) {
        float strength = (float) power / MAX_POWER;

        float green = strength * 0.6F + 0.4F;
        float blue = MathHelper.clamp(strength * strength * 0.35F + 0.15F, 0.0F, 1.0F);
        float red = MathHelper.clamp(strength * strength * 0.25F, 0.0F, 1.0F);

        return new Vec3d(red, green, blue);
    }

    private static Vec3d createYellowColour(int power) {
        float strength = (float) power / MAX_POWER;

        float red = strength * 0.6F + 0.4F;
        float green = strength * 0.5F + 0.3F;
        float blue = MathHelper.clamp(strength * strength * 0.4F + 0.2F, 0.0F, 1.0F);

        return new Vec3d(red, green, blue);
    }
}
