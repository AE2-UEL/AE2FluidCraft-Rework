package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import com.glodblock.github.common.tile.TileFluidDiscretizer;
import net.minecraft.block.material.Material;

public class BlockFluidDiscretizer extends AEBaseTileBlock<TileFluidDiscretizer> {

    public BlockFluidDiscretizer() {
        super(defaultProps(Material.IRON));
        setTileEntity(TileFluidDiscretizer.class, TileFluidDiscretizer::new);
    }

}
