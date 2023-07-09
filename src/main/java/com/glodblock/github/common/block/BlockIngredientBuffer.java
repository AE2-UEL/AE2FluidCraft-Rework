package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.fluids.util.AEFluidInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import com.glodblock.github.client.container.ContainerIngredientBuffer;
import com.glodblock.github.common.tile.TileSimpleBuffer;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.util.FCUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockIngredientBuffer extends AEBaseTileBlock<BlockIngredientBuffer.TileIngredientBuffer> {

    public BlockIngredientBuffer() {
        super(defaultProps(Material.IRON).setOpaque((x, y, z) -> false).notSolid());
        setTileEntity(TileIngredientBuffer.class, TileIngredientBuffer::new);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (player.isSneaking()) {
            return super.onBlockActivated(state, world, pos, player, hand, hit);
        }
        TileIngredientBuffer tile = this.getTileEntity(world, pos);
        if (tile != null) {
            if (!world.isRemote) {
                ContainerOpener.openContainer(
                        ContainerIngredientBuffer.TYPE,
                        player,
                        ContainerLocator.forTileEntitySide(tile, hit.getFace())
                );
            }
            return ActionResultType.func_233537_a_(world.isRemote);
        }
        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    public static class TileIngredientBuffer extends TileSimpleBuffer {

        public TileIngredientBuffer() {
            super(FCUtil.getTileType(TileIngredientBuffer.class, FCBlocks.INGREDIENT_BUFFER));
        }

        @Override
        protected AppEngInternalInventory createItemBuffer() {
            return new AppEngInternalInventory(this, 9);
        }

        @Override
        protected AEFluidInventory createFluidBuffer() {
            return new AEFluidInventory(this, 4, 16000);
        }
    }

}
