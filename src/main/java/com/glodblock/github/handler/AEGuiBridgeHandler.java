package com.glodblock.github.handler;

import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.sync.GuiWrapper;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class AEGuiBridgeHandler implements GuiWrapper.IExternalGui {
    public static final AEGuiBridgeHandler WIRELESS_FLUID_PATTERN_TERMINAL = new AEGuiBridgeHandler(GuiType.WIRELESS_FLUID_PATTERN_TERMINAL, "wireless_fluid_pattern_terminal");
    public static final AEGuiBridgeHandler FLUID_PATTERN_TERMINAL = new AEGuiBridgeHandler(GuiType.FLUID_PATTERN_TERMINAL, "fluid_pattern_terminal");
    public static final AEGuiBridgeHandler FLUID_EXTENDED_PATTERN_TERMINAL = new AEGuiBridgeHandler(GuiType.FLUID_EXTENDED_PATTERN_TERMINAL, "fluid_extended_pattern_terminal");

    private final ResourceLocation id;
    private final GuiType guiType;

    public AEGuiBridgeHandler(GuiType type, String key) {
        id = FluidCraft.resource(key);
        guiType = type;
        GuiWrapper.INSTANCE.registerExternalGuiHandler(this.id, this::openGui);
    }

    @Override
    public ResourceLocation getID() {
        return id;
    }

    private void openGui(GuiWrapper.IExternalGui gui, GuiWrapper.GuiContext ctx) {
        if (gui instanceof AEGuiBridgeHandler) {
            GuiType type = ((AEGuiBridgeHandler) gui).guiType;
            if (ctx.pos != null && ctx.facing != null) {
                InventoryHandler.openGui(ctx.player, ctx.world, ctx.pos, ctx.facing, type);
            } else if (ctx.extra != null) {
                int slot = ctx.extra.getInteger("slot");
                boolean isBauble = ctx.extra.getBoolean("isBauble");
                InventoryHandler.openGui(ctx.player, ctx.world, new BlockPos(slot, isBauble ? 1 : 0, Integer.MIN_VALUE), EnumFacing.DOWN, type);
            } else {
                if (ctx.player.openContainer instanceof IInventorySlotAware) {
                    IInventorySlotAware c = (IInventorySlotAware) ctx.player.openContainer;
                    InventoryHandler.openGui(ctx.player, ctx.world, new BlockPos(c.getInventorySlot(), c.isBaubleSlot() ? 1 : 0, Integer.MIN_VALUE), EnumFacing.DOWN, type);
                } else {
                    InventoryHandler.openGui(ctx.player, ctx.world, new BlockPos(ctx.player.inventory.currentItem, 0, Integer.MIN_VALUE), EnumFacing.DOWN, type);
                }
            }
        }
    }
}
