package com.glodblock.github.mixins;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.container.AEBaseContainer;
import appeng.container.implementations.InterfaceTerminalContainer;
import appeng.core.sync.BasePacket;
import appeng.helpers.DualityInterface;
import appeng.helpers.IInterfaceHost;
import appeng.parts.misc.InterfacePart;
import appeng.tile.misc.InterfaceTileEntity;
import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.common.tile.TileDualInterface;
import com.glodblock.github.util.Ae2Reflect;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(InterfaceTerminalContainer.class)
public abstract class InterfaceTerminalContainerMixin extends AEBaseContainer {

    public InterfaceTerminalContainerMixin(ContainerType<?> containerType, int id, PlayerInventory playerInventory, Object host) {
        super(containerType, id, playerInventory, host);
    }

    @Shadow(remap = false)
    protected abstract IGrid getGrid();
    @Shadow(remap = false)
    protected abstract void sendIncrementalUpdate(Consumer<BasePacket> packetSender);
    @Shadow(remap = false)
    protected abstract void sendFullUpdate(@Nullable IGrid grid, Consumer<BasePacket> packetSender);
    @Final
    @Shadow(remap = false)
    @SuppressWarnings("rawtypes")
    private Map diList;


    /**
     * @author GlodBlock
     * @reason Add dual interface to interface terminal
     */
    @Overwrite
    public void detectAndSendChanges() {
        if (!this.isClient()) {
            super.detectAndSendChanges();
            IGrid grid = this.getGrid();
            Object state = Ae2Reflect.genVisitorState();
            if (grid != null) {
                Ae2Reflect.visitInterfaceHost((InterfaceTerminalContainer)(Object) this, grid, InterfaceTileEntity.class, state);
                Ae2Reflect.visitInterfaceHost((InterfaceTerminalContainer)(Object) this, grid, InterfacePart.class, state);
                Ae2Reflect.visitInterfaceHost((InterfaceTerminalContainer)(Object) this, grid, TileDualInterface.class, state);
                Ae2Reflect.visitInterfaceHost((InterfaceTerminalContainer)(Object) this, grid, PartDualInterface.class, state);
            }
            if (Ae2Reflect.getVisitorStateTotal(state) == this.diList.size() && !Ae2Reflect.getVisitorStateUpdate(state)) {
                this.sendIncrementalUpdate(this::sendPacketToClient);
            } else {
                this.sendFullUpdate(grid, this::sendPacketToClient);
            }
        }
    }

    @Inject(
            method = "sendFullUpdate",
            at = @At(value = "INVOKE", target = "Lappeng/api/networking/IGrid;getMachines(Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;", ordinal = 0),
            remap = false
    )
    @SuppressWarnings("unchecked")
    private void sendExtendUpdateInfo(IGrid grid, Consumer<BasePacket> packetSender, CallbackInfo ci) {
        for (final IGridNode gn : grid.getMachines(TileDualInterface.class)) {
            final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
            final DualityInterface dual = ih.getInterfaceDuality();
            if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                this.diList.put(ih, Ae2Reflect.genInvTracker(dual, dual.getPatterns(), dual.getTermName()));
            }
        }
        for (final IGridNode gn : grid.getMachines(PartDualInterface.class)) {
            final IInterfaceHost ih = (IInterfaceHost) gn.getMachine();
            final DualityInterface dual = ih.getInterfaceDuality();
            if (gn.isActive() && dual.getConfigManager().getSetting(Settings.INTERFACE_TERMINAL) == YesNo.YES) {
                this.diList.put(ih, Ae2Reflect.genInvTracker(dual, dual.getPatterns(), dual.getTermName()));
            }
        }
    }

}
