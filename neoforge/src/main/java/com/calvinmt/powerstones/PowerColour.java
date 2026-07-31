package com.calvinmt.powerstones;

import java.util.function.IntFunction;

import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public enum PowerColour {
    RED(0, PowerColour::createRedColour),
    BLUE(1, PowerColour::createBlueColour),
    GREEN(2, PowerColour::createGreenColour),
    YELLOW(3, PowerColour::createYellowColour);

    private static final int MIN_POWER = 0;
    private static final int MAX_POWER = 15;

    private final int tintIndex;
    private final Vec3[] colours;

    public static final int WHITE = ARGB.colorFromFloat(1.0F, 1.0F, 1.0F, 1.0F);

    PowerColour(int tintIndex, IntFunction<Vec3> colourFactory) {
        this.tintIndex = tintIndex;
        this.colours = createColourPalette(colourFactory);
    }

    public int getTintIndex() {
        return this.tintIndex;
    }

    public int getVectorColour(int powerLevel) {
        int clampedPower = Mth.clamp(powerLevel, MIN_POWER, MAX_POWER);

        Vec3 colour = this.colours[clampedPower];

        return ARGB.colorFromFloat(1.0F, (float) colour.x, (float) colour.y, (float) colour.z);
    }

    public int getWireColour(int powerLevel) {
        int clampedPower = Mth.clamp(powerLevel, MIN_POWER, MAX_POWER);

        Vec3 colour = this.colours[clampedPower];

        return ARGB.colorFromFloat(1.0F, (float) colour.x, (float) colour.y, (float) colour.z);
    }

    public Vec3 getColour(int powerLevel) {
        int clampedPower = Mth.clamp(powerLevel, MIN_POWER, MAX_POWER);
        return this.colours[clampedPower];
    }

    public Vec3[] getColours() {
        return this.colours;
    }

    private static Vec3[] createColourPalette(IntFunction<Vec3> colourFactory) {
        Vec3[] colours = new Vec3[MAX_POWER + 1];

        for (int power = MIN_POWER; power <= MAX_POWER; power++) {
            colours[power] = colourFactory.apply(power);
        }

        return colours;
    }

    private static Vec3 createRedColour(int power) {
        float strength = (float) power / MAX_POWER;

        float red = strength * 0.6F + (strength > 0.0F ? 0.4F : 0.3F);
        float green = Mth.clamp(strength * strength * 0.7F - 0.5F, 0.0F, 1.0F);
        float blue = Mth.clamp(strength * strength * 0.6F - 0.7F, 0.0F, 1.0F);

        return new Vec3(red, green, blue);
    }

    private static Vec3 createBlueColour(int power) {
        float strength = (float) power / MAX_POWER;

        float blue = strength * 0.6F + 0.4F;
        float red = Mth.clamp(strength * strength * 0.35F + 0.15F, 0.0F, 1.0F);
        float green = Mth.clamp(strength * strength * 0.4F + 0.2F, 0.0F, 1.0F);

        return new Vec3(red, green, blue);
    }

    private static Vec3 createGreenColour(int power) {
        float strength = (float) power / MAX_POWER;

        float green = strength * 0.6F + 0.4F;
        float blue = Mth.clamp(strength * strength * 0.35F + 0.15F, 0.0F, 1.0F);
        float red = Mth.clamp(strength * strength * 0.25F, 0.0F, 1.0F);

        return new Vec3(red, green, blue);
    }

    private static Vec3 createYellowColour(int power) {
        float strength = (float) power / MAX_POWER;

        float red = strength * 0.6F + 0.4F;
        float green = strength * 0.5F + 0.3F;
        float blue = Mth.clamp(strength * strength * 0.4F + 0.2F, 0.0F, 1.0F);

        return new Vec3(red, green, blue);
    }
}
