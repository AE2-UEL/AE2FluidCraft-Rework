package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import com.glodblock.github.common.tile.TileGasDiscretizer;
import net.minecraft.block.material.Material;

public class BlockGasDiscretizer extends AEBaseTileBlock {

    public BlockGasDiscretizer() {
        super(Material.IRON);
        setTileEntity(TileGasDiscretizer.class);
    }

}