package com.glodblock.github.integration.dynamistics;

import appeng.api.storage.data.IAEItemStack;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.fake.FakeFluids;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidPatternDetails;
import eutros.dynamistics.jei.SingletonRecipe;
import mcp.MethodsReturnNonnullByDefault;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeRegistryPlugin;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FluidPatternPlugin implements IRecipeRegistryPlugin {

    public static final IRecipeRegistryPlugin INSTANCE = new FluidPatternPlugin();
    private static ItemStack EXAMPLE_PATTERN;

    private FluidPatternPlugin() {
    }

    @Override
    public <V> List<String> getRecipeCategoryUids(IFocus<V> focus) {
        if(focus.getValue() instanceof ItemStack) {
            ItemStack stack = (ItemStack) focus.getValue();
            if((stack.getItem() == FCItems.DENSE_ENCODED_PATTERN || stack.getItem() == FCItems.LARGE_ITEM_ENCODED_PATTERN) && stack.hasTagCompound()) {
                return Collections.singletonList(FluidPatternCategory.UID);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper, V> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory, IFocus<V> focus) {
        if(recipeCategory instanceof FluidPatternCategory &&
                (((ItemStack) focus.getValue()).getItem() == FCItems.DENSE_ENCODED_PATTERN
                        || ((ItemStack) focus.getValue()).getItem() == FCItems.LARGE_ITEM_ENCODED_PATTERN)) {
            return Collections.singletonList(new SingletonRecipe((ItemStack) focus.getValue(), focus.getMode() == IFocus.Mode.INPUT).cast());
        }
        return Collections.emptyList();
    }

    @Override
    public <T extends IRecipeWrapper> List<T> getRecipeWrappers(IRecipeCategory<T> recipeCategory) {
        if (recipeCategory instanceof FluidPatternCategory) {
            return Collections.singletonList(new SingletonRecipe(getExampleItem(), true).cast());
        }
        return Collections.emptyList();
    }

    private static ItemStack getExampleItem() {
        if (EXAMPLE_PATTERN == null) {
            EXAMPLE_PATTERN = new ItemStack(FCItems.DENSE_ENCODED_PATTERN);
            FluidPatternDetails pattern = new FluidPatternDetails(EXAMPLE_PATTERN);
            pattern.setInputs(new IAEItemStack[] {
                    FakeFluids.packFluid2AEDrops(FluidRegistry.getFluidStack("water", 1000)),
                    AEItemStack.fromItemStack(new ItemStack(Items.BUCKET))
            });
            pattern.setOutputs(new IAEItemStack[] {
                    AEItemStack.fromItemStack(new ItemStack(Items.WATER_BUCKET))
            });
            pattern.writeToStack();
        }
        return EXAMPLE_PATTERN;
    }

}
