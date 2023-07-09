package com.glodblock.github.client.container;

import appeng.container.AEBaseContainer;
import appeng.container.SlotSemantic;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.slot.AppEngSlot;
import com.glodblock.github.common.block.BlockIngredientBuffer;
import com.glodblock.github.interfaces.TankDumpable;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.items.IItemHandler;

public class ContainerIngredientBuffer extends AEBaseContainer implements TankDumpable {

    private final BlockIngredientBuffer.TileIngredientBuffer tile;
    public static final ContainerType<ContainerIngredientBuffer> TYPE = ContainerTypeBuilder
            .create(ContainerIngredientBuffer::new, BlockIngredientBuffer.TileIngredientBuffer.class)
            .build("ingredient_buffer");

    public ContainerIngredientBuffer(int id, PlayerInventory ip, BlockIngredientBuffer.TileIngredientBuffer tile) {
        super(TYPE, id, ip, tile);
        this.tile = tile;
        IItemHandler inv = tile.getInternalInventory();
        for (int i = 0; i < 9; i++) {
            addSlot(new AppEngSlot(inv, i), SlotSemantic.STORAGE);
        }
        createPlayerInventorySlots(ip);
    }

    public BlockIngredientBuffer.TileIngredientBuffer getTile() {
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
