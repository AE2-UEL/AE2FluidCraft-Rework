package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import com.glodblock.github.client.container.ContainerFluidPacketDecoder;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

public class BlockFluidPacketDecoder extends AEBaseTileBlock<TileFluidPacketDecoder> {

    public BlockFluidPacketDecoder() {
        super(defaultProps(Material.IRON));
        setTileEntity(TileFluidPacketDecoder.class, TileFluidPacketDecoder::new);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (player.isSneaking()) {
            return super.onBlockActivated(state, world, pos, player, hand, hit);
        }
        TileFluidPacketDecoder tile = this.getTileEntity(world, pos);
        if (tile != null) {
            if (!world.isRemote) {
                ContainerOpener.openContainer(
                        ContainerFluidPacketDecoder.TYPE,
                        player,
                        ContainerLocator.forTileEntitySide(tile, hit.getFace())
                );
            }
            return ActionResultType.func_233537_a_(world.isRemote);
        }
        return super.onBlockActivated(state, world, pos, player, hand, hit);
    }

}
