package com.glodblock.github.common.item;

import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.NameConst;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import java.util.HashMap;
import java.util.List;

public class ItemBasicFluidStoragePart extends Item {

    public static final int types = 7;
    private final static HashMap<Integer, IIcon> icon = new HashMap<>();

    public ItemBasicFluidStoragePart() {
        super();
        setHasSubtypes(true);
        setUnlocalizedName(NameConst.ITEM_FLUID_PART);
    }

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings("unchecked")
    public void getSubItems(Item item, CreativeTabs tab, List list) {
        for (int i = 0; i < types; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        int meta = stack.getItemDamage();
        return StatCollector.translateToLocalFormatted("item.fluid_part." + meta + ".name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        for (int i = 0; i < types; i ++) {
            icon.put(i, iconRegister.registerIcon(NameConst.RES_KEY + NameConst.ITEM_FLUID_PART + "." + i));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return icon.get(meta);
    }

    public ItemBasicFluidStoragePart register() {
        if (!Config.fluidCells) return null;
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_PART, FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

}
