package com.glodblock.github.common.block;

import appeng.block.AEBaseItemBlock;
import com.glodblock.github.common.tile.TileFluidDiscretizer;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.material.Material;

public class BlockFluidDiscretizer extends FCBaseBlock {

    public BlockFluidDiscretizer() {
        super(Material.iron, NameConst.BLOCK_FLUID_DISCRETIZER);
        setFullBlock(true);
        setOpaque(true);
        setTileEntity(TileFluidDiscretizer.class);
    }

    public BlockFluidDiscretizer register() {
        GameRegistry.registerBlock(this, AEBaseItemBlock.class, NameConst.BLOCK_FLUID_DISCRETIZER);
        GameRegistry.registerTileEntity(TileFluidDiscretizer.class, NameConst.BLOCK_FLUID_DISCRETIZER);
        return this;
    }

}
