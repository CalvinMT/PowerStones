package com.calvinmt.powerstones;

import net.minecraft.util.StringIdentifiable;

public enum PowerPair implements StringIdentifiable {
    RED_BLUE("rb"),
    GREEN_YELLOW("gy");

    private final String name;

    private PowerPair(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

}
