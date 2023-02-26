package com.glodblock.github.integration.jei;

import com.glodblock.github.integration.jei.interfaces.IngredientExtractor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.config.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

@JEIPlugin
public class FCJeiPlugin implements IModPlugin {
    @Override
    public void register(IModRegistry registry) {
        IngredientExtractor<FluidStack> extModMach = Loader.isModLoaded("modularmachinery")
                ? new ModMachHybridFluidStackExtractor(registry) : null;
        RecipeTransferBuilder.setExtractor(new ExtraExtractors(extModMach));
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new FluidPatternEncoderRecipeTransferHandler(), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new FluidPatternTerminalRecipeTransferHandler(), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new ExtendedFluidPatternTerminalRecipeTransferHandler(), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
    }

}