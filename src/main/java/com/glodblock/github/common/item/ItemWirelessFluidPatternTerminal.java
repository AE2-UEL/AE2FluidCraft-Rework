package com.glodblock.github.common.item;

import appeng.items.tools.powered.ToolWirelessTerminal;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class ItemWirelessFluidPatternTerminal extends ToolWirelessTerminal {

    @Override
    public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer player, EnumHand hand) {
        Util.openWirelessTerminal(player.getHeldItem(hand), hand, w, player, GuiType.WIRELESS_FLUID_PATTERN_TERMINAL);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public boolean canHandle(ItemStack is) {
        return is.getItem() == FCItems.WIRELESS_FLUID_PATTERN_TERMINAL;
    }

}
