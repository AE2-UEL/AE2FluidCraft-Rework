package com.glodblock.github.integration.pauto;

import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.loader.FCItems;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import thelm.packagedauto.client.gui.GuiEncoder;

import java.util.List;

public class RecipeEncoderFluidTooltipHandler {

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onItemTooltip(ItemTooltipEvent event) {
        if (Minecraft.getMinecraft().currentScreen instanceof GuiEncoder) {
            ItemStack stack = event.getItemStack();
            if (stack.getItem() == FCItems.FLUID_PACKET) {
                FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
                if (fluid != null) {
                    List<String> tooltip = event.getToolTip();
                    tooltip.clear();
                    tooltip.add(fluid.getLocalizedName());
                    tooltip.add(String.format(TextFormatting.GRAY + "%,d mB", fluid.amount));
                }
            }
        }
    }

}
