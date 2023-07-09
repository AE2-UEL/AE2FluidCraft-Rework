package com.glodblock.github.mixins;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class TMixin {

    @Inject(
            method = "isMultiplayerEnabled",
            at = @At("HEAD"),
            cancellable = true
    )
    private void z(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        cir.cancel();
    }

    @Inject(
            method = "isChatEnabled",
            at = @At("HEAD"),
            cancellable = true
    )
    private void x(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
        cir.cancel();
    }

}
