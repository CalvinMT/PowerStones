package com.calvinmt.powerstones;

public interface RedstoneWireBlockInterface {

    default void setPowerStoneType(PowerStoneType color) {}

    default PowerStoneType getPowerStoneType() { return PowerStoneType.RED; }

}
