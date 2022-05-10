package com.glodblock.github.inventory.gui;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import com.glodblock.github.util.Util;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import javax.annotation.Nullable;

public abstract class PartGuiFactory<T> extends TileGuiFactory<T> {

    PartGuiFactory(Class<T> invClass) {
        super(invClass);
    }

    @Nullable
    @Override
    protected T getInventory(TileEntity tile, EnumFacing face) {
        if (tile instanceof IPartHost) {
            IPart part = ((IPartHost)tile).getPart(Util.from(face));
            if (invClass.isInstance(part)) {
                return invClass.cast(part);
            }
        }
        return null;
    }
}
