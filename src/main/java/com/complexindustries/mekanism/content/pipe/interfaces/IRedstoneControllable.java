package com.complexindustries.mekanism.content.pipe.interfaces;

import mekanism.common.tile.interfaces.IRedstoneControl.RedstoneControl;

public interface IRedstoneControllable {

    RedstoneControl getRedstoneMode();

    void setRedstoneMode(RedstoneControl mode);

    boolean isRedstonePowered();

    default boolean canOperate() {
        RedstoneControl mode = getRedstoneMode();
        if (mode == RedstoneControl.DISABLED) {
            return true;
        } else if (mode == RedstoneControl.HIGH) {
            return isRedstonePowered();
        } else if (mode == RedstoneControl.LOW) {
            return !isRedstonePowered();
        }
        return true;
    }

    static RedstoneControl byIndex(int index) {
        RedstoneControl[] values = RedstoneControl.values();
        if (index >= 0 && index < values.length) {
            return values[index];
        }
        return RedstoneControl.DISABLED;
    }
}
