package com.glodblock.github.mixins;

import appeng.me.cache.CraftingGridCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CraftingGridCache.class)
public abstract class CraftingGridCacheMixin {

    @Redirect(
            method = "addCraftingOption",
            at = @At(value = "INVOKE", target = "Lcom/google/common/base/Preconditions;checkArgument(ZLjava/lang/Object;)V", remap = false),
            remap = false
    )
    private void removeDetailRestriction(boolean expression, Object errorMessage) {
        // NO-OP
    }

}
