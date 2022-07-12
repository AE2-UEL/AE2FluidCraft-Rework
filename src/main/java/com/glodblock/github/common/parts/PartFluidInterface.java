package com.glodblock.github.common.parts;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.client.texture.CableBusTextures;
import appeng.core.settings.TickRates;
import appeng.me.GridAccessException;
import appeng.parts.misc.PartInterface;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.AEFluidInventory;
import com.glodblock.github.inventory.IAEFluidInventory;
import com.glodblock.github.inventory.IAEFluidTank;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.DualityFluidInterface;
import com.glodblock.github.util.Util;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.io.IOException;
import java.util.Objects;

public class PartFluidInterface extends PartInterface implements IFluidHandler, IAEFluidInventory {

    private final BaseActionSource ownActionSource = new MachineSource(this);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 6 );
    private final DualityFluidInterface fluidDuality = new DualityFluidInterface(this.getProxy(), this);

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

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        fluidDuality.onChannelStateChange(c);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        fluidDuality.onPowerStateChange(c);
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        fluidDuality.gridChanged();
    }

    public DualityFluidInterface getDualityFluid() {
        return fluidDuality;
    }

    public AEFluidInventory getInternalFluid() {
        return fluidDuality.getTanks();
    }

    public AppEngInternalAEInventory getConfig() {
        for (int i = 0; i < fluidDuality.getConfig().getSlots(); i ++) {
            IAEFluidStack fluid = fluidDuality.getConfig().getFluidInSlot(i);
            if (fluid == null) {
                config.setInventorySlotContents(i, null);
            }
            else {
                config.setInventorySlotContents(i, ItemFluidPacket.newDisplayStack(fluid.getFluidStack()));
            }
        }
        return config;
    }

    @Override
    public void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        for (int i = 0; i < config.getSizeInventory(); i++) {
            ByteBufUtils.writeItemStack(data, config.getStackInSlot(i));
            for (int j = 0; j < config.getSizeInventory(); j ++) {
                FluidStack fluid = ItemFluidPacket.getFluidStack(config.getStackInSlot(j));
                fluidDuality.getConfig().setFluidInSlot(j, fluidDuality.getStandardFluid(fluid));
            }
        }
        int fluidMask = 0;
        for (int i = 0; i < getInternalFluid().getSlots(); i++) {
            if (getInternalFluid().getFluidInSlot(i) != null) {
                fluidMask |= 1 << i;
            }
        }
        data.writeByte(fluidMask);
        for (int i = 0; i < getInternalFluid().getSlots(); i++) {
            IAEFluidStack fluid = getInternalFluid().getFluidInSlot(i);
            if (fluid != null) {
                fluid.writeToPacket(data);
            }
        }
    }

    @Override
    public boolean readFromStream(ByteBuf data) throws IOException {
        super.readFromStream(data);
        boolean changed = false;
        for (int i = 0; i < config.getSizeInventory(); i++) {
            ItemStack stack = ByteBufUtils.readItemStack(data);
            if (!ItemStack.areItemStacksEqual(stack, config.getStackInSlot(i))) {
                config.setInventorySlotContents(i, stack);
                changed = true;
            }
            for (int j = 0; j < config.getSizeInventory(); j ++) {
                FluidStack fluid = ItemFluidPacket.getFluidStack(config.getStackInSlot(j));
                fluidDuality.getConfig().setFluidInSlot(j, fluidDuality.getStandardFluid(fluid));
            }
        }
        int fluidMask = data.readByte();
        for (int i = 0; i < getInternalFluid().getSlots(); i++) {
            if ((fluidMask & (1 << i)) != 0) {
                IAEFluidStack fluid = AEFluidStack.loadFluidStackFromPacket(data);
                if (fluid != null) { // this shouldn't happen, but better safe than sorry
                    IAEFluidStack origFluid = getInternalFluid().getFluidInSlot(i);
                    if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                        getInternalFluid().setFluidInSlot(i, fluid);
                        changed = true;
                    }
                }
            } else if (getInternalFluid().getFluidInSlot(i) != null) {
                getInternalFluid().setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void readFromNBT( NBTTagCompound data ) {
        super.readFromNBT( data );
        config.readFromNBT(data, "ConfigInv");
        for (int i = 0; i < config.getSizeInventory(); i ++) {
            FluidStack fluid = ItemFluidPacket.getFluidStack(config.getStackInSlot(i));
            fluidDuality.getConfig().setFluidInSlot(i, fluidDuality.getStandardFluid(fluid));
        }
        getInternalFluid().readFromNBT(data, "FluidInv");
    }

    @Override
    public void writeToNBT( NBTTagCompound data ) {
        super.writeToNBT(data);
        config.writeToNBT(data, "ConfigInv");
        getInternalFluid().writeToNBT(data, "FluidInv");
    }

    @Override
    public int getInstalledUpgrades( final Upgrades u )
    {
        return getInterfaceDuality().getInstalledUpgrades( u );
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
            remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.MODULATE, ownActionSource);
        } else {
            remove = fluidGrid.injectItems(AEFluidStack.create(resource), Actionable.SIMULATE, ownActionSource);
        }
        return remove == null ? ori : (int) (ori - remove.getStackSize());
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return fluidDuality.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return fluidDuality.drain(from, maxDrain, doDrain);
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return fluidDuality.getTankInfo(from);
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos)
    {
        if( player.isSneaking() )
        {
            return false;
        }
        if( Platform.isServer() )
        {
            InventoryHandler.openGui(player, this.getHost().getTile().getWorldObj(), new BlockPos(this.getHost().getTile()), Objects.requireNonNull(Util.from(this.getSide())), GuiType.DUAL_INTERFACE_PART);
        }
        return true;
    }

    @Override
    public void onFluidInventoryChanged(IAEFluidTank inv, int slot) {
        saveChanges();
        getTileEntity().markDirty();
        fluidDuality.onFluidInventoryChanged(inv, slot);
    }

    public void setConfig(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            config.setInventorySlotContents(id, ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack()));
            fluidDuality.getConfig().setFluidInSlot(id, fluidDuality.getStandardFluid(fluid));
        }
    }

    public void setFluidInv(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            getInternalFluid().setFluidInSlot(id, fluid);
        }
    }

    @Override
    public TickingRequest getTickingRequest( final IGridNode node )
    {
        TickingRequest item = super.getTickingRequest(node);
        TickingRequest fluid = fluidDuality.getTickingRequest(node);
        return new TickingRequest(
            Math.min(item.minTickRate, fluid.minTickRate),
            Math.max(item.maxTickRate, fluid.maxTickRate),
            item.isSleeping && fluid.isSleeping,
            true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        TickRateModulation item = super.tickingRequest(node, TicksSinceLastCall);
        TickRateModulation fluid = fluidDuality.tickingRequest(node, TicksSinceLastCall);
        if (item.ordinal() >= fluid.ordinal()) {
            return item;
        } else {
            return fluid;
        }
    }

}
