package com.glodblock.github.common.item;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.common.Config;
import com.glodblock.github.common.storage.CellType;
import com.glodblock.github.common.storage.IFluidCellInventory;
import com.glodblock.github.common.storage.IFluidCellInventoryHandler;
import com.glodblock.github.common.storage.IStorageFluidCell;
import com.glodblock.github.common.tabs.FluidCraftingTabs;
import com.glodblock.github.util.ModAndClassUtil;
import com.glodblock.github.util.NameConst;
import com.google.common.base.Optional;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

public class ItemBasicFluidStorageCell extends AEBaseItem implements IStorageFluidCell
{
    private final CellType component;
    private final int totalBytes;
    private final int perType;
    private final double idleDrain;
    private final static HashMap<Integer, IIcon> icon = new HashMap<>();

    public ItemBasicFluidStorageCell( final CellType whichCell, final int kilobytes )
    {
        super( Optional.of( kilobytes + "k" ) );
        setUnlocalizedName(NameConst.ITEM_FLUID_STORAGE + kilobytes);
        this.setFeature( EnumSet.of( AEFeature.StorageCells ) );
        this.setMaxStackSize( 1 );
        this.totalBytes = kilobytes * 1024;
        this.component = whichCell;

        switch( this.component )
        {
            case Cell1kPart:
                this.idleDrain = 0.5;
                this.perType = 8;
                break;
            case Cell4kPart:
                this.idleDrain = 1.0;
                this.perType = 8;
                break;
            case Cell16kPart:
                this.idleDrain = 1.5;
                this.perType = 8;
                break;
            case Cell64kPart:
                this.idleDrain = 2.0;
                this.perType = 8;
                break;
            case Cell256kPart:
                this.idleDrain = 2.5;
                this.perType = 8;
                break;
            case Cell1024kPart:
                this.idleDrain = 3.0;
                this.perType = 8;
                break;
            case Cell4096kPart:
                this.idleDrain = 3.5;
                this.perType = 8;
                break;
            default:
                this.idleDrain = 0.0;
                this.perType = 8;
        }
    }

    public ItemStack getComponent() {
        return component.stack(1);
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        return StatCollector.translateToLocalFormatted("item.fluid_storage." + this.totalBytes / 1024 + ".name");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister) {
        icon.put(this.totalBytes / 1024, iconRegister.registerIcon(NameConst.RES_KEY + NameConst.ITEM_FLUID_STORAGE + "." + this.totalBytes / 1024));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        int id = this.totalBytes / 1024;
        return icon.get(id);
    }

    @Override
    public void addCheckedInformation(final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
    {
        final IMEInventoryHandler<?> inventory = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.FLUIDS );

        if( inventory instanceof IFluidCellInventoryHandler)
        {
            final IFluidCellInventoryHandler handler = (IFluidCellInventoryHandler) inventory;
            final IFluidCellInventory cellInventory = handler.getCellInv();

            if( cellInventory != null )
            {
                lines.add( cellInventory.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );

                lines.add( cellInventory.getStoredFluidTypes() + " " + GuiText.Of.getLocal() + ' ' + cellInventory.getTotalFluidTypes() + ' ' + GuiText.Types.getLocal() );

                if( handler.isPreformatted() )
                {
                    final String list = (handler.getIncludeExcludeMode() == IncludeExclude.WHITELIST ? GuiText.Included : GuiText.Excluded).getLocal();
                    lines.add(GuiText.Partitioned.getLocal() + " - " + list + ' ' + GuiText.Precise.getLocal());
                }
            }
        }
    }

    @Override
    public int getBytes( final ItemStack cellItem )
    {
        return this.totalBytes;
    }

    @Override
    public int getBytesPerType( final ItemStack cellItem )
    {
        return this.perType;
    }

    @Override
    public boolean isBlackListed(ItemStack cellItem, IAEFluidStack requestedAddition) {
        if (Config.blacklistEssentiaGas && ModAndClassUtil.ThE && requestedAddition != null) {
            return ModAndClassUtil.essentiaGas.isInstance(requestedAddition.getFluid());
        }
        return false;
    }

    @Override
    public int getTotalTypes( final ItemStack cellItem )
    {
        return 1;
    }

    @Override
    public boolean storableInStorageCell()
    {
        return false;
    }

    @Override
    public boolean isStorageCell( final ItemStack i )
    {
        return true;
    }

    @Override
    public double getIdleDrain()
    {
        return this.idleDrain;
    }

