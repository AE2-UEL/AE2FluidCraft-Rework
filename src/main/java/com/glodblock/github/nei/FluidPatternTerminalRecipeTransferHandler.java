package com.glodblock.github.nei;

import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import codechicken.nei.recipe.TemplateRecipeHandler;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.gui.GuiFluidPatternTerminal;
import com.glodblock.github.nei.object.OrderStack;
import com.glodblock.github.nei.recipes.FluidRecipe;
import com.glodblock.github.network.CPacketTransferRecipe;
import net.minecraft.client.gui.inventory.GuiContainer;

import java.util.HashSet;
import java.util.List;

public class FluidPatternTerminalRecipeTransferHandler implements IOverlayHandler {

    public static final FluidPatternTerminalRecipeTransferHandler INSTANCE = new FluidPatternTerminalRecipeTransferHandler();
    public static final HashSet<String> notOtherSet = new HashSet<>();
    public static final HashSet<String> craftSet = new HashSet<>();

    static {
        notOtherSet.add("smelting");
        notOtherSet.add("brewing");
        craftSet.add("crafting");
        craftSet.add("crafting2x2");
    }

    @Override
    public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
        if (isCorrectGui(firstGui)) {
            List<OrderStack<?>> in = FluidRecipe.getPackageInputs(recipe, recipeIndex);
            List<OrderStack<?>> out = FluidRecipe.getPackageOutputs(recipe, recipeIndex, !notUseOther(recipe));
            boolean craft = shouldCraft(recipe);
            FluidCraft.proxy.netHandler.sendToServer(new CPacketTransferRecipe(in, out, craft));
        }
    }

    private boolean isCorrectGui(GuiContainer firstGui) {
        return firstGui instanceof GuiFluidPatternTerminal;
    }

    private boolean notUseOther(IRecipeHandler recipeHandler) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipeHandler;
        return notOtherSet.contains(tRecipe.getOverlayIdentifier());
    }

    private boolean shouldCraft(IRecipeHandler recipeHandler) {
        TemplateRecipeHandler tRecipe = (TemplateRecipeHandler) recipeHandler;
        return craftSet.contains(tRecipe.getOverlayIdentifier());
    }

}
