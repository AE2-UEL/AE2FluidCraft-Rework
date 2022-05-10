package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import appeng.core.features.AEFeature;
import com.glodblock.github.FluidCraft;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;

import java.util.EnumSet;

public class FCBaseBlock extends AEBaseTileBlock {

    public FCBaseBlock(Material mat, String name) {
        super(mat);
        this.setBlockName(name);
        this.setBlockTextureName(FluidCraft.MODID + ":" + name);
    }

    @Override
    public void setTileEntity( final Class<? extends TileEntity> clazz ) {
        super.setTileEntity(clazz);
    }

    public void setOpaque(boolean opaque) {
        this.isOpaque = opaque;
    }

    public void setFullBlock(boolean full) {
        this.isFullSize = full;
    }

    @Override
    public void setFeature( final EnumSet<AEFeature> f ) {
        super.setFeature(f);
    }

}
