package com.glodblock.github.client.gui.container;

import appeng.api.config.Upgrades;
import appeng.api.implementations.IUpgradeableHost;
import appeng.api.storage.data.IAEFluidStack;
import appeng.container.implementations.ContainerUpgradeable;
import appeng.container.slot.AppEngSlot;
import appeng.tile.inventory.AppEngInternalAEInventory;
import appeng.util.Platform;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.slot.OptionalFluidSlotFakeTypeOnly;
import com.glodblock.github.network.SPacketFluidUpdate;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

public abstract class ContainerFluidConfigurable extends ContainerUpgradeable {

    public ContainerFluidConfigurable(InventoryPlayer ip, IUpgradeableHost te)
    {
        super( ip, te );
    }

    public abstract AppEngInternalAEInventory getFakeFluidInv();

    @Override
    public ItemStack transferStackInSlot(final EntityPlayer p, final int idx )
    {
        AppEngSlot clickSlot = (AppEngSlot)this.inventorySlots.get(idx);
        if (clickSlot == null || !clickSlot.getHasStack()) return null;

        ItemStack tis = clickSlot.getStack();
        FluidStack fs = Util.getFluidFromItem(tis);
        if( fs != null )
        {
            final AppEngInternalAEInventory inv = this.getFakeFluidInv();
            final IAEFluidStack stack = AEFluidStack.create( fs );
            for( int i = 0; i < inv.getSizeInventory(); ++i )
            {
                if( inv.getStackInSlot(i) == null && this.isValidForConfig( i, stack ) )
                {
                    ItemStack tmp = ItemFluidPacket.newStack(fs);
                    if (tmp != null) {
                        tmp.setStackDisplayName(fs.getLocalizedName());
                    }
                    inv.setInventorySlotContents( i, tmp );
                    break;
                }
            }
        }
        this.standardDetectAndSendChanges();
        return null;
    }

    protected void setupConfig()
    {
        this.setupUpgrades();

        final IInventory inv = Ae2Reflect.getUpgradeList(this).getInventoryByName( "config" );
        final int y = 40;
        final int x = 80;
        this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 0, x, y, 0, 0, 0 ) );
        if( this.supportCapacity() )
        {
            this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 1, x, y, -1, 0, 1 ) );
            this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 2, x, y, 1, 0, 1 ) );
            this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 3, x, y, 0, -1, 1 ) );
            this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 4, x, y, 0, 1, 1 ) );

            this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 5, x, y, -1, -1, 2 ) );
            this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 6, x, y, 1, -1, 2 ) );
            this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 7, x, y, -1, 1, 2 ) );
            this.addSlotToContainer( new OptionalFluidSlotFakeTypeOnly( inv, this, 8, x, y, 1, 1, 2 ) );
        }
    }

    protected boolean isValidForConfig( int slot, IAEFluidStack fs )
    {
        if( this.supportCapacity() )
        {
            // assumes 4 slots per upgrade
            final int upgrades = Ae2Reflect.getUpgradeList(this).getInstalledUpgrades( Upgrades.CAPACITY );

            if( slot > 0 && upgrades < 1 )
            {
                return false;
            }
            return slot <= 4 || upgrades >= 2;
        }

        return true;
    }

    @Override
    protected void standardDetectAndSendChanges()
    {
        if( Platform.isServer() )
        {
            // clear out config items that are no longer valid (eg capacity upgrade removed)
            final AppEngInternalAEInventory inv = this.getFakeFluidInv();
            final Map<Integer, IAEFluidStack> tmp = new HashMap<>();
            for( int i = 0; i < inv.getSizeInventory(); ++i )
            {
                if( inv.getStackInSlot( i ) != null && !this.isValidForConfig( i, AEFluidStack.create(ItemFluidPacket.getFluidStack(inv.getStackInSlot( i )))) )
                {
                    inv.setInventorySlotContents( i, null );
                }
                tmp.put(i, AEFluidStack.create(ItemFluidPacket.getFluidStack(inv.getStackInSlot( i ))));
            }
            for( final Object g : this.crafters )
            {
                if( g instanceof EntityPlayer)
                {
                    FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(tmp), (EntityPlayerMP) g);
                }
            }
        }
        super.standardDetectAndSendChanges();
    }

}
