package com.glodblock.github.mixins;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.core.api.ApiCrafting;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ApiCrafting.class)
public abstract class ApiCraftingMixin {

    @Inject(
            method = "decodePattern",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private void injectDecodePattern(ItemStack is, World world, boolean autoRecovery, CallbackInfoReturnable<ICraftingPatternDetails> cir) {
        if (is != null && is.getItem() instanceof ItemFluidEncodedPattern) {
            ItemFluidEncodedPattern pattern = (ItemFluidEncodedPattern) is.getItem();
            if (pattern.isEncodedPattern(is)) {
                ICraftingPatternDetails d = pattern.getDetails(is);
                cir.setReturnValue(d);
            }
            cir.cancel();
        }
    }

}
