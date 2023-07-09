package com.glodblock.github.mixins;

import appeng.api.networking.security.IActionHost;
import appeng.container.AEBaseContainer;
import appeng.container.me.crafting.CraftConfirmContainer;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(CraftConfirmContainer.class)
public abstract class CraftConfirmContainerMixin extends AEBaseContainer {

    public CraftConfirmContainerMixin(ContainerType<?> containerType, int id, PlayerInventory playerInventory, Object host) {
        super(containerType, id, playerInventory, host);
    }

    @ModifyVariable(
            method = "startJob",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0,
            remap = false
    )
    private ContainerType<?> addExtendedGUI(ContainerType<?> ct) {
        IActionHost ah = this.getActionHost();
        if (ah instanceof PartFluidPatternTerminal) {
            return ContainerFluidPatternTerminal.TYPE;
        }
        return null;
    }

}
