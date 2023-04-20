package com.calvinmt.powerstones;

import net.minecraft.util.StringRepresentable;

public enum PowerPair implements StringRepresentable {
    RED_BLUE("rb"),
    GREEN_YELLOW("gy");

    private final String name;

    private PowerPair(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

}
