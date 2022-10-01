package com.glodblock.github.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnull;
import java.util.Objects;

public class BlockPos {

    private final int x;
    private final int y;
    private final int z;
    private final World w;

    public BlockPos(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = null;
    }

    public BlockPos(@Nonnull TileEntity te) {
        this.x = te.xCoord;
        this.y = te.yCoord;
        this.z = te.zCoord;
        this.w = te.getWorldObj();
    }

    public BlockPos(int x, int y, int z, World w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public BlockPos getOffSet(ForgeDirection face) {
        return new BlockPos(this.x + face.offsetX, this.y + face.offsetY, this.z + face.offsetZ, this.w);
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

    public World getWorld() {
        return w;
    }

    public TileEntity getTileEntity() {
        if (w != null) {
            return w.getTileEntity(x, y, z);
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BlockPos) {
            BlockPos pos = (BlockPos) obj;
            return pos.x == x && pos.y == y && pos.z == z && Objects.equals(pos.w, w);
        }
        return false;
    }

}
