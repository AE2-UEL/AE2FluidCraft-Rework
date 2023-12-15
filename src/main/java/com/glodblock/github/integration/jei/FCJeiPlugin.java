package com.glodblock.github.integration.jei;

import com.glodblock.github.integration.dynamistics.FluidCraftPatternCategory;
import com.glodblock.github.integration.dynamistics.FluidCraftPatternPlugin;
import com.glodblock.github.integration.dynamistics.FluidPatternCategory;
import com.glodblock.github.integration.dynamistics.FluidPatternPlugin;
import com.glodblock.github.integration.jei.interfaces.IngredientExtractor;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.util.ModAndClassUtil;
import eutros.dynamistics.helper.ItemHelper;
import eutros.dynamistics.helper.JeiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import mezz.jei.config.Constants;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;

@JEIPlugin
public class FCJeiPlugin implements IModPlugin {

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        if (ModAndClassUtil.DY) {
            IJeiHelpers helpers = registry.getJeiHelpers();
            JeiHelper.makeSlotDrawable(helpers.getGuiHelper());
            registry.addRecipeCategories(
                    new FluidPatternCategory(helpers),
                    new FluidCraftPatternCategory(helpers)
            );
        }
    }

    @Override
    public void register(@Nonnull IModRegistry registry) {
        IngredientExtractor<FluidStack> extModMach = Loader.isModLoaded("modularmachinery")
                ? new ModMachHybridFluidStackExtractor(registry) : null;
        RecipeTransferBuilder.setExtractor(new ExtraExtractors(extModMach));
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new FluidPatternEncoderRecipeTransferHandler(), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new FluidPatternTerminalRecipeTransferHandler(), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new WirelessFluidPatternTerminalRecipeTransferHandler(), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new ExtendedFluidPatternTerminalRecipeTransferHandler(), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        registry.getRecipeTransferRegistry().addRecipeTransferHandler(
                new UltimateEncoderRecipeTransferHandler(), Constants.UNIVERSAL_RECIPE_TRANSFER_UID);
        if (ModAndClassUtil.DY) {
            registry.addRecipeRegistryPlugin(FluidPatternPlugin.INSTANCE);
            registry.addRecipeCatalyst(new ItemStack(ItemHelper.AE2.INTERFACE), FluidPatternCategory.UID);
            registry.addRecipeCatalyst(new ItemStack(FCBlocks.DUAL_INTERFACE), FluidPatternCategory.UID);
            registry.addRecipeRegistryPlugin(FluidCraftPatternPlugin.INSTANCE);
            registry.addRecipeCatalyst(new ItemStack(FCBlocks.FLUID_ASSEMBLER), FluidCraftPatternCategory.UID);
        }
    }

}