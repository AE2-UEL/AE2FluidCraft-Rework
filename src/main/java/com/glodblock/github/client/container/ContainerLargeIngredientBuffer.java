package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.slot.AppEngSlot;
import com.glodblock.github.common.block.BlockLargeIngredientBuffer;
import com.glodblock.github.interfaces.TankDumpable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.items.IItemHandler;

public class ContainerLargeIngredientBuffer extends AEBaseContainer implements TankDumpable {

    private final BlockLargeIngredientBuffer.TileLargeIngredientBuffer tile;
    public static final ContainerType<ContainerLargeIngredientBuffer> TYPE = ContainerTypeBuilder
            .create(ContainerLargeIngredientBuffer::new, BlockLargeIngredientBuffer.TileLargeIngredientBuffer.class)
            .build("large_ingredient_buffer");

    public ContainerLargeIngredientBuffer(int id, PlayerInventory ip, BlockLargeIngredientBuffer.TileLargeIngredientBuffer tile) {
        super(TYPE, id, ip, tile);
        this.tile = tile;
        IItemHandler inv = tile.getInternalInventory();
        for (int i = 0; i < 27; i++) {
            addSlot(new AppEngSlot(inv, i), SlotSemantic.STORAGE);
        }
        createPlayerInventorySlots(ip);
    }

    public BlockLargeIngredientBuffer.TileLargeIngredientBuffer getTile() {
        return tile;
    }

    @Override
    public boolean canDumpTank(int index) {
        return tile.getFluidInventory().getFluidInSlot(index) != null;
    }

    @Override
    public void dumpTank(int index) {
        if (index >= 0 && index < tile.getFluidInventory().getSlots()) {
            tile.getFluidInventory().setFluidInSlot(index, null);
        }
    }

}