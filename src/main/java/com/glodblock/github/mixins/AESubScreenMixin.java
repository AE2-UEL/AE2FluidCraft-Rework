package com.glodblock.github.mixins;

import appeng.client.gui.implementations.AESubScreen;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.loader.FCItems;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AESubScreen.class)
public abstract class AESubScreenMixin {

    @Final
    @Mutable
    @Shadow(remap = false)
    private ContainerType<?> previousContainerType;
    @Final
    @Mutable
    @Shadow(remap = false)
    private ItemStack previousContainerIcon;

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void addExtendedGUI(Object containerHost, CallbackInfo ci) {
        if (containerHost instanceof PartFluidPatternTerminal) {
            previousContainerIcon = new ItemStack(FCItems.PART_FLUID_PATTERN_TERMINAL);
            previousContainerType = ContainerFluidPatternTerminal.TYPE;
        }
    }

}
