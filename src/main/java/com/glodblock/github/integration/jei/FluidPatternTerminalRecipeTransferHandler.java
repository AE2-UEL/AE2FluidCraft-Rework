package com.glodblock.github.integration.jei;

import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketValueConfig;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.client.container.ContainerFluidPatternTerminal;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.network.CPacketLoadPattern;
import com.glodblock.github.util.NameConst;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.VanillaRecipeCategoryUid;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

public class FluidPatternTerminalRecipeTransferHandler implements IRecipeTransferHandler<ContainerFluidPatternTerminal> {

    private final ExtraExtractors ext;

    FluidPatternTerminalRecipeTransferHandler(ExtraExtractors ext) {
        this.ext = ext;
    }

    @Override
    @Nonnull
    public Class<ContainerFluidPatternTerminal> getContainerClass() {
        return ContainerFluidPatternTerminal.class;
    }

    @Nullable
    @Override
    public IRecipeTransferError transferRecipe(@Nonnull ContainerFluidPatternTerminal container, @Nonnull IRecipeLayout recipeLayout,
                                               @Nonnull EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
        try {
            if (container.isCraftingMode() && !recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
                NetworkHandler.instance().sendToServer(new PacketValueConfig("PatternTerminal.CraftMode", "0"));
            }
            else if (!container.isCraftingMode() && recipeLayout.getRecipeCategory().getUid().equals(VanillaRecipeCategoryUid.CRAFTING)) {
                NetworkHandler.instance().sendToServer(new PacketValueConfig("PatternTerminal.CraftMode", "1"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (doTransfer && container.getPatternTerminal() instanceof PartFluidPatternTerminal) {
            PartFluidPatternTerminal tile = (PartFluidPatternTerminal)container.getPatternTerminal();
            IAEItemStack[] crafting = new IAEItemStack[tile.getInventoryByName("crafting").getSlots()];
            IAEItemStack[] output = new IAEItemStack[tile.getInventoryByName("output").getSlots()];
            FluidPatternEncoderRecipeTransferHandler.transferRecipeSlots(recipeLayout, crafting, output, container.craftingMode, ext);
            FluidCraft.proxy.netHandler.sendToServer(new CPacketLoadPattern(crafting, output));
        }
        return null;
    }

}
