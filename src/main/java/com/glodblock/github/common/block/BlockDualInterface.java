package com.glodblock.github.common.block;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.util.Platform;
import com.glodblock.github.client.container.ContainerItemDualInterface;
import com.glodblock.github.common.tile.TileDualInterface;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class BlockDualInterface extends AEBaseTileBlock<TileDualInterface> {

    private static final BooleanProperty OMNIDIRECTIONAL = BooleanProperty.create("omnidirectional");
    private static final DirectionProperty FACING = DirectionProperty.create("facing", EnumSet.allOf(Direction.class));

    public BlockDualInterface() {
        super(defaultProps(Material.IRON));
        this.setTileEntity(TileDualInterface.class, TileDualInterface::new);
    }

    @Override
    public ActionResultType onActivated(World w, BlockPos pos, PlayerEntity p, Hand hand, @Nullable ItemStack heldItem, BlockRayTraceResult hit) {
        if (p.isSneaking()) {
            return ActionResultType.PASS;
        }
        final TileDualInterface tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                ContainerOpener.openContainer(
                        ContainerItemDualInterface.TYPE,
                        p,
                        ContainerLocator.forTileEntitySide(tg, hit.getFace())
                );
            }
            return ActionResultType.func_233537_a_(w.isRemote);
        }
        return ActionResultType.PASS;
    }

    @Override
    protected void fillStateContainer(@Nonnull StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(OMNIDIRECTIONAL, FACING);
    }

    @Override
    protected BlockState updateBlockStateFromTileEntity(BlockState currentState, TileDualInterface te) {
        return currentState.with(OMNIDIRECTIONAL, te.isOmniDirectional()).with(FACING, te.getForward());
    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }

    @Override
    protected void customRotateBlock(final IOrientable rotatable, final Direction axis) {
        if (rotatable instanceof TileDualInterface) {
            ((TileDualInterface) rotatable).setSide(axis);
        }
    }

}