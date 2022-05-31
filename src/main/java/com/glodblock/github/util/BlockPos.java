package com.glodblock.github.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;

public class BlockPos {

    private final int x;
    private final int y;
    private final int z;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public BlockPos(@Nonnull TileEntity te) {
        this.x = te.xCoord;
        this.y = te.yCoord;
        this.z = te.zCoord;
    }

    public BlockPos getOffSet(ForgeDirection face) {
        return new BlockPos(this.x + face.offsetX, this.y + face.offsetY, this.z + face.offsetZ);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockPos) {
            BlockPos pos = (BlockPos) obj;
            return pos.x == x && pos.y == y && pos.z == z;
        }
        return false;
    }

}
