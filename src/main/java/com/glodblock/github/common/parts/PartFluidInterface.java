package com.glodblock.github.common.parts;

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.texture.CableBusTextures;
import appeng.me.GridAccessException;
import appeng.parts.misc.PartInterface;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.client.textures.FCPartsTexture;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class PartFluidInterface extends PartInterface implements IFluidHandler {

    private final BaseActionSource ownActionSource = new MachineSource(this);

    public PartFluidInterface(ItemStack is) {
        super(is);
    }

    @Override
    @SideOnly( Side.CLIENT )
    public void renderStatic(final int x, final int y, final int z, final IPartRenderHelper rh, final RenderBlocks renderer )
    {
        this.setRenderCache( rh.useSimplifiedRendering( x, y, z, this, this.getRenderCache() ) );
        rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), FCPartsTexture.BlockInterface_Face.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

        rh.setBounds( 2, 2, 14, 14, 14, 16 );
        rh.renderBlock( x, y, z, renderer );

        rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), FCPartsTexture.BlockInterface_Face.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

        rh.setBounds( 5, 5, 12, 11, 11, 13 );
        rh.renderBlock( x, y, z, renderer );

        rh.setTexture( CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), FCPartsTexture.BlockInterface_Face.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon(), CableBusTextures.PartMonitorSidesStatus.getIcon() );

        rh.setBounds( 5, 5, 13, 11, 11, 14 );
        rh.renderBlock( x, y, z, renderer );

        this.renderLights( x, y, z, rh, renderer );
    }

    @Override
    @SideOnly( Side.CLIENT )
    public void renderInventory( final IPartRenderHelper rh, final RenderBlocks renderer )
    {
        rh.setTexture( CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorBack.getIcon(), FCPartsTexture.BlockInterface_Face.getIcon(), CableBusTextures.PartMonitorSides.getIcon(), CableBusTextures.PartMonitorSides.getIcon() );

        rh.setBounds( 2, 2, 14, 14, 14, 16 );
        rh.renderInventoryBox( renderer );

        rh.setBounds( 5, 5, 12, 11, 11, 13 );
        rh.renderInventoryBox( renderer );

        rh.setBounds( 5, 5, 13, 11, 11, 14 );
        rh.renderInventoryBox( renderer );
    }

    private IMEMonitor<IAEFluidStack> getFluidGrid() {
        try {
            return getProxy().getGrid().<IStorageGrid>getCache(IStorageGrid.class).getFluidInventory();
        } catch (GridAccessException e) {
            return null;
        }
    }

    private IEnergyGrid getEnergyGrid() {
        try {
            return getProxy().getGrid().getCache(IEnergyGrid.class);
        } catch (GridAccessException e) {
            return null;
        }
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        IMEMonitor<IAEFluidStack> fluidGrid = getFluidGrid();
        IEnergyGrid energyGrid = getEnergyGrid();
        if (energyGrid == null || fluidGrid == null || resource == null)
            return 0;
        int ori = resource.amount;
        IAEFluidStack remove;
        if (doFill) {
            remove = Platform.poweredInsert(energyGrid, fluidGrid, AEFluidStack.create(resource), ownActionSource);
        } else {
            remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.SIMULATE, ownActionSource);
        }
        return remove == null ? ori : (int) (ori - remove.getStackSize());
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[]{new FluidTankInfo(null, 0)};
    }
}