    @Override
    public boolean isEditable( final ItemStack is )
    {
        return true;
    }

    @Override
    public IInventory getUpgradesInventory(final ItemStack is )
    {
        return new CellUpgrades( is, 0 );
    }

    @Override
    public IInventory getConfigInventory( final ItemStack is )
    {
        return new CellConfig( is );
    }

    @Override
    public FuzzyMode getFuzzyMode(final ItemStack is )
    {
        final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
        try
        {
            return FuzzyMode.valueOf( fz );
        }
        catch( final Throwable t )
        {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
    {
        Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
    }

    public String getOreFilter(ItemStack is) {
        return Platform.openNbtData( is ).getString( "OreFilter" );
    }

    public void setOreFilter(ItemStack is, String filter) {
        Platform.openNbtData( is ).setString("OreFilter", filter);
    }

    @Override
    public ItemStack onItemRightClick(final ItemStack stack, final World world, final EntityPlayer player )
    {
        this.disassembleDrive( stack, world, player );
        return stack;
    }

    private boolean disassembleDrive( final ItemStack stack, final World world, final EntityPlayer player )
    {
        if( player.isSneaking() )
        {
            if( Platform.isClient() )
            {
                return false;
            }
            final InventoryPlayer playerInventory = player.inventory;
            final IMEInventoryHandler<?> inv = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.FLUIDS );
            if( inv != null && playerInventory.getCurrentItem() == stack )
            {
                final InventoryAdaptor ia = InventoryAdaptor.getAdaptor( player, ForgeDirection.UNKNOWN );
                final IItemList<IAEFluidStack> list = inv.getAvailableItems( StorageChannel.FLUIDS.createList() );
                if( list.isEmpty() && ia != null )
                {
                    playerInventory.setInventorySlotContents( playerInventory.currentItem, null );

                    // drop core
                    final ItemStack extraB = ia.addItems( this.component.stack( 1 ) );
                    if( extraB != null )
                    {
                        player.dropPlayerItemWithRandomChoice( extraB, false );
                    }

                    // drop upgrades
                    final IInventory upgradesInventory = this.getUpgradesInventory( stack );
                    for( int upgradeIndex = 0; upgradeIndex < upgradesInventory.getSizeInventory(); upgradeIndex++ )
                    {
                        final ItemStack upgradeStack = upgradesInventory.getStackInSlot( upgradeIndex );
                        final ItemStack leftStack = ia.addItems( upgradeStack );
                        if( leftStack != null && upgradeStack.getItem() instanceof IUpgradeModule)
                        {
                            player.dropPlayerItemWithRandomChoice( upgradeStack, false );
                        }
                    }

                    // drop empty storage cell case
                    for( final ItemStack storageCellStack : AEApi.instance().definitions().materials().emptyStorageCell().maybeStack( 1 ).asSet() )
                    {
                        final ItemStack extraA = ia.addItems( storageCellStack );
                        if( extraA != null )
                        {
                            player.dropPlayerItemWithRandomChoice( extraA, false );
                        }
                    }

                    if( player.inventoryContainer != null )
                    {
                        player.inventoryContainer.detectAndSendChanges();
                    }

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onItemUseFirst( final ItemStack stack, final EntityPlayer player, final World world, final int x, final int y, final int z, final int side, final float hitX, final float hitY, final float hitZ )
    {
        if( ForgeEventFactory.onItemUseStart( player, stack, 1 ) <= 0 )
            return true;

        return this.disassembleDrive( stack, world, player );
    }

    @Override
    public ItemStack getContainerItem( final ItemStack itemStack )
    {
        for( final ItemStack stack : AEApi.instance().definitions().materials().emptyStorageCell().maybeStack( 1 ).asSet() )
        {
            return stack;
        }

        throw new MissingDefinition( "Tried to use empty storage cells while basic storage cells are defined." );
    }

    @Override
    public boolean hasContainerItem( final ItemStack stack )
    {
        return AEConfig.instance.isFeatureEnabled( AEFeature.EnableDisassemblyCrafting );
    }

    public ItemBasicFluidStorageCell register() {
        if (!Config.fluidCells) return null;
        GameRegistry.registerItem(this, NameConst.ITEM_FLUID_STORAGE + this.totalBytes / 1024 , FluidCraft.MODID);
        setCreativeTab(FluidCraftingTabs.INSTANCE);
        return this;
    }

    public ItemStack stack(int size) {
        return new ItemStack(this, size);
    }

    public ItemStack stack() {
        return new ItemStack(this, 1);
    }

}
