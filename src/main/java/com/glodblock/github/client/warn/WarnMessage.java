package com.glodblock.github.client.warn;

import com.glodblock.github.FluidCraft;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WarnMessage {

    public static WarnMessage INSTANCE = new WarnMessage();
    private boolean finished = false;
    private WarnMessage() {
        // NO-OP
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent(receiveCanceled = true)
    public void onTick(TickEvent.ClientTickEvent event) {
        if (FluidCraft.beta && !finished && Minecraft.getMinecraft().player != null) {
            EntityPlayer player = Minecraft.getMinecraft().player;
            finished = true;
            player.sendMessage(new TextComponentString("AE2FC 2.5.3-r is on beta test!"));
            player.sendMessage(new TextComponentString("If you encounter any bug or something not working"));
            player.sendMessage(new TextComponentString("Please report it to https://github.com/GlodBlock/AE2FluidCraft-Rework"));
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }

}
