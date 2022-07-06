package com.glodblock.github.common.parts;

import appeng.api.config.Actionable;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
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
    private final AEFluidInventory invFluids = new AEFluidInventory(this, 6, 16000);
    private final AppEngInternalAEInventory config = new AppEngInternalAEInventory( this, 6 );

    private final boolean[] tickState = new boolean[]{true, true, true, true, true, true};
    private final int[] tickCount = new int[]{0, 0, 0, 0, 0, 0};

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

    public AEFluidInventory getInternalFluid() {
        return invFluids;
    }

    public AppEngInternalAEInventory getConfig() {
        return config;
    }

    @Override
    public void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream( data );
        for (int i = 0; i < config.getSizeInventory(); i++) {
            ByteBufUtils.writeItemStack(data, config.getStackInSlot(i));
        }
        int fluidMask = 0;
        for (int i = 0; i < invFluids.getSlots(); i++) {
            if (invFluids.getFluidInSlot(i) != null) {
                fluidMask |= 1 << i;
            }
        }
        data.writeByte(fluidMask);
        for (int i = 0; i < invFluids.getSlots(); i++) {
            IAEFluidStack fluid = invFluids.getFluidInSlot(i);
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
        }
        int fluidMask = data.readByte();
        for (int i = 0; i < invFluids.getSlots(); i++) {
            if ((fluidMask & (1 << i)) != 0) {
                IAEFluidStack fluid = AEFluidStack.loadFluidStackFromPacket(data);
                if (fluid != null) { // this shouldn't happen, but better safe than sorry
                    IAEFluidStack origFluid = invFluids.getFluidInSlot(i);
                    if (!fluid.equals(origFluid) || fluid.getStackSize() != origFluid.getStackSize()) {
                        invFluids.setFluidInSlot(i, fluid);
                        changed = true;
                    }
                }
            } else if (invFluids.getFluidInSlot(i) != null) {
                invFluids.setFluidInSlot(i, null);
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public void readFromNBT( NBTTagCompound extra ) {
        super.readFromNBT( extra );
        config.readFromNBT(extra, "ConfigInv");
        invFluids.readFromNBT(extra, "FluidInv");
    }

    @Override
    public void writeToNBT( NBTTagCompound extra ) {
        super.writeToNBT(extra);
        config.writeToNBT(extra, "ConfigInv");
        invFluids.writeToNBT(extra, "FluidInv");
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
        return invFluids.drain(from, resource, doDrain);
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return invFluids.drain(from, maxDrain, doDrain);
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
        return invFluids.getTankInfo(from);
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
    }

    public void setConfig(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            config.setInventorySlotContents(id, ItemFluidPacket.newDisplayStack(fluid == null ? null : fluid.getFluidStack()));
        }
    }

    public void setFluidInv(int id, IAEFluidStack fluid) {
        if (id >= 0 && id < 6) {
            invFluids.setFluidInSlot(id, fluid);
        }
    }

    @Override
    public TickingRequest getTickingRequest( final IGridNode node )
    {
        return new TickingRequest( TickRates.Interface.getMin(), TickRates.Interface.getMax(), false, true );
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        //Very Hacky thing.
        if (!isActive()) {
            return TickRateModulation.SLEEP;
        }
        for (int i = 0; i < 6; i ++) {

            FluidStack configFluid = ItemFluidPacket.getFluidStack(config.getStackInSlot(i));
            FluidStack storedFluid = invFluids.getFluidInSlot(i) == null ? null : invFluids.getFluidInSlot(i).getFluidStack();

            if (!Util.areFluidsEqual(configFluid, storedFluid)) {
                if (storedFluid != null) {
                    int ori = storedFluid.amount;
                    int filled = fill(ForgeDirection.UNKNOWN, storedFluid, false);
                    if (ori == filled) {
                        fill(ForgeDirection.UNKNOWN, storedFluid, true);
                        invFluids.setFluidInSlot(i, null);
                    }
                    else {
                        tickState[i] = false;
                    }
                }
            }

            if (configFluid != null) {
                if (tickState[i]) {
                    if (getFluidGrid() != null) {
                        FluidStack configCopy = configFluid.copy();
                        configCopy.amount = Math.min(8000, getLeftSpace(storedFluid, configCopy));
                        IAEFluidStack fluidDrain = getFluidGrid().extractItems(AEFluidStack.create(configCopy), Actionable.MODULATE, ownActionSource);
                        if (fluidDrain != null && fluidDrain.getStackSize() != 0) {
                            invFluids.fill(i, fluidDrain.getFluidStack(), true);
                        } else {
                            tickState[i] = false;
                        }
                    }
                }
                else if (tickCount[i] % 40 == 0) {
                    if (getFluidGrid() != null) {
                        FluidStack configCopy = configFluid.copy();
                        configCopy.amount = Math.min(8000, getLeftSpace(storedFluid, configCopy));
                        IAEFluidStack fluidDrain = getFluidGrid().extractItems(AEFluidStack.create(configCopy), Actionable.MODULATE, ownActionSource);
                        if (fluidDrain != null && fluidDrain.getStackSize() != 0) {
                            invFluids.fill(i, fluidDrain.getFluidStack(), true);
                            tickState[i] = true;
                        }
                    }
                }
            }

            tickCount[i] ++;
            if (tickCount[i] > 500) {
                tickCount[i] = 1;
            }
        }
        super.tickingRequest(node, TicksSinceLastCall);
        return TickRateModulation.URGENT;
    }

    private int getLeftSpace(FluidStack stored, FluidStack req) {
        if (stored == null) {
            return 16000;
        }
        if (!Util.areFluidsEqual(stored, req)) {
            return 0;
        }
        return 16000 - stored.amount;
    }
}
