package com.glodblock.github.inventory;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.network.CPacketSwitchGuis;
import com.glodblock.github.util.BlockPos;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class InventoryHandler implements IGuiHandler {

    public static void switchGui(GuiType guiType) {
        FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(guiType));
    }

    public static void openGui(EntityPlayer player, World world, BlockPos pos, EnumFacing face, GuiType guiType) {
        player.openGui(FluidCraft.INSTANCE,
            (guiType.ordinal() << 3) | face.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        int faceOrd = id & 0x7;
        if (faceOrd > EnumFacing.values().length) {
            return null;
        }
        EnumFacing face = EnumFacing.getFront(faceOrd);
        GuiType type = GuiType.getByOrdinal(id >>> 3);
        return type != null ? type.guiFactory.createServerGui(player, world, x, y, z, face) : null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        int faceOrd = id & 0x7;
        if (faceOrd > EnumFacing.values().length) {
            return null;
        }
        EnumFacing face = EnumFacing.getFront(faceOrd);
        GuiType type = GuiType.getByOrdinal(id >>> 3);
        return type != null ? type.guiFactory.createClientGui(player, world, x, y, z, face) : null;
    }
}
