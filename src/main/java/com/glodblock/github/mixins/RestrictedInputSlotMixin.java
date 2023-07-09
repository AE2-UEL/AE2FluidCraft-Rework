package com.glodblock.github.mixins;

import appeng.container.slot.RestrictedInputSlot;
import appeng.items.misc.EncodedPatternItem;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RestrictedInputSlot.class)
public abstract class RestrictedInputSlotMixin {

    @Redirect(
            method = "getDisplayStack",
            at = @At(value = "INVOKE", target = "Lappeng/items/misc/EncodedPatternItem;getOutput(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;"),
            remap = false
    )
    private ItemStack renderDropAsFluid(EncodedPatternItem pattern, ItemStack item) {
        ItemStack output = pattern.getOutput(item);
        if (output != null && output.getItem() instanceof ItemFluidDrop) {
            FluidStack fluid = ItemFluidDrop.getFluidStack(output);
            if (!fluid.isEmpty()) {
                return ItemFluidPacket.newDisplayStack(fluid);
            }
        }
        return output;
    }

}
