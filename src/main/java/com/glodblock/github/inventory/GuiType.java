package com.glodblock.github.inventory;

import appeng.api.AEApi;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.storage.ITerminalHost;
import appeng.api.util.AEPartLocation;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftingStatus;
import appeng.container.implementations.ContainerPriority;
import appeng.fluids.helper.IFluidInterfaceHost;
import appeng.helpers.IInterfaceHost;
import appeng.helpers.WirelessTerminalGuiObject;
import com.glodblock.github.client.GuiBurette;
import com.glodblock.github.client.GuiExtendedFluidPatternTerminal;
import com.glodblock.github.client.GuiFCCraftAmount;
import com.glodblock.github.client.GuiFCCraftConfirm;
import com.glodblock.github.client.GuiFCPriority;
import com.glodblock.github.client.GuiFluidAssembler;
import com.glodblock.github.client.GuiFluidDualInterface;
import com.glodblock.github.client.GuiFluidExportBus;
import com.glodblock.github.client.GuiFluidLevelMaintainer;
import com.glodblock.github.client.GuiFluidPacketDecoder;
import com.glodblock.github.client.GuiFluidPatternEncoder;
import com.glodblock.github.client.GuiFluidPatternTerminal;
import com.glodblock.github.client.GuiFluidPatternTerminalCraftingStatus;
import com.glodblock.github.client.GuiIngredientBuffer;
import com.glodblock.github.client.GuiItemAmountChange;
import com.glodblock.github.client.GuiItemDualInterface;
import com.glodblock.github.client.GuiLargeIngredientBuffer;
import com.glodblock.github.client.GuiUltimateEncoder;
import com.glodblock.github.client.GuiWirelessFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerBurette;
import com.glodblock.github.client.container.ContainerExtendedFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerFCCraftConfirm;
import com.glodblock.github.client.container.ContainerFluidAssembler;
import com.glodblock.github.client.container.ContainerFluidDualInterface;
import com.glodblock.github.client.container.ContainerFluidExportBus;
import com.glodblock.github.client.container.ContainerFluidLevelMaintainer;
import com.glodblock.github.client.container.ContainerFluidPacketDecoder;
import com.glodblock.github.client.container.ContainerFluidPatternEncoder;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.client.container.ContainerIngredientBuffer;
import com.glodblock.github.client.container.ContainerItemAmountChange;
import com.glodblock.github.client.container.ContainerItemDualInterface;
import com.glodblock.github.client.container.ContainerLargeIngredientBuffer;
import com.glodblock.github.client.container.ContainerUltimateEncoder;
import com.glodblock.github.client.container.ContainerWirelessFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidExportBus;
import com.glodblock.github.common.tile.TileBurette;
import com.glodblock.github.common.tile.TileFluidAssembler;
import com.glodblock.github.common.tile.TileFluidLevelMaintainer;
import com.glodblock.github.common.tile.TileFluidPacketDecoder;
import com.glodblock.github.common.tile.TileFluidPatternEncoder;
import com.glodblock.github.common.tile.TileIngredientBuffer;
import com.glodblock.github.common.tile.TileLargeIngredientBuffer;
import com.glodblock.github.common.tile.TileUltimateEncoder;
import com.glodblock.github.interfaces.FCPriorityHost;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public enum GuiType {

    ULTIMATE_ENCODER(new TileGuiFactory<TileUltimateEncoder>(TileUltimateEncoder.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileUltimateEncoder inv) {
            return new ContainerUltimateEncoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileUltimateEncoder inv) {
            return new GuiUltimateEncoder(player.inventory, inv);
        }
    }),

    FLUID_ASSEMBLER(new TileGuiFactory<TileFluidAssembler>(TileFluidAssembler.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidAssembler inv) {
            return new ContainerFluidAssembler(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidAssembler inv) {
            return new GuiFluidAssembler(player.inventory, inv);
        }
    }),

    ITEM_AMOUNT_SET(new AllGuiFactory<ITerminalHost>(ITerminalHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerItemAmountChange(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiItemAmountChange(player.inventory, inv);
        }
    }),

    FLUID_LEVEL_MAINTAINER(new PartOrTileGuiFactory<TileFluidLevelMaintainer>(TileFluidLevelMaintainer.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidLevelMaintainer inv) {
            return new ContainerFluidLevelMaintainer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidLevelMaintainer inv) {
            return new GuiFluidLevelMaintainer(player.inventory, inv);
        }
    }),

    FLUID_EXPORT_BUS(new PartOrTileGuiFactory<PartFluidExportBus>(PartFluidExportBus.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, PartFluidExportBus inv) {
            return new ContainerFluidExportBus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, PartFluidExportBus inv) {
            return new GuiFluidExportBus(player.inventory, inv);
        }
    }),

    INGREDIENT_BUFFER(new TileGuiFactory<TileIngredientBuffer>(TileIngredientBuffer.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileIngredientBuffer inv) {
            return new ContainerIngredientBuffer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileIngredientBuffer inv) {
            return new GuiIngredientBuffer(player.inventory, inv);
        }
    }),

    LARGE_INGREDIENT_BUFFER(new TileGuiFactory<TileLargeIngredientBuffer>(TileLargeIngredientBuffer.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileLargeIngredientBuffer inv) {
            return new ContainerLargeIngredientBuffer(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileLargeIngredientBuffer inv) {
            return new GuiLargeIngredientBuffer(player.inventory, inv);
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

    FLUID_PACKET_DECODER(new TileGuiFactory<TileFluidPacketDecoder>(TileFluidPacketDecoder.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileFluidPacketDecoder inv) {
            return new ContainerFluidPacketDecoder(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileFluidPacketDecoder inv) {
            return new GuiFluidPacketDecoder(player.inventory, inv);
        }
    }),

    PRECISION_BURETTE(new TileGuiFactory<TileBurette>(TileBurette.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, TileBurette inv) {
            return new ContainerBurette(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, TileBurette inv) {
            return new GuiBurette(player.inventory, inv);
        }
    }),

    DUAL_ITEM_INTERFACE(new PartOrTileGuiFactory<IInterfaceHost>(IInterfaceHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, IInterfaceHost inv) {
            return new ContainerItemDualInterface(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IInterfaceHost inv) {
            return new GuiItemDualInterface(player.inventory, inv);
        }
    }),

    DUAL_FLUID_INTERFACE(new PartOrTileGuiFactory<IFluidInterfaceHost>(IFluidInterfaceHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, IFluidInterfaceHost inv) {
            return new ContainerFluidDualInterface(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, IFluidInterfaceHost inv) {
            return new GuiFluidDualInterface(player.inventory, inv);
        }
    }),

    FLUID_PAT_TERM_CRAFTING_STATUS(new ItemOrPartGuiFactory<ITerminalHost>(ITerminalHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftingStatus(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
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

    FLUID_EXTENDED_PATTERN_TERMINAL(new PartGuiFactory<ITerminalHost>(ITerminalHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerExtendedFluidPatternTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiExtendedFluidPatternTerminal(player.inventory, inv);
        }
    }),

    WIRELESS_FLUID_PATTERN_TERMINAL(new ItemGuiFactory<WirelessTerminalGuiObject>(WirelessTerminalGuiObject.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, WirelessTerminalGuiObject inv) {
            return new ContainerWirelessFluidPatternTerminal(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, WirelessTerminalGuiObject inv) {
            return new GuiWirelessFluidPatternTerminal(player.inventory, inv);
        }
    }),

    FLUID_CRAFT_AMOUNT(new ItemOrPartGuiFactory<ITerminalHost>(ITerminalHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerCraftAmount(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFCCraftAmount(player.inventory, inv);
        }
    }),

    FLUID_CRAFT_CONFIRM(new ItemOrPartGuiFactory<ITerminalHost>(ITerminalHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, ITerminalHost inv) {
            return new ContainerFCCraftConfirm(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, ITerminalHost inv) {
            return new GuiFCCraftConfirm(player.inventory, inv);
        }
    }),

    PRIORITY(new PartOrTileGuiFactory<FCPriorityHost>(FCPriorityHost.class) {
        @Override
        protected Object createServerGui(EntityPlayer player, FCPriorityHost inv) {
            return new ContainerPriority(player.inventory, inv);
        }

        @Override
        protected Object createClientGui(EntityPlayer player, FCPriorityHost inv) {
            return new GuiFCPriority(player.inventory, inv);
        }
    });

    public static final List<GuiType> VALUES = ImmutableList.copyOf(values());

    @Nullable
    public static GuiType getByOrdinal(int ordinal) {
        return ordinal < 0 || ordinal >= VALUES.size() ? null : VALUES.get(ordinal);
    }

    final GuiFactory guiFactory;

    GuiType(GuiFactory guiFactory) {
        this.guiFactory = guiFactory;
    }

    public interface GuiFactory {

        @Nullable
        Object createServerGui(EntityPlayer player, World world, int x, int y, int z, EnumFacing face);

        @SideOnly(Side.CLIENT)
        @Nullable
        Object createClientGui(EntityPlayer player, World world, int x, int y, int z, EnumFacing face);

    }

    private static abstract class TileGuiFactory<T> implements GuiFactory {

        protected final Class<T> invClass;

        TileGuiFactory(Class<T> invClass) {
            this.invClass = invClass;
        }

        @Nullable
        protected T getInventory(@Nullable TileEntity tile, EntityPlayer player, EnumFacing face, BlockPos pos) {
            return invClass.isInstance(tile) ? invClass.cast(tile) : null;
        }

        @Nullable
        @Override
        public Object createServerGui(EntityPlayer player, World world, int x, int y, int z, EnumFacing face) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            T inv = getInventory(tile, player, face, new BlockPos(x, y, z));
            if (inv == null) {
                return null;
            }
            Object gui = createServerGui(player, inv);
            if (gui instanceof AEBaseContainer) {
                ContainerOpenContext ctx = new ContainerOpenContext(inv);
                ctx.setWorld(world);
                ctx.setX(x);
                ctx.setY(y);
                ctx.setZ(z);
                ctx.setSide(AEPartLocation.fromFacing(face));
                ((AEBaseContainer)gui).setOpenContext(ctx);
            }
            return gui;
        }

        @Nullable
        protected abstract Object createServerGui(EntityPlayer player, T inv);

        @Nullable
        @Override
        public Object createClientGui(EntityPlayer player, World world, int x, int y, int z, EnumFacing face) {
            TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
            T inv = getInventory(tile, player, face, new BlockPos(x, y, z));
            if (inv == null) {
                return null;
            }
            return createClientGui(player, inv);
        }

        @Nullable
        protected abstract Object createClientGui(EntityPlayer player, T inv);

    }

    private static abstract class PartOrTileGuiFactory<T> extends TileGuiFactory<T> {

        PartOrTileGuiFactory(Class<T> invClass) {
            super(invClass);
        }

        @Nullable
        @Override
        protected T getInventory(TileEntity tile, EntityPlayer player, EnumFacing face, BlockPos pos) {
            if (pos.getZ() != Integer.MIN_VALUE && tile instanceof IPartHost) {
                IPart part = ((IPartHost)tile).getPart(face);
                if (invClass.isInstance(part)) {
                    return invClass.cast(part);
                }
            }
            return super.getInventory(tile, player, face, pos);
        }

    }

    private static abstract class PartGuiFactory<T> extends TileGuiFactory<T> {

        PartGuiFactory(Class<T> invClass) {
            super(invClass);
        }

        @Nullable
        @Override
        protected T getInventory(TileEntity tile, EntityPlayer player, EnumFacing face, BlockPos pos) {
            if (pos.getZ() != Integer.MIN_VALUE && tile instanceof IPartHost) {
                IPart part = ((IPartHost)tile).getPart(face);
                if (invClass.isInstance(part)) {
                    return invClass.cast(part);
                }
            }
            return null;
        }

    }

    private static abstract class ItemGuiFactory<T> extends TileGuiFactory<T> {

        ItemGuiFactory(Class<T> invClass) {
            super(invClass);
        }

        @Nullable
        @Override
        protected T getInventory(TileEntity tile, EntityPlayer player, EnumFacing face, BlockPos pos) {
            ItemStack hold = player.getHeldItem(EnumHand.values()[face.ordinal() % 2]);
            if (pos.getZ() == Integer.MIN_VALUE && !hold.isEmpty()) {
                Object holder = getItemGuiObject(hold, player, player.world, pos.getX(), pos.getY(), pos.getZ());
                if (invClass.isInstance(holder)) {
                    return invClass.cast(holder);
                }
            }
            return null;
        }

    }

    private static abstract class ItemOrPartGuiFactory<T> extends PartGuiFactory<T> {

        ItemOrPartGuiFactory(Class<T> invClass) {
            super(invClass);
        }

        @Nullable
        @Override
        protected T getInventory(TileEntity tile, EntityPlayer player, EnumFacing face, BlockPos pos) {
            ItemStack hold = player.getHeldItem(EnumHand.values()[face.ordinal() % 2]);
            if (pos.getZ() == Integer.MIN_VALUE && !hold.isEmpty()) {
                Object holder = getItemGuiObject(hold, player, player.world, pos.getX(), pos.getY(), pos.getZ());
                if (invClass.isInstance(holder)) {
                    return invClass.cast(holder);
                }
            }
            return super.getInventory(tile, player, face, pos);
        }

    }

    private static abstract class AllGuiFactory<T> extends PartOrTileGuiFactory<T> {

        AllGuiFactory(Class<T> invClass) {
            super(invClass);
        }

        @Nullable
        @Override
        protected T getInventory(TileEntity tile, EntityPlayer player, EnumFacing face, BlockPos pos) {
            ItemStack hold = player.getHeldItem(EnumHand.values()[face.ordinal() % 2]);
            if (pos.getZ() == Integer.MIN_VALUE && !hold.isEmpty()) {
                Object holder = getItemGuiObject(hold, player, player.world, pos.getX(), pos.getY(), pos.getZ());
                if (invClass.isInstance(holder)) {
                    return invClass.cast(holder);
                }
            }
            return super.getInventory(tile, player, face, pos);
        }

    }

    private static Object getItemGuiObject(ItemStack it, EntityPlayer player, World w, int x, int y, int z) {
        if (!it.isEmpty()) {
            if (it.getItem() instanceof IGuiItem) {
                return ((IGuiItem)it.getItem()).getGuiObject(it, w, new BlockPos(x, y, z));
            }
            IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return new WirelessTerminalGuiObject(wh, it, player, w, x, y, z);
            }
        }
        return null;
    }

}
