package com.glodblock.github.client.container;

import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.SlotInaccessible;
import appeng.tile.inventory.AppEngInternalInventory;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;

public class ContainerItemAmountChange extends AEBaseContainer {

    private final Slot patternValue;
    @GuiSync(10)
    public long initValue;
    @GuiSync(11)
    public int valueIndex;

    public ContainerItemAmountChange(final InventoryPlayer ip, final ITerminalHost te )
    {
        super( ip, te );
        this.patternValue = new SlotInaccessible( new AppEngInternalInventory( null, 1 ), 0, 34, 53 );
        this.addSlotToContainer( patternValue );
    }

    public Slot getPatternValue()
    {
        return patternValue;
    }

    public int getValueIndex()
    {
        return valueIndex;
    }

    public void setValueIndex( int valueIndex )
    {
        this.valueIndex = valueIndex;
    }

    public void setInitValue( long value ) {
        this.initValue = value;
    }

    public long getInitValue() {
        return this.initValue;
    }

}
