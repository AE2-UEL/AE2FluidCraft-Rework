package baubles.api.cap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IBaublesItemHandler extends IItemHandlerModifiable {
    boolean isItemValidForSlot(int var1, ItemStack var2, EntityLivingBase var3);

    boolean isEventBlocked();

    void setEventBlock(boolean var1);

    boolean isChanged(int var1);

    void setChanged(int var1, boolean var2);

    void setPlayer(EntityLivingBase var1);
}