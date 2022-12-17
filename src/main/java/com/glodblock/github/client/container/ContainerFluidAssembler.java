package com.glodblock.github.client.container;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.interfaces.IProgressProvider;
import appeng.container.slot.*;
import appeng.util.Platform;
import com.glodblock.github.common.item.ItemFluidCraftEncodedPattern;
import com.glodblock.github.common.tile.TileFluidAssembler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

public class ContainerFluidAssembler extends AEBaseContainer implements IOptionalSlotHost, IProgressProvider {

    private final TileFluidAssembler tile;
    @GuiSync(1)
    public int patternCap = 0;
    @GuiSync(2)
    public int progress = 0;

    public ContainerFluidAssembler(InventoryPlayer ipl, TileFluidAssembler tile) {
        super(ipl, tile);
        this.tile = tile;
        for (int row = 0; row < 4; ++row) {
            for (int x = 0; x < 9; x++) {
                this.addSlotToContainer(new CraftPattern(tile.invPatterns, this, x + row * 9,
                        8 + 18 * x, 83 + 18 * row, row, this.getInventoryPlayer()).setStackLimit(1));
            }
        }
        for (int i = 0; i < 3; i ++) {
            for (int j = 0; j < 3; j ++) {
                this.addSlotToContainer(new SlotFake(tile.gridInv, j + i * 3, 29 + j * 18, 16 + i * 18));
            }
        }
        this.addSlotToContainer(new SlotFake(tile.output, 0, 126, 35));
        this.addSlotToContainer(new AppEngSlot(tile.upgrade, 0, 134, 61));
        this.addSlotToContainer(new AppEngSlot(tile.upgrade, 1, 152, 61));
        bindPlayerInventory(ipl, 0, 167);
    }

    @Override
    public boolean isSlotEnabled(int i) {
        return tile.getPatternCap() >= i;
    }

    @Override
    public void detectAndSendChanges() {
        this.verifyPermissions(SecurityPermissions.BUILD, false);
        if (patternCap != tile.getPatternCap()) {
            patternCap = tile.getPatternCap();
            tile.dropExcessPatterns();
        }
        if (Platform.isServer()) {
            progress = tile.getProgress();
        }
        super.detectAndSendChanges();
    }

    @Override
    public void onUpdate(final String field, final Object oldValue, final Object newValue) {
        super.onUpdate(field, oldValue, newValue);
        if (Platform.isClient() && field.equals("patternCap"))
            tile.dropExcessPatterns();
    }

    @Override
    public int getCurrentProgress() {
        return progress;
    }

    @Override
    public int getMaxProgress() {
        return TileFluidAssembler.TIME;
    }

    static class CraftPattern extends OptionalSlotRestrictedInput {

        public CraftPattern(IItemHandler i, IOptionalSlotHost host, int slotIndex, int x, int y, int grpNum, InventoryPlayer invPlayer) {
            super(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, i, host, slotIndex, x, y, grpNum, invPlayer);
        }

        @Override
        public boolean isItemValid(ItemStack i) {
            if (!this.getContainer().isValidForSlot(this, i)) {
                return false;
            } else if (i.isEmpty()) {
                return false;
            } else if (i.getItem() == Items.AIR) {
                return false;
            } else if (!super.isItemValid(i)) {
                return false;
            }
            return i.getItem() instanceof ItemFluidCraftEncodedPattern;
        }
    }
}

