package com.glodblock.github.integration.dynamistics;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.items.misc.ItemEncodedPattern;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.integration.jei.CubicFluidRender;
import com.glodblock.github.loader.FCBlocks;
import com.glodblock.github.loader.FCItems;
import eutros.dynamistics.helper.JeiHelper;
import eutros.dynamistics.jei.SingletonRecipe;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidPatternCategory implements IRecipeCategory<SingletonRecipe> {

    private static final int HEIGHT = 126;
    private static final int WIDTH = 146;
    public static final String UID = FluidCraft.resource("dy_fluid_pattern").toString();
    private final IDrawableStatic background;
    private final IDrawableStatic slot;
    private final ItemStack interfaceStack;
    private final IDrawableStatic craftingBackground;
    private final IDrawable icon;
    private final IDrawableStatic arrow;
    private final int guiStartX;
    private final int guiStartY;

    public FluidPatternCategory(IJeiHelpers helpers) {
        IGuiHelper guiHelper = helpers.getGuiHelper();
        this.slot = JeiHelper.getSlotDrawable();
        this.craftingBackground = guiHelper.createDrawable(new ResourceLocation("appliedenergistics2", "textures/guis/pattern2.png"), 9, 85, 126, 68);
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(FCItems.DENSE_ENCODED_PATTERN));
        this.interfaceStack = new ItemStack(FCBlocks.DUAL_INTERFACE);
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
        return I18n.format("ae2fc.dynamistics.category.title.pattern");
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
        IGuiFluidStackGroup fluids = recipeLayout.getFluidStacks();

        ItemStack patternStack = recipe.stack;

        if (patternStack == null) {
            patternStack = ingredients.getInputs(VanillaTypes.ITEM).get(0).get(0);
        }

        stacks.init(13, true, WIDTH / 2 - 8, 8);
        stacks.set(13, patternStack);
        stacks.setBackground(13, this.slot);
        stacks.init(14, true, WIDTH / 2 - 8, 29);
        stacks.set(14, this.interfaceStack);

        if (patternStack.getItem() instanceof ItemFluidEncodedPattern) {
            final ItemEncodedPattern pattern = (ItemEncodedPattern) patternStack.getItem();
            final ICraftingPatternDetails details = pattern.getPatternForItem(patternStack, null);
            if (details == null) {
                return;
            }

            IAEItemStack[] inputs = details.getInputs();
            IAEItemStack[] outputs = details.getOutputs();

            int gridStartY = 7;
            int gridStartX = 8;
            int gridSize = 18;
            for(int i = 0; i < inputs.length; i++) {
                if (inputs[i] != null) {
                    if (inputs[i].getItem() instanceof ItemFluidDrop) {
                        FluidStack fluidStack = ItemFluidDrop.getFluidStack(inputs[i].createItemStack());
                        if (fluidStack != null) {
                            fluids.init(
                                    i, true,
                                    new CubicFluidRender(fluidStack.amount, false, 16, 16, null),
                                    this.guiStartX + gridStartX + (gridSize * (i % 3)) + 1,
                                    this.guiStartY + gridStartY + (gridSize * (i / 3)) + 1,
                                    16, 16,
                                    0, 0
                            );
                            fluids.set(i, fluidStack);
                        } else {
                            stacks.init(i, true, this.guiStartX + gridStartX + (gridSize * (i % 3)), this.guiStartY + gridStartY + (gridSize * (i / 3)));
                            stacks.set(i, inputs[i].createItemStack());
                        }
                    } else {
                        stacks.init(i, true, this.guiStartX + gridStartX + (gridSize * (i % 3)), this.guiStartY + gridStartY + (gridSize * (i / 3)));
                        stacks.set(i, inputs[i].createItemStack());
                    }
                }
            }

            int outStartX = 100;
            for(int i = 0; i < outputs.length; i++) {
                if (outputs[i] != null) {
                    if (outputs[i].getItem() instanceof ItemFluidDrop) {
                        FluidStack fluidStack = ItemFluidDrop.getFluidStack(inputs[i].createItemStack());
                        if (fluidStack != null) {
                            fluids.init(
                                    9 + i, false,
                                    new CubicFluidRender(fluidStack.amount, false, 16, 16, null),
                                    this.guiStartX + outStartX + 1,
                                    this.guiStartY + gridStartY + gridSize * i + 1,
                                    16, 16,
                                    0, 0
                            );
                            fluids.set(9 + i, fluidStack);
                        } else {
                            stacks.init(9 + i, false, this.guiStartX + outStartX, this.guiStartY + gridStartY + gridSize * i);
                            stacks.set(9 + i, outputs[i].createItemStack());
                        }
                    } else {
                        stacks.init(9 + i, false, this.guiStartX + outStartX, this.guiStartY + gridStartY + gridSize * i);
                        stacks.set(9 + i, outputs[i].createItemStack());
                    }
                }
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
