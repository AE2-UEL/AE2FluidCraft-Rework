package com.glodblock.github.common.item;

import appeng.api.parts.IPartItem;
import appeng.core.Api;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.glodblock.github.loader.FCItems.defaultProps;

public class ItemPartFluidPatternTerminal extends Item implements IPartItem<PartFluidPatternTerminal> {

    public ItemPartFluidPatternTerminal() {
        super(defaultProps());
    }

    @Nullable
    @Override
    public PartFluidPatternTerminal createPart(ItemStack is) {
        return new PartFluidPatternTerminal(is);
    }

    @Override
    @Nonnull
    public ActionResultType onItemUse(@Nonnull ItemUseContext context) {
        return Api.instance().partHelper().placeBus(
                context.getItem(),
                context.getPos(),
                context.getFace(),
                context.getPlayer(),
                context.getHand(),
                context.getWorld()
        );
    }

}
