package com.glodblock.github.loader;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.*;
import cpw.mods.fml.relauncher.Side;

public class ChannelLoader implements Runnable {
    @Override
    @SuppressWarnings("all")
    public void run() {
        int id = 0;
        FluidCraft.proxy.netHandler.registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketFluidPatternTermBtns.Handler(), CPacketFluidPatternTermBtns.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketEncodePattern.Handler(), CPacketEncodePattern.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new SPacketMEInventoryUpdate.Handler(), SPacketMEInventoryUpdate.class, id ++, Side.CLIENT);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketCraftRequest.Handler(), CPacketCraftRequest.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketInventoryAction.Handler(), CPacketInventoryAction.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketSetTargetItem.Handler(), CPacketSetTargetItem.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id ++, Side.CLIENT);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketTransferRecipe.Handler(), CPacketTransferRecipe.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketDumpTank.Handler(), CPacketDumpTank.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new SPacketFluidUpdate.Handler(), SPacketFluidUpdate.class, id ++, Side.CLIENT);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketPatternValueSet.Handler(), CPacketPatternValueSet.class, id ++, Side.SERVER);
    }
}
