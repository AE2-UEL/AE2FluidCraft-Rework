package com.glodblock.github.common.storage;

import appeng.core.features.AEFeature;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;

public enum CellType {

    Cell1kPart( 0, AEFeature.StorageCells ),
    Cell4kPart( 1, AEFeature.StorageCells ),
    Cell16kPart( 2, AEFeature.StorageCells ),
    Cell64kPart( 3, AEFeature.StorageCells ),
    Cell256kPart( 4, AEFeature.StorageCells ),
    Cell1024kPart( 5, AEFeature.StorageCells ),
    Cell4096kPart( 6, AEFeature.StorageCells );

    private final EnumSet<AEFeature> features;
    private int damageValue;
    private Item itemInstance;

    CellType( final int metaValue, final AEFeature part )
    {
        this.setDamageValue( metaValue );
        this.features = EnumSet.of( part );
    }

    void setDamageValue( final int damageValue )
    {
        this.damageValue = damageValue;
    }

    public int getDamageValue()
    {
        return this.damageValue;
    }

    EnumSet<AEFeature> getFeature()
    {
        return this.features;
    }

    public ItemStack stack(final int size )
    {
        return new ItemStack( this.getItemInstance(), size, this.getDamageValue() );
    }

    public Item getItemInstance()
    {
        return this.itemInstance;
    }

    public void setItemInstance( final Item itemInstance )
    {
        this.itemInstance = itemInstance;
    }

}
