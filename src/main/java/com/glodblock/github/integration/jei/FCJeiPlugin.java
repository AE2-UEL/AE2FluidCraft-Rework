package com.glodblock.github.integration.jei;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.integration.jei.handlers.FluidPatternTerminalRecipeTransferHandler;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

@JeiPlugin
public class FCJeiPlugin implements IModPlugin {

    @Nonnull
    @Override
    public ResourceLocation getPluginUid() {
        return FluidCraft.resource("jei");
    }

    @Override
    public void registerRecipeTransferHandlers(@Nonnull IRecipeTransferRegistration registration) {
        registration.addUniversalRecipeTransferHandler(new FluidPatternTerminalRecipeTransferHandler());
    }

}
