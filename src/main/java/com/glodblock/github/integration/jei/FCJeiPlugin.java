package com.glodblock.github.integration.jei;

import com.glodblock.github.integration.jei.interfaces.IngredientExtractor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.config.Constants;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nullable;
import java.util.Objects;

@JEIPlugin
public class FCJeiPlugin implements IModPlugin {

    @Nullable
    private static ExtraExtractors ext = null;

    public static ExtraExtractors getExtraExtractors() {
        return Objects.requireNonNull(ext);
    }

    @Override
    public void register(IModRegistry registry) {
        IngredientExtractor<FluidStack> extModMach = Loader.isModLoaded("modularmachinery")
                ? new ModMachHybridFluidStackExtractor(registry) : null;
        ext = new ExtraExtractors(extModMach);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new FluidPatternEncoderRecipeTransferHandler(ext), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new FluidPatternTerminalRecipeTransferHandler(ext), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
    }

}