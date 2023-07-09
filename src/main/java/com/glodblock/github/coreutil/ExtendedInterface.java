package com.glodblock.github.coreutil;

public interface ExtendedInterface {

    default boolean getFluidPacketMode() {
        return false;
    }

    default void setFluidPacketMode(boolean value) {

    }

    default boolean getSplittingMode() {
        return false;
    }

    default void setSplittingMode(boolean value) {

    }

    default int getExtendedBlockMode() {
        return 0;
    }

    default void setExtendedBlockMode(int value) {

    }

}
