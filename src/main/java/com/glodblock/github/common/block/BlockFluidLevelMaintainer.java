package com.glodblock.github.common.block;

import appeng.block.AEBaseTileBlock;
import com.glodblock.github.common.tile.TileFluidLevelMaintainer;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

public class BlockFluidLevelMaintainer extends AEBaseTileBlock {

    public static final PropertyDirection facingProperty = PropertyDirection.create("facing",EnumFacing.Plane.HORIZONTAL);

    public BlockFluidLevelMaintainer() {
        super(Material.IRON);
        setTileEntity(TileFluidLevelMaintainer.class);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
        }
        TileFluidLevelMaintainer tile = getTileEntity(world, pos);
        if (tile != null) {
            if (!world.isRemote) {
                InventoryHandler.openGui(player, world, pos, facing, GuiType.FLUID_LEVEL_MAINTAINER);
            }
            return true;
        }

        return super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{facingProperty},new IUnlistedProperty[]{});
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileFluidLevelMaintainer)
        {
            if (((TileFluidLevelMaintainer) tileEntity).facing != null)
            {
                return state.withProperty(facingProperty,((TileFluidLevelMaintainer) tileEntity).facing);
            }
        }
        return state;
    }

    @Override
    public void onBlockPlacedBy(World w, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack is) {
        super.onBlockPlacedBy(w, pos, state, placer, is);
        TileEntity tileEntity = w.getTileEntity(pos);
        if (tileEntity instanceof TileFluidLevelMaintainer){
            ((TileFluidLevelMaintainer) tileEntity).facing = placer.getHorizontalFacing().getOpposite();
        }
    }
}
