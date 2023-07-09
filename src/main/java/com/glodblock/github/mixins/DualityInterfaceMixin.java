package com.glodblock.github.mixins;

import appeng.helpers.DualityInterface;
import appeng.util.InventoryAdaptor;
import com.glodblock.github.coreutil.ExtendedInterface;
import com.glodblock.github.inventory.FluidConvertingInventoryAdaptor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DualityInterface.class)
public abstract class DualityInterfaceMixin implements ExtendedInterface {

    private boolean fluidPacket;
    private boolean allowSplitting;
    private int blockModeEx;

    @Inject(
            method = "writeToNBT",
            at = @At("HEAD"),
            remap = false
    )
    public void writeToNBT(CompoundNBT data, CallbackInfo ci) {
        data.putBoolean("fluidPacket", fluidPacket);
        data.putBoolean("allowSplitting", allowSplitting);
        data.putInt("blockModeEx", blockModeEx);
    }

    @Inject(
            method = "readFromNBT",
            at = @At("HEAD"),
            remap = false
    )
    public void readFromNBT(CompoundNBT data, CallbackInfo ci) {
        fluidPacket = data.getBoolean("fluidPacket");
        allowSplitting = data.getBoolean("allowSplitting");
        blockModeEx = data.getInt("blockModeEx");;
    }

    @Redirect(
            method = {"pushItemsOut", "pushPattern", "isBusy"},
            at = @At(value = "INVOKE", target = "Lappeng/util/InventoryAdaptor;getAdaptor(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/Direction;)Lappeng/util/InventoryAdaptor;", remap = false),
            remap = false
    )
    private InventoryAdaptor wrapInventoryAdaptor(TileEntity cap, Direction te) {
        return cap != null ? FluidConvertingInventoryAdaptor.wrap(cap, te) : null;
    }

    @Override
    public boolean getFluidPacketMode() {
        return fluidPacket;
    }

    @Override
    public void setFluidPacketMode(boolean value) {
        this.fluidPacket = value;
    }

    @Override
    public boolean getSplittingMode() {
        return allowSplitting;
    }

    @Override
    public void setSplittingMode(boolean value) {
        this.allowSplitting = value;
    }

    @Override
    public int getExtendedBlockMode() {
        return blockModeEx;
    }

    @Override
    public void setExtendedBlockMode(int value) {
        this.blockModeEx = value;
    }

}
