package com.glodblock.github.common.item;

import appeng.api.parts.IPartItem;
import appeng.core.Api;
import com.glodblock.github.common.part.PartDualInterface;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.glodblock.github.loader.FCItems.defaultProps;

public class ItemPartDualInterface extends Item implements IPartItem<PartDualInterface> {

    public ItemPartDualInterface() {
        super(defaultProps());
    }

    @Nullable
    @Override
    public PartDualInterface createPart(ItemStack is) {
        return new PartDualInterface(is);
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
