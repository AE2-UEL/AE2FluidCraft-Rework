package com.glodblock.github.common.block;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseTileBlock;
import appeng.util.Platform;
import com.glodblock.github.common.tile.TileDualInterface;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockDualInterface extends AEBaseTileBlock {

    private static final PropertyBool OMNIDIRECTIONAL = PropertyBool.create("omnidirectional");
    private static final PropertyDirection FACING = PropertyDirection.create("facing");

    public BlockDualInterface() {
        super(Material.IRON);
        this.setTileEntity(TileDualInterface.class);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected IProperty[] getAEStates() {
        return new IProperty[] { OMNIDIRECTIONAL };
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, OMNIDIRECTIONAL, FACING);
    }

    @Override
    public boolean onActivated(final World w, final BlockPos pos, final EntityPlayer p,
                               final EnumHand hand, final @Nullable ItemStack heldItem, final EnumFacing side,
                               final float hitX, final float hitY, final float hitZ) {
        if (p.isSneaking()) {
            return false;
        }
        final TileDualInterface tg = this.getTileEntity(w, pos);
        if (tg != null) {
            if (Platform.isServer()) {
                InventoryHandler.openGui(p, w, pos, side, GuiType.DUAL_ITEM_INTERFACE);
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    @Nonnull
    public IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
        TileDualInterface te = this.getTileEntity(world, pos);
        return te == null ? state
                : state.withProperty(OMNIDIRECTIONAL, te.isOmniDirectional()).withProperty(FACING, te.getForward());
    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }

    @Override
    protected void customRotateBlock(final IOrientable rotatable, final EnumFacing axis) {
        if (rotatable instanceof TileDualInterface) {
            ((TileDualInterface)rotatable).setSide(axis);
        }
    }

}
