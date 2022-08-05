package com.glodblock.github.client.gui.container;

import appeng.api.storage.data.IAEFluidStack;
import appeng.container.AEBaseContainer;
import appeng.container.slot.SlotNormal;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.TankDumpable;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.network.SPacketFluidUpdate;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;

import java.util.HashMap;
import java.util.Map;

public class ContainerLargeIngredientBuffer extends AEBaseContainer implements TankDumpable {

    private final TileLargeIngredientBuffer tile;

    public ContainerLargeIngredientBuffer(InventoryPlayer ipl, TileLargeIngredientBuffer tile) {
        super(ipl, tile);
        this.tile = tile;
        IInventory inv = tile.getInternalInventory();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new SlotNormal(inv, i * 9 + j, 8 + 18 * j, 72 + 18 * i));
            }
        }
        bindPlayerInventory(ipl, 0, 140);
    }

    public TileLargeIngredientBuffer getTile() {
        return tile;
    }

    @Override
    public boolean canDumpTank(int index) {
        return tile.getFluidInventory().getFluidInSlot(index) != null;
    }

    @Override
    public void dumpTank(int index) {
        if (index >= 0 && index < tile.getFluidInventory().getSlots()) {
            tile.getFluidInventory().setFluidInSlot(index, null);
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        Map<Integer, IAEFluidStack> tmp = new HashMap<>();
        for (int i = 0; i < tile.getFluidInventory().getSlots(); i ++) {
            tmp.put(i, tile.getFluidInventory().getFluidInSlot(i));
        }
        for( final Object g : this.crafters )
        {
            if( g instanceof EntityPlayer)
            {
                FluidCraft.proxy.netHandler.sendTo(new SPacketFluidUpdate(tmp), (EntityPlayerMP) g);
            }
        }
    }

}
