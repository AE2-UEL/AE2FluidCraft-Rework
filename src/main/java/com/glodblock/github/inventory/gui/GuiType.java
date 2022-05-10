package com.glodblock.github.inventory.gui;

import appeng.api.storage.ITerminalHost;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftingStatus;
import com.glodblock.github.client.gui.*;
import com.glodblock.github.client.gui.container.ContainerFluidCraftConfirm;
import com.glodblock.github.client.gui.container.ContainerFluidPacketDecoder;
import com.glodblock.github.client.gui.container.ContainerFluidPatternEncoder;
import com.glodblock.github.client.gui.container.ContainerFluidPatternTerminal;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nullable;
import java.util.List;

public enum GuiType {

    FLUID_PAT_TERM_CRAFTING_STATUS(new PartGuiFactory<PartFluidPatternTerminal>(PartFluidPatternTerminal.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartFluidPatternTerminal inv) {
            return new ContainerCraftingStatus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartFluidPatternTerminal inv) {
            return new GuiFluidPatternTerminalCraftingStatus(player.inventory, inv);
        }
    }),

    FLUID_PATTERN_TERMINAL(new PartGuiFactory<ITerminalHost>(ITerminalHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerFluidPatternTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFluidPatternTerminal(player.inventory, inv);
        }
    }),

    FLUID_PATTERN_ENCODER(new TileGuiFactory<TileFluidPatternEncoder>(TileFluidPatternEncoder.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidPatternEncoder inv) {
            return new ContainerFluidPatternEncoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidPatternEncoder inv) {
            return new GuiFluidPatternEncoder(player.inventory, inv);
        }
    }),

    FLUID_CRAFTING_CONFIRM(new PartGuiFactory<PartFluidPatternTerminal>(PartFluidPatternTerminal.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartFluidPatternTerminal inv) {
            return new ContainerFluidCraftConfirm(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartFluidPatternTerminal inv) {
            return new GuiFluidCraftConfirm(player.inventory, inv);
        }
    }),

    FLUID_CRAFTING_AMOUNT(new PartGuiFactory<PartFluidPatternTerminal>(PartFluidPatternTerminal.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartFluidPatternTerminal inv) {
            return new ContainerCraftAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartFluidPatternTerminal inv) {
            return new GuiFluidCraftAmount(player.inventory, inv);
        }
    }),

    FLUID_PACKET_DECODER(new TileGuiFactory<TileFluidPacketDecoder>(TileFluidPacketDecoder.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidPacketDecoder inv) {
            return new ContainerFluidPacketDecoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidPacketDecoder inv) {
            return new GuiFluidPacketDecoder(player.inventory, inv);
        }
    });

    public static final List<GuiType> VALUES = ImmutableList.copyOf(values());

    @Nullable
    public static GuiType getByOrdinal(int ordinal) {
        return ordinal < 0 || ordinal >= VALUES.size() ? null : VALUES.get(ordinal);
    }

    public final GuiFactory guiFactory;

    GuiType(GuiFactory guiFactory) {
        this.guiFactory = guiFactory;
    }

}
