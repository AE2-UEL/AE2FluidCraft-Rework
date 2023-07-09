package com.glodblock.github.mixins;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.crafting.MECraftingInventory;
import appeng.me.cluster.implementations.CraftingCPUCluster;
import appeng.me.helpers.MachineSource;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.FCItems;
import com.google.common.base.Preconditions;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingCPUCluster.class)
public abstract class CraftingCpuMixin {

    @Shadow(remap = false)
    private boolean isComplete;
    @Shadow(remap = false)
    private MECraftingInventory inventory;
    @Shadow(remap = false)
    private MachineSource machineSrc;
    @Shadow(remap = false)
    protected abstract IGrid getGrid();
    @Shadow(remap = false)
    protected abstract void postChange(IAEItemStack diff, IActionSource src);
    @Shadow(remap = false)
    protected abstract void markDirty();

    @Redirect(
            method = "executeCrafting",
            at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEItemStack;getStackSize()J", ordinal = 0, remap = false),
            remap = false
    )
    private long getFluidStackSize(IAEItemStack packet) {
        if (packet.getDefinition() != null && !packet.getDefinition().isEmpty() && packet.getDefinition().getItem() instanceof ItemFluidDrop) {
            return (long) Math.max(packet.getStackSize() / 1000D, 1);
        } else return packet.getStackSize();
    }

    @Redirect(
            method = "executeCrafting",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/CraftingInventory;getStackInSlot(I)Lnet/minecraft/item/ItemStack;"),
            remap = false
    )
    private ItemStack removeFluidPackets(CraftingInventory inv, int index) {
        ItemStack stack = inv.getStackInSlot(index);
        if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemFluidPacket) {
            FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
            return ItemFluidDrop.newStack(fluid);
        }
        else {
            return stack;
        }
    }

    @Redirect(
            method = "executeCrafting",
            at = @At(value = "INVOKE", target = "Lappeng/util/item/AEItemStack;fromItemStack(Lnet/minecraft/item/ItemStack;)Lappeng/util/item/AEItemStack;", ordinal = 0, remap = false),
            remap = false
    )
    private AEItemStack wrapFluidPacketStack(ItemStack stack) {
        if (stack.getItem() == FCItems.FLUID_PACKET) {
            IAEItemStack dropStack = ItemFluidDrop.newAeStack(ItemFluidPacket.getFluidStack(stack));
            if (dropStack != null) {
                return (AEItemStack) dropStack;
            }
        }
        return AEItemStack.fromItemStack(stack);
    }

    /**
     * @author GlodBlock
     * @reason Fix fluid storage
     */
    @Overwrite(
            remap = false
    )
    private void storeItems() {
        Preconditions.checkState(this.isComplete, "CPU should be complete to prevent re-insertion when dumping items");
        final IGrid g = this.getGrid();

        if (g == null) {
            return;
        }

        final IStorageGrid sg = g.getCache( IStorageGrid.class );
        final IMEInventory<IAEItemStack> ii = sg.getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
        final IMEInventory<IAEFluidStack> jj = sg.getInventory(Api.instance().storage().getStorageChannel(IFluidStorageChannel.class));

        for (IAEItemStack is : this.inventory.getItemList()) {
            this.postChange(is, this.machineSrc);

            if (is.getItem() instanceof ItemFluidDrop ) {
                IAEFluidStack drop = ItemFluidDrop.getAeFluidStack(is);
                IAEFluidStack fluidRemainder = jj.injectItems(drop, Actionable.MODULATE, this.machineSrc);
                if (fluidRemainder != null) {
                    is.setStackSize(fluidRemainder.getStackSize());
                } else {
                    is.reset();
                }
            } else {
                IAEItemStack remainder = ii.injectItems(is.copy(), Actionable.MODULATE, this.machineSrc);
                if (remainder != null) {
                    is.setStackSize(remainder.getStackSize());
                } else {
                    is.reset();
                }
            }
        }

        if (this.inventory.getItemList().isEmpty()) {
            this.inventory = new MECraftingInventory();
        }

        this.markDirty();
    }


}
