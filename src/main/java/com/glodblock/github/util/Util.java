package com.glodblock.github.util;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.storage.data.IAEFluidStack;
import appeng.util.item.AEFluidStack;
import com.glodblock.github.common.item.ItemFluidPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public final class Util {

    public static EnumFacing from(ForgeDirection direction) {
        switch (direction) {
            case WEST: return EnumFacing.EAST;
            case EAST: return EnumFacing.WEST;
            case UNKNOWN: return null;
            default: {
                int o = direction.ordinal();
                return EnumFacing.values()[o];
            }
        }
    }

    public static ForgeDirection from(EnumFacing direction) {
        switch (direction) {
            case WEST: return ForgeDirection.EAST;
            case EAST: return ForgeDirection.WEST;
            default: {
                int o = direction.ordinal();
                return ForgeDirection.values()[o];
            }
        }
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGrid grid) {
        return grid == null || hasPermission(player, permission, (ISecurityGrid) grid.getCache(ISecurityGrid.class));
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGridHost host) {
        return hasPermission(player, permission, host, ForgeDirection.UNKNOWN);
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGridHost host, ForgeDirection side) {
        return host == null || hasPermission(player, permission, host.getGridNode(side));
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IGridNode host) {
        return host == null || hasPermission(player, permission, host.getGrid());
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, IPart part) {
        return part == null || hasPermission(player, permission, part.getGridNode());
    }

    public static boolean hasPermission(EntityPlayer player, SecurityPermissions permission, ISecurityGrid securityGrid) {
        return player == null || permission == null || securityGrid == null || securityGrid.hasPermission(player, permission);
    }

    public static ItemStack copyStackWithSize(ItemStack itemStack, int size)
    {
        if (size == 0 || itemStack == null)
            return null;
        ItemStack copy = itemStack.copy();
        copy.stackSize = size;
        return copy;
    }

    public static FluidStack getFluidFromItem(ItemStack stack) {
        if (stack != null && (stack.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(stack))) {
            if (stack.getItem() instanceof IFluidContainerItem) {
                FluidStack fluid = ((IFluidContainerItem) stack.getItem()).getFluid(stack);
                if (fluid != null) {
                    FluidStack fluid0 = fluid.copy();
                    fluid0.amount *= stack.stackSize;
                    return fluid0;
                }
            }
            if (FluidContainerRegistry.isContainer(stack)) {
                FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
                if (fluid != null) {
                    FluidStack fluid0 = fluid.copy();
                    fluid0.amount *= stack.stackSize;
                    return fluid0;
                }
            }
        }
        return null;
    }

    public static boolean isFluidPacket(ItemStack stack) {
        return stack != null && stack.getItem() instanceof ItemFluidPacket;
    }

    public static FluidStack cloneFluidStack(FluidStack fluidStack) {
        if (fluidStack != null) return fluidStack.copy();
        return null;
    }

    public static IAEFluidStack loadFluidStackFromNBT( final NBTTagCompound i )
    {
        //Fuck ae2
        final FluidStack t = FluidRegistry.getFluidStack(i.getString("FluidName"), 1);
        final AEFluidStack fluid = AEFluidStack.create( t );
        fluid.setStackSize( i.getLong( "Cnt" ) );
        fluid.setCountRequestable( i.getLong( "Req" ) );
        fluid.setCraftable( i.getBoolean( "Craft" ) );
        return fluid;
    }

    public static boolean areFluidsEqual(FluidStack fluid1, FluidStack fluid2) {
        if (fluid1 == null || fluid2 == null) return false;
        return fluid1.isFluidEqual(fluid2);
    }

}
