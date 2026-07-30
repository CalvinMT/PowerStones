package com.calvinmt.powerstones;

import java.util.Arrays;
import java.util.List;

import net.minecraft.util.StringRepresentable;

public enum PowerPair implements StringRepresentable  {
    RED_BLUE("rb", PowerColour.RED, PowerColour.BLUE),
    RED_GREEN("rg", PowerColour.RED, PowerColour.GREEN),
    RED_YELLOW("ry", PowerColour.RED, PowerColour.YELLOW),
    BLUE_GREEN("bg", PowerColour.BLUE, PowerColour.GREEN),
    BLUE_YELLOW("by", PowerColour.BLUE, PowerColour.YELLOW),
    GREEN_YELLOW("gy", PowerColour.GREEN, PowerColour.YELLOW);

    private final String name;
    private final PowerColour colourA;
    private final PowerColour colourB;

    PowerPair(String name, PowerColour colourA, PowerColour colourB) {
        this.name = name;
        this.colourA = colourA;
        this.colourB = colourB;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public boolean contains(PowerColour colour) {
        return this.colourA == colour || this.colourB == colour;
    }

    public boolean hasRed() {
        return contains(PowerColour.RED);
    }

    public boolean hasBlue() {
        return contains(PowerColour.BLUE);
    }

    public boolean hasGreen() {
        return contains(PowerColour.GREEN);
    }

    public boolean hasYellow() {
        return contains(PowerColour.YELLOW);
    }

    public boolean sharesColourWith(PowerPair other) {
        return this.contains(other.getColourA()) || this.contains(other.getColourB());
    }

    public PowerColour getColourA() {
        return this.colourA;
    }

    public PowerColour getColourB() {
        return this.colourB;
    }

    public PowerChannel getChannel(PowerColour colour) {
        if (this.colourA == colour) return PowerChannel.A;
        if (this.colourB == colour) return PowerChannel.B;

        throw new IllegalArgumentException(colour + " is not contained in power pair " + this);
    }

    public static PowerPair getPairFromColours(PowerColour colourA, PowerColour colourB) {
        for (PowerPair pair : values()) {
            if (pair.contains(colourA) && pair.contains(colourB)) {
                return pair;
            }
        }

        throw new IllegalArgumentException("No power pair exists for " + colourA + " and " + colourB);
    }

    public static List<PowerPair> getPairsFor(PowerColour colour) {
        return Arrays.stream(values()).filter(pair -> pair.contains(colour)).toList();
    }
}
