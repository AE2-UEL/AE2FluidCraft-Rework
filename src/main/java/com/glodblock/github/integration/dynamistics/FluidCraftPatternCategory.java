package com.glodblock.github.integration.dynamistics;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.misc.ItemEncodedPattern;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidCraftingPatternDetails;
import eutros.dynamistics.helper.JeiHelper;
import eutros.dynamistics.jei.SingletonRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidCraftPatternCategory implements IRecipeCategory<SingletonRecipe> {

    private static final int HEIGHT = 126;
    private static final int WIDTH = 146;
    public static final String UID = FluidCraft.resource("dy_fluid_craft_pattern").toString();
    private final IDrawableStatic background;
    private final IDrawableStatic slot;
    private final ItemStack interfaceStack;
    private final IDrawableStatic craftingBackground;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final int guiStartX;
    private final int guiStartY;

    public FluidCraftPatternCategory(IJeiHelpers helpers) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.slot = JeiHelper.getSlotDrawable();
        this.craftingBackground = guiHelper.createDrawable(new ResourceLocation("appliedenergistics2", "textures/guis/pattern.png"), 9, 85, 126, 68);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(FCItems.DENSE_CRAFT_ENCODED_PATTERN));
        this.interfaceStack = new ItemStack(FCBlocks.FLUID_ASSEMBLER);
        this.arrow = guiHelper.createDrawable(new ResourceLocation("dynamistics", "textures/gui/arrows.png"), 64, 16, 32, 32);
        this.guiStartY = HEIGHT - this.craftingBackground.getHeight();
        this.guiStartX = (WIDTH - this.craftingBackground.getWidth()) / 2;
    }

    @Nullable
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Nonnull
    @Override
    public String getUid() {
        return UID;
    }

    @Nonnull
    @Override
    public String getTitle() {
        return I18n.format("ae2fc.dynamistics.category.title.craft_pattern");
    }

    @Nonnull
    @Override
    public String getModName() {
        return "Fluid Craft for AE2";
    }

    @Nonnull
    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, @Nonnull SingletonRecipe recipe, @Nonnull IIngredients ingredients) {
        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

        ItemStack patternStack = recipe.stack;

        if (patternStack == null) {
            patternStack = ingredients.getInputs(VanillaTypes.ITEM).get(0).get(0);
        }

        stacks.init(13, true, WIDTH / 2 - 8, 8);
        stacks.set(13, patternStack);
        stacks.setBackground(13, this.slot);
        stacks.init(14, true, WIDTH / 2 - 8, 29);
        stacks.set(14, this.interfaceStack);

        if (patternStack.getItem() instanceof ItemFluidCraftEncodedPattern) {
            final ItemEncodedPattern pattern = (ItemEncodedPattern) patternStack.getItem();
            final ICraftingPatternDetails details = pattern.getPatternForItem(patternStack, null);
            if (!(details instanceof FluidCraftingPatternDetails)) {
                return;
            }

            IAEItemStack[] inputs = ((FluidCraftingPatternDetails) details).getOriginInputs();
            IAEItemStack[] outputs = details.getOutputs();

            int gridStartY = 7;
            int gridStartX = 8;
            int gridSize = 18;
            for(int i = 0; i < inputs.length; i++) {
                if (inputs[i] != null) {
                    stacks.init(i, true, this.guiStartX + gridStartX + (gridSize * (i % 3)), this.guiStartY + gridStartY + (gridSize * (i / 3)));
                    stacks.set(i, inputs[i].createItemStack());
                }
            }

            int outStartX = 100;
            if (outputs.length > 0 && outputs[0] != null ) {
                stacks.init(9, false, guiStartX + outStartX, guiStartY + gridStartY + gridSize);
                stacks.set(9, outputs[0].createItemStack());
            }
        }
    }

    @Override
    public void drawExtras(@Nonnull Minecraft minecraft) {
        this.arrow.draw(minecraft,
                (WIDTH - this.arrow.getWidth()) / 2,
                27);
        this.craftingBackground.draw(minecraft, this.guiStartX, this.guiStartY);
    }

}
