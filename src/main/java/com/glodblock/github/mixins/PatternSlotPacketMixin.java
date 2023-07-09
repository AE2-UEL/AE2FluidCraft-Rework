package com.glodblock.github.mixins;

import appeng.core.sync.network.INetworkInfo;
import appeng.core.sync.packets.PatternSlotPacket;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PatternSlotPacket.class)
public abstract class PatternSlotPacketMixin {

    @Inject(
            method = "serverPacketData",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void addFluidPatternHandler(INetworkInfo manager, PlayerEntity player, CallbackInfo ci) {
        ServerPlayerEntity sender = (ServerPlayerEntity) player;
        if (sender.openContainer instanceof ContainerFluidPatternTerminal) {
            ContainerFluidPatternTerminal patternTerminal = (ContainerFluidPatternTerminal)sender.openContainer;
            patternTerminal.craftOrGetItem((PatternSlotPacket)(Object) this);
            ci.cancel();
        }
    }

}
