package com.glodblock.github.network;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.ICraftingGrid;
import appeng.api.networking.crafting.ICraftingJob;
import appeng.api.networking.security.IActionHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerOpenContext;
import appeng.container.implementations.ContainerCraftAmount;
import appeng.container.implementations.ContainerCraftConfirm;
import appeng.container.interfaces.IInventorySlotAware;
import appeng.core.AELog;
import appeng.core.sync.GuiBridge;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFCCraftConfirm;
import com.glodblock.github.client.container.ContainerItemAmountChange;
import com.glodblock.github.common.item.fake.FakeItemRegister;
import com.glodblock.github.integration.mek.FCGasItems;
import com.glodblock.github.inventory.GuiType;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.Ae2Reflect;
import com.glodblock.github.util.ModAndClassUtil;
import io.netty.buffer.ByteBuf;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.concurrent.Future;

public class CPacketInventoryAction implements IMessage {

    private Action action;
    private int slot;
    private long id;
    private IAEItemStack stack;
    private boolean isEmpty;

    public CPacketInventoryAction() {
        //NO-OP
    }

    public CPacketInventoryAction( final Action action, final int slot, final int id, IAEItemStack stack ) {
        this.action = action;
        this.slot = slot;
        this.id = id;
        this.stack = stack;
        this.isEmpty = stack == null || stack.getDefinition().isEmpty();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(action.ordinal());
        buf.writeInt(slot);
        buf.writeLong(id);
        buf.writeBoolean(isEmpty);
        if (!isEmpty) {
            try {
                stack.writeToPacket(buf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        action = Action.values()[buf.readInt()];
        slot = buf.readInt();
        id = buf.readLong();
        isEmpty = buf.readBoolean();
        if (!isEmpty) {
            stack = AEItemStack.fromPacket(buf);
        }
    }

    public static class Handler implements IMessageHandler<CPacketInventoryAction, IMessage> {
        @Nullable
        @Override
        public IMessage onMessage(CPacketInventoryAction message, MessageContext ctx) {
            final EntityPlayerMP sender = ctx.getServerHandler().player;
            sender.getServerWorld().addScheduledTask(() -> {
                if( sender.openContainer instanceof AEBaseContainer) {
                    final AEBaseContainer baseContainer = (AEBaseContainer) sender.openContainer;
                    final ContainerOpenContext context = baseContainer.getOpenContext();
                    if (message.action == Action.CHANGE_AMOUNT)
                    {
                        if( context != null )
                        {
                            InventoryHandler.openGui(
                                    sender,
                                    Ae2Reflect.getContextWorld(context),
                                    new BlockPos(Ae2Reflect.getContextX(context), Ae2Reflect.getContextY(context), Ae2Reflect.getContextZ(context)),
                                    context.getSide().getFacing(),
                                    GuiType.ITEM_AMOUNT_SET );
                            int amt = (int) message.stack.getStackSize();
                            if (message.stack.getItem() == FCItems.FLUID_PACKET) {
                                FluidStack fluid = FakeItemRegister.getStack(message.stack);
                                amt = fluid == null ? 1 : fluid.amount;
                            } else if (ModAndClassUtil.GAS && message.stack.getItem() == FCGasItems.GAS_PACKET) {
                                GasStack gas = FakeItemRegister.getStack(message.stack);
                                amt = gas == null ? 1 : gas.amount;
                            }
                            FluidCraft.proxy.netHandler.sendTo(new SPacketSetItemAmount(amt), sender);
                            if( sender.openContainer instanceof ContainerItemAmountChange)
                            {
                                final ContainerItemAmountChange iac = (ContainerItemAmountChange) sender.openContainer;
                                if( message.stack != null )
                                {
                                    iac.getPatternValue().putStack( message.stack.getDefinition() );
                                    iac.setValueIndex( message.slot );
                                    iac.setInitValue( message.stack.getStackSize() );
                                }
                                iac.detectAndSendChanges();
                            }
                        }
                    }
                    if (message.action == Action.AUTO_CRAFT) {
                        if( context != null ) {
                            InventoryHandler.openGui(
                                    sender,
                                    Ae2Reflect.getContextWorld(context),
                                    new BlockPos(Ae2Reflect.getContextX(context), Ae2Reflect.getContextY(context), Ae2Reflect.getContextZ(context)),
                                    context.getSide().getFacing(),
                                    GuiType.FLUID_CRAFT_AMOUNT
                            );
                            if (sender.openContainer instanceof ContainerCraftAmount) {
                                final ContainerCraftAmount cca = (ContainerCraftAmount) sender.openContainer;
                                if (message.stack != null) {
                                    cca.getCraftingItem().putStack(message.stack.getDefinition());
                                    cca.setItemToCraft(message.stack);
                                }
                                cca.detectAndSendChanges();
                            }
                        }
                    }
                    if (message.action == Action.REQUEST_JOB) {
                        Object target = baseContainer.getTarget();
                        if (context != null && target instanceof IActionHost && baseContainer instanceof ContainerCraftAmount) {
                            final IActionHost ah = (IActionHost) target;
                            final IGridNode gn = ah.getActionableNode();
                            final IGrid g = gn.getGrid();
                            final ContainerCraftAmount cca = (ContainerCraftAmount) baseContainer;
                            if (cca.getItemToCraft() == null) {
                                return;
                            }
                            cca.getItemToCraft().setStackSize(message.id);
                            Future<ICraftingJob> futureJob = null;
                            try {
                                final ICraftingGrid cg = g.getCache(ICraftingGrid.class);
                                futureJob = cg.beginCraftingJob(cca.getWorld(), cca.getGrid(), cca.getActionSrc(), cca.getItemToCraft(), null);
                                InventoryHandler.openGui(
                                        sender,
                                        Ae2Reflect.getContextWorld(context),
                                        new BlockPos(Ae2Reflect.getContextX(context), Ae2Reflect.getContextY(context), Ae2Reflect.getContextZ(context)),
                                        context.getSide().getFacing(),
                                        GuiType.FLUID_CRAFT_CONFIRM
                                );
                                if (sender.openContainer instanceof ContainerFCCraftConfirm) {
                                    final ContainerFCCraftConfirm ccc = (ContainerFCCraftConfirm) sender.openContainer;
                                    ccc.setAutoStart(message.slot == 1);
                                    ccc.setJob(futureJob);
                                    cca.detectAndSendChanges();
                                }
                            } catch (final Throwable e) {
                                if (futureJob != null) {
                                    futureJob.cancel(true);
                                }
                                AELog.debug(e);
                            }
                        }
                    }
                }
            });
            return null;
        }
    }

    public enum Action {

        CHANGE_AMOUNT,
        AUTO_CRAFT,
        REQUEST_JOB

    }

}
