package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.fluids.util.AEFluidInventory;
import appeng.tile.inventory.AppEngInternalInventory;
import com.glodblock.github.client.container.ContainerLargeIngredientBuffer;
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

public class BlockLargeIngredientBuffer extends AEBaseTileBlock<BlockLargeIngredientBuffer.TileLargeIngredientBuffer> {

    public BlockLargeIngredientBuffer() {
        super(defaultProps(Material.IRON).setOpaque((x, y, z) -> false).notSolid());
        setTileEntity(TileLargeIngredientBuffer.class, TileLargeIngredientBuffer::new);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (player.isSneaking()) {
            return super.onBlockActivated(state, world, pos, player, hand, hit);
        }
        TileLargeIngredientBuffer tile = this.getTileEntity(world, pos);
        if (tile != null) {
            if (!world.isRemote) {
                ContainerOpener.openContainer(
                        ContainerLargeIngredientBuffer.TYPE,
                        player,
                        ContainerLocator.forTileEntitySide(tile, hit.getFace())
                );
            }
            return ActionResultType.func_233537_a_(world.isRemote);
        }
        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

    public static class TileLargeIngredientBuffer extends TileSimpleBuffer {

        public TileLargeIngredientBuffer() {
            super(FCUtil.getTileType(TileLargeIngredientBuffer.class, FCBlocks.LARGE_INGREDIENT_BUFFER));
        }

        @Override
        protected AppEngInternalInventory createItemBuffer() {
            return new AppEngInternalInventory(this, 27);
        }

        @Override
        protected AEFluidInventory createFluidBuffer() {
            return new AEFluidInventory(this, 7, 16000);
        }
    }

}