package com.glodblock.github.common.storage;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.*;
import appeng.client.texture.ExtraBlockTextures;
import com.glodblock.github.util.ModAndClassUtil;
import extracells.network.GuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

public class FluidCellHandler implements ICellHandler
{

    @Override
    public boolean isCell( final ItemStack is )
    {
        return FluidCellInventory.isCell( is );
    }

    @Override
    public IMEInventoryHandler getCellInventory(final ItemStack is, final ISaveProvider container, final StorageChannel channel )
    {
        if( channel == StorageChannel.FLUIDS )
        {
            return FluidCellInventory.getCell( is, container );
        }
        return null;
    }

    @Override
    public IIcon getTopTexture_Light()
    {
        return ExtraBlockTextures.BlockMEChestItems_Light.getIcon();
    }

    @Override
    public IIcon getTopTexture_Medium()
    {
        return ExtraBlockTextures.BlockMEChestItems_Medium.getIcon();
    }

    @Override
    public IIcon getTopTexture_Dark()
    {
        return ExtraBlockTextures.BlockMEChestItems_Dark.getIcon();
    }

    @Override
    public void openChestGui(final EntityPlayer player, final IChestOrDrive chest, final ICellHandler cellHandler, final IMEInventoryHandler inv, final ItemStack is, final StorageChannel chan )
    {
        if (ModAndClassUtil.EC2) {
            if (chan == StorageChannel.FLUIDS) {
                IStorageMonitorable monitorable = null;
                if (chest != null) {
                    monitorable = ((IMEChest)chest).getMonitorable(ForgeDirection.UNKNOWN, new PlayerSource(player, chest));
                }
                if (monitorable != null) {
                    GuiHandler.launchGui(GuiHandler.getGuiId(0), player, new Object[]{monitorable.getFluidInventory()});
                }
            }
        }
    }

    @Override
    public int getStatusForCell( final ItemStack is, final IMEInventory handler )
    {
        if( handler instanceof FluidCellInventoryHandler)
        {
            final FluidCellInventoryHandler ci = (FluidCellInventoryHandler) handler;
            return ci.getStatusForCell();
        }
        return 0;
    }

    @Override
    public double cellIdleDrain( final ItemStack is, final IMEInventory handler )
    {
        final IFluidCellInventory inv = ( (IFluidCellInventoryHandler) handler ).getCellInv();
        return inv.getIdleDrain();
    }
}
