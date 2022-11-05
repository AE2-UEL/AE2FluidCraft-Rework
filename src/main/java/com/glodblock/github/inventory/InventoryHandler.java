package com.glodblock.github.inventory;

import appeng.core.AELog;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.network.CPacketSwitchGuis;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ConcurrentModificationException;

public class InventoryHandler implements IGuiHandler {

    public static void switchGui(GuiType guiType) {
        FluidCraft.proxy.netHandler.sendToServer(new CPacketSwitchGuis(guiType));
    }

    public static void openGui(EntityPlayer player, World world, BlockPos pos, EnumFacing face, GuiType guiType) {
        try {
            player.openGui(FluidCraft.INSTANCE,
                    (guiType.ordinal() << 3) | face.ordinal(), world, pos.getX(), pos.getY(), pos.getZ());
        } catch (ConcurrentModificationException e) {
            AELog.warn("catch CME when trying to open %s.", guiType);
        }
    }

    @Nullable
    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        int faceOrd = id & 0x7;
        if (faceOrd > EnumFacing.VALUES.length) {
            return null;
        }
        EnumFacing face = EnumFacing.VALUES[faceOrd];
        GuiType type = GuiType.getByOrdinal(id >>> 3);
        return type != null ? type.guiFactory.createServerGui(player, world, x, y, z, face) : null;
    }

    @SideOnly(Side.CLIENT)
    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        int faceOrd = id & 0x7;
        if (faceOrd > EnumFacing.VALUES.length) {
            return null;
        }
        EnumFacing face = EnumFacing.VALUES[faceOrd];
        GuiType type = GuiType.getByOrdinal(id >>> 3);
        return type != null ? type.guiFactory.createClientGui(player, world, x, y, z, face) : null;
    }

}