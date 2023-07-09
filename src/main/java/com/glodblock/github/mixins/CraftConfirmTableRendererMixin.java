package com.glodblock.github.mixins;

import appeng.client.gui.me.crafting.CraftConfirmTableRenderer;
import appeng.container.me.crafting.CraftingPlanSummaryEntry;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftConfirmTableRenderer.class)
public abstract class CraftConfirmTableRendererMixin {

    /**
     * @author GlodBlock
     * @reason Render drop as fluid
     */
    @Overwrite(
            remap = false
    )
    protected ItemStack getEntryItem(CraftingPlanSummaryEntry entry) {
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
            at = @At(value = "INVOKE", target = "Lappeng/container/me/crafting/CraftingPlanSummaryEntry;getItem()Lnet/minecraft/item/ItemStack;"),
            remap = false
    )
    private ItemStack changeItem(CraftingPlanSummaryEntry entry) {
        return getEntryItem(entry);
    }

}
