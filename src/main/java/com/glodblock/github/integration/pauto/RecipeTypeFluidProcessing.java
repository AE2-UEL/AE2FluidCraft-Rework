package com.glodblock.github.integration.pauto;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.integration.jei.FCJeiPlugin;
import com.glodblock.github.integration.jei.WrappedIngredient;
import com.glodblock.github.loader.FCBlocks;
import it.unimi.dsi.fastutil.ints.*;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thelm.packagedauto.api.IRecipeInfo;
import thelm.packagedauto.api.IRecipeType;
import thelm.packagedauto.integration.jei.PackagedAutoJEIPlugin;

import java.awt.*;
import java.util.Iterator;
import java.util.List;

public class RecipeTypeFluidProcessing implements IRecipeType {

    public static final RecipeTypeFluidProcessing INSTANCE = new RecipeTypeFluidProcessing();

    private static final ResourceLocation NAME = FluidCraft.resource("fluid_processing");
    private static final IntSet SLOTS;
    private static final Color SLOT_COLOUR = new Color(0x8B8BAC);
    private static final int NUM_SLOTS_CRAFT = 81, NUM_SLOTS_OUT = 9;

    static {
        IntSet slots = new IntOpenHashSet();
        for (int i = 0; i < 90; i++) {
            slots.add(i);
        }
        SLOTS = IntSets.unmodifiable(slots);
    }

    private RecipeTypeFluidProcessing() {
        // NO-OP
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal("ae2fc.pauto.fluid_processing.name");
    }

    @SuppressWarnings("deprecation")
    @Override
    public String getLocalizedNameShort() {
        return I18n.translateToLocal("ae2fc.pauto.fluid_processing.name_short");
    }

    @Override
    public IRecipeInfo getNewRecipeInfo() {
        return new RecipeInfoFluidProcessing();
    }

    @Override
    public IntSet getEnabledSlots() {
        return SLOTS;
    }

    @Override
    public boolean canSetOutput() {
        return true;
    }

    @Override
    public boolean hasMachine() {
        return false;
    }

    @Override
    public List<String> getJEICategories() {
        return PackagedAutoJEIPlugin.getAllRecipeCategories();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Object getRepresentation() {
        return new ItemStack(FCBlocks.FLUID_PATTERN_ENCODER);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public Color getSlotColor(int slot) {
        return SLOT_COLOUR;
    }

    @Optional.Method(modid = "jei")
    @Override
    public Int2ObjectMap<ItemStack> getRecipeTransferMap(IRecipeLayout recipeLayout, String category) {
        Int2ObjectMap<ItemStack> tfrs = new Int2ObjectOpenHashMap<>();
        int ndxCrafting = 0, ndxOutput = 0;
        for (IGuiIngredient<ItemStack> ing : recipeLayout.getItemStacks().getGuiIngredients().values()) {
            if (ing.isInput()) {
                if (ndxCrafting < NUM_SLOTS_CRAFT) {
                    ItemStack stack = ing.getDisplayedIngredient();
                    if (stack != null) {
                        tfrs.put(ndxCrafting++, stack);
                    }
                }
            } else {
                if (ndxOutput < NUM_SLOTS_OUT) {
                    ItemStack stack = ing.getDisplayedIngredient();
                    if (stack != null) {
                        tfrs.put(NUM_SLOTS_CRAFT + ndxOutput++, stack);
                    }
                }
            }
        }
        for (IGuiIngredient<FluidStack> ing : recipeLayout.getFluidStacks().getGuiIngredients().values()) {
            if (ing.isInput()) {
                if (ndxCrafting < NUM_SLOTS_CRAFT) {
                    tfrs.put(ndxCrafting++, ItemFluidPacket.newStack(ing.getDisplayedIngredient()));
                }
            } else {
                if (ndxOutput < NUM_SLOTS_OUT) {
                    tfrs.put(NUM_SLOTS_CRAFT + ndxOutput++, ItemFluidPacket.newStack(ing.getDisplayedIngredient()));
                }
            }
        }
        Iterator<WrappedIngredient<FluidStack>> iter = FCJeiPlugin.getExtraExtractors().extractFluids(recipeLayout).iterator();
        while (iter.hasNext()) {
            WrappedIngredient<FluidStack> ing = iter.next();
            if (ing.isInput()) {
                if (ndxCrafting < NUM_SLOTS_CRAFT) {
                    tfrs.put(ndxCrafting++, ItemFluidPacket.newStack(ing.getIngredient()));
                }
            } else {
                if (ndxOutput < NUM_SLOTS_OUT) {
                    tfrs.put(NUM_SLOTS_CRAFT + ndxOutput++, ItemFluidPacket.newStack(ing.getIngredient()));
                }
            }
        }
        return tfrs;
    }

}
