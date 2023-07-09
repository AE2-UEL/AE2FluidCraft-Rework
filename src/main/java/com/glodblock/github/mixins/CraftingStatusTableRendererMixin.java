package com.glodblock.github.mixins;

import appeng.client.gui.me.crafting.CraftingStatusTableRenderer;
import appeng.container.me.crafting.CraftingPlanSummaryEntry;
import appeng.container.me.crafting.CraftingStatusEntry;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingStatusTableRenderer.class)
public abstract class CraftingStatusTableRendererMixin {

    /**
     * @author GlodBlock
     * @reason Render drop as fluid
     */
    @Overwrite(
            remap = false
    )
    protected ItemStack getEntryItem(CraftingStatusEntry entry) {
        if (entry.getItem() != null && entry.getItem().getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(entry.getItem());
            if (!fluid.isEmpty()) {
                return ItemFluidPacket.newDisplayStack(fluid);
            }
        }
        return entry.getItem();
    }

    @Redirect(
            method = "getEntryTooltip*",
            at = @At(value = "INVOKE", target = "Lappeng/container/me/crafting/CraftingStatusEntry;getItem()Lnet/minecraft/item/ItemStack;", remap = false),
            remap = false
    )
    private ItemStack changeItem(CraftingStatusEntry entry) {
        return getEntryItem(entry);
    }

}
