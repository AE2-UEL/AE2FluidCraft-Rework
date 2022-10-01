package com.glodblock.github.loader;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.*;
import net.minecraftforge.fml.relauncher.Side;

public class ChannelLoader implements Runnable {

    @Override
    @SuppressWarnings("all")
    public void run() {
        int id = 0;
        FluidCraft.proxy.netHandler.registerMessage(new CPacketSwitchGuis.Handler(), CPacketSwitchGuis.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketDumpTank.Handler(), CPacketDumpTank.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketTransposeFluid.Handler(), CPacketTransposeFluid.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketEncodePattern.Handler(), CPacketEncodePattern.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketLoadPattern.Handler(), CPacketLoadPattern.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketUpdateFluidLevel.Handler(), CPacketUpdateFluidLevel.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketFluidPatternTermBtns.Handler(), CPacketFluidPatternTermBtns.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new SPacketSetFluidLevel.Handler(), SPacketSetFluidLevel.class, id ++, Side.CLIENT);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketPatternValueSet.Handler(), CPacketPatternValueSet.class, id ++, Side.SERVER);
        FluidCraft.proxy.netHandler.registerMessage(new CPacketInventoryAction.Handler(), CPacketInventoryAction.class, id ++, Side.SERVER);
    }

}
