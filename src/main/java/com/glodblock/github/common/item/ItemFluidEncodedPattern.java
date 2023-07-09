package com.glodblock.github.common.item;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.Api;
import appeng.core.localization.GuiText;
import appeng.items.misc.EncodedPatternItem;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;
import com.glodblock.github.interfaces.HasCustomModel;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.FluidPatternDetails;
import com.glodblock.github.util.InvalidFCPatternHelper;
import com.glodblock.github.util.NameConst;
import com.google.common.base.Preconditions;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import java.util.Collection;
import java.util.List;

import static com.glodblock.github.loader.FCItems.defaultProps;

public class ItemFluidEncodedPattern extends EncodedPatternItem implements HasCustomModel {

    public ItemFluidEncodedPattern() {
        super(defaultProps());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> lines, ITooltipFlag advancedTooltips) {
        final ICraftingPatternDetails details = Api.instance().crafting().decodePattern(stack, world);
        if (details == null) {
            if (!stack.hasTag()) {
                return;
            }
            stack.setDisplayName(GuiText.InvalidPattern.text().deepCopy().mergeStyle(TextFormatting.RED));
            InvalidFCPatternHelper invalid = new InvalidFCPatternHelper(stack);
            final ITextComponent label = (GuiText.Creates.text()).deepCopy().appendString(": ");
            final ITextComponent and = new StringTextComponent(" ").deepCopy().appendSibling(GuiText.And.text())
                    .deepCopy()
                    .appendString(" ");
            final ITextComponent with = GuiText.With.text().deepCopy().appendString(": ");
            boolean first = true;
            for (final InvalidFCPatternHelper.PatternIngredient output : invalid.getOutputs()) {
                lines.add((first ? label : and).deepCopy().appendSibling(output.getFormattedToolTip()));
                first = false;
            }

            first = true;
            for (final InvalidFCPatternHelper.PatternIngredient input : invalid.getInputs()) {
                lines.add((first ? with : and).deepCopy().appendSibling(input.getFormattedToolTip()));
                first = false;
            }
            return;
        }

        if (stack.hasDisplayName()) {
            stack.removeChildTag("display");
        }

        final boolean isCrafting = details.isCraftable();
        final boolean substitute = details.canSubstitute();

        final Collection<IAEItemStack> in = details.getInputs();
        final Collection<IAEItemStack> out = details.getOutputs();

        final ITextComponent label = (isCrafting ? GuiText.Crafts.text() : GuiText.Creates.text()).deepCopy()
                .appendString(": ");
        final ITextComponent and = new StringTextComponent(" ").deepCopy().appendSibling(GuiText.And.text())
                .appendString(" ");
        final ITextComponent with = GuiText.With.text().deepCopy().appendString(": ");

        boolean first = true;
        for (final IAEItemStack anOut : out) {
            if (anOut == null) {
                continue;
            }

            lines.add((first ? label : and).deepCopy().appendString(anOut.getStackSize() + "x ")
                    .appendSibling(Platform.getItemDisplayName(anOut)));
            first = false;
        }

        first = true;
        for (final IAEItemStack anIn : in) {
            if (anIn == null) {
                continue;
            }

            lines.add((first ? with : and).deepCopy().appendString(anIn.getStackSize() + "x ")
                    .appendSibling(Platform.getItemDisplayName(anIn)));
            first = false;
        }

        if (isCrafting) {
            final ITextComponent substitutionLabel = GuiText.Substitute.text().deepCopy().appendString(" ");
            final ITextComponent canSubstitute = substitute ? GuiText.Yes.text() : GuiText.No.text();

            lines.add(substitutionLabel.deepCopy().appendSibling(canSubstitute));
        }
    }

    public ICraftingPatternDetails getDetails(ItemStack stack) {
        return FluidPatternDetails.fromPattern(stack);
    }

    @Override
    public boolean isEncodedPattern(ItemStack itemStack) {
        return itemStack != null && !itemStack.isEmpty() && itemStack.getItem() instanceof ItemFluidEncodedPattern && itemStack.getTag() != null && itemStack.getTag().contains("in") && itemStack.getTag().contains("out");
    }

    @Override
    public ItemStack getOutput(ItemStack item) {
        ICraftingPatternDetails d = getDetails(item);
        if (d == null) {
            return ItemStack.EMPTY;
        } else {
            return d.getOutputs().size() > 0 ?
                    d.getOutputs().get(0).createItemStack() :
                    ItemStack.EMPTY;
        }
    }

    @Override
    public ResourceLocation getCraftingRecipeId(ItemStack itemStack) {
        return null;
    }

    @Override
    public List<IAEItemStack> getIngredients(ItemStack itemStack) {
        ICraftingPatternDetails d = getDetails(itemStack);
        Preconditions.checkArgument(d != null, "Invalid fluid pattern inputs!");
        return d.getInputs();
    }

    @Override
    public List<IAEItemStack> getProducts(ItemStack itemStack) {
        ICraftingPatternDetails d = getDetails(itemStack);
        Preconditions.checkArgument(d != null, "Invalid fluid pattern outputs!");
        return d.getOutputs();
    }

    @Override
    public boolean allowsSubstitution(ItemStack itemStack) {
        return false;
    }

    @Override
    public ResourceLocation getCustomModelPath() {
        return NameConst.MODEL_DENSE_ENCODED_PATTERN;
    }

    public ItemStack encodeStack(ItemStack[] inputs, ItemStack[] outputs) {
        ItemStack pattern = new ItemStack(FCItems.DENSE_ENCODED_PATTERN);
        ListNBT tagIn = new ListNBT();
        ListNBT tagOut = new ListNBT();
        CompoundNBT v = new CompoundNBT();
        for (ItemStack stack : inputs) {
            tagIn.add(encodeNBT(stack));
        }
        for (ItemStack stack : outputs) {
            tagOut.add(encodeNBT(stack));
        }
        v.put("in", tagIn);
        v.put("out", tagOut);
        pattern.setTag(v);
        return pattern;
    }

    private CompoundNBT encodeNBT(ItemStack stack) {
        if (stack.isEmpty()) {
            return new CompoundNBT();
        } else {
            CompoundNBT tag = new CompoundNBT();
            if (stack.getItem() instanceof ItemFluidPacket) {
                FluidStack fluid = ItemFluidPacket.getFluidStack(stack);
                if (!fluid.isEmpty()) {
                    IAEItemStack drop = ItemFluidDrop.newAeStack(fluid);
                    if (drop != null) {
                        drop.writeToNBT(tag);
                    }
                }
            } else {
                IAEItemStack aeStack = AEItemStack.fromItemStack(stack);
                if (aeStack != null) {
                    aeStack.writeToNBT(tag);
                }
            }
            return tag;
        }
    }

}
