package com.glodblock.github.integration.dynamistics;

import com.glodblock.github.loader.FCItems;
import eutros.dynamistics.jei.SingletonRecipe;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidCraftPatternPlugin implements IRecipeRegistryPlugin {

    public static final IRecipeRegistryPlugin INSTANCE = new FluidCraftPatternPlugin();
    private static ItemStack EXAMPLE_PATTERN;

    private FluidCraftPatternPlugin() {
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        if(focus.getValue() instanceof ItemStack) {
            ItemStack stack = (ItemStack) focus.getValue();
            if(stack.getItem() == FCItems.DENSE_CRAFT_ENCODED_PATTERN && stack.hasTagCompound()) {
                return Collections.singletonList(FluidCraftPatternCategory.UID);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if(recipeCategory instanceof FluidCraftPatternCategory && ((ItemStack) focus.getValue()).getItem() == FCItems.DENSE_CRAFT_ENCODED_PATTERN) {
            return Collections.singletonList(new SingletonRecipe((ItemStack) focus.getValue(), focus.getMode() == IFocus.Mode.INPUT).cast());
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        if (recipeCategory instanceof FluidCraftPatternCategory) {
            return Collections.singletonList(new SingletonRecipe(getExampleItem(), true).cast());
        }
        return Collections.emptyList();
    }

    private static ItemStack getExampleItem() {
        if (EXAMPLE_PATTERN == null) {
            EXAMPLE_PATTERN = new ItemStack(FCItems.DENSE_CRAFT_ENCODED_PATTERN);
            final NBTTagCompound encodedValue = new NBTTagCompound();
            final NBTTagList tagIn = new NBTTagList();
            final NBTTagList tagOut = new NBTTagList();
            ItemStack wool = new ItemStack(Blocks.WOOL, 1, 2);
            ItemStack water = new ItemStack(Items.WATER_BUCKET);
            ItemStack clear = new ItemStack(Blocks.WOOL);
            tagIn.appendTag(water.writeToNBT(new NBTTagCompound()));
            tagIn.appendTag(wool.writeToNBT(new NBTTagCompound()));
            tagOut.appendTag(clear.writeToNBT(new NBTTagCompound()));
            encodedValue.setTag("in", tagIn);
            encodedValue.setTag("out", tagOut);
            EXAMPLE_PATTERN.setTagCompound(encodedValue);
        }
        return EXAMPLE_PATTERN;
    }

}
