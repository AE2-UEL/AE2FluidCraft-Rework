package com.glodblock.github.common.item;

import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.parts.PartFluidPatternTerminal;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemPartFluidPatternTerminal extends Item implements IPartItem {

    public ItemPartFluidPatternTerminal() {
        this.setMaxStackSize(64);
        this.setUnlocalizedName(NameConst.ITEM_PART_FLUID_PATTERN_TERMINAL);
        AEApi.instance().partHelper().setItemBusRenderer(this);
    }

    @Nullable
    @Override
    public PartFluidPatternTerminal createPartFromItemStack(ItemStack is) {
        return new PartFluidPatternTerminal(is);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float xOffset, float yOffset, float zOffset) {
        return AEApi.instance().partHelper().placeBus(player.getHeldItem(), x, y, z, side, player, world);
    }

    public ItemPartFluidPatternTerminal register() {
        GameRegistry.registerItem(this, NameConst.ITEM_PART_FLUID_PATTERN_TERMINAL, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    @Override
    public void registerIcons(IIconRegister _iconRegister) {
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getSpriteNumber() {
        return 0;
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

}
