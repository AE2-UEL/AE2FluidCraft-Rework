package com.glodblock.github.inventory.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface GuiFactory {

    @Nullable
    Object createServerGui(EntityPlayer player, World world, int x, int y, int z, EnumFacing face);

    @SideOnly(Side.CLIENT)
    @Nullable
    Object createClientGui(EntityPlayer player, World world, int x, int y, int z, EnumFacing face);

}
