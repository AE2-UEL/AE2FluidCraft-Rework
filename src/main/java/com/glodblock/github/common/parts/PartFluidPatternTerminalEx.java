package com.glodblock.github.common.parts;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.IGridHost;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.sync.GuiBridge;
import appeng.tile.inventory.AppEngInternalInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import com.glodblock.github.client.textures.FCPartsTexture;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.inventory.InventoryHandler;
import com.glodblock.github.inventory.gui.GuiType;
import com.glodblock.github.util.BlockPos;
import com.glodblock.github.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;

import java.util.List;
import java.util.Objects;

public class PartFluidPatternTerminalEx extends FCBasePart {

    private static final FCPartsTexture FRONT_BRIGHT_ICON = FCPartsTexture.PartFluidPatternTerminal_Bright;
    private static final FCPartsTexture FRONT_DARK_ICON = FCPartsTexture.PartFluidPatternTerminal_Colored;
    private static final FCPartsTexture FRONT_COLORED_ICON = FCPartsTexture.PartFluidPatternTerminal_Dark;

    private final AppEngInternalInventory crafting = new AppEngInternalInventory( this, 32 );
    private final AppEngInternalInventory output = new AppEngInternalInventory( this, 32 );
    private final AppEngInternalInventory pattern = new AppEngInternalInventory( this, 2 );

    private boolean substitute = false;
    private boolean combine = false;
    private boolean inverted = false;
    private int activePage = 0;

    public PartFluidPatternTerminalEx(ItemStack is) {
        super(is, true);
    }

    @Override
    public void getDrops(final List<ItemStack> drops, final boolean wrenched )
    {
        for( final ItemStack is : this.pattern )
        {
            if( is != null )
            {
                drops.add( is );
            }
        }
    }

    @Override
    public boolean onPartActivate(final EntityPlayer player, final Vec3 pos ) {
        TileEntity te = this.getTile();
        BlockPos tePos = new BlockPos(te);
        if (Platform.isWrench(player, player.inventory.getCurrentItem(), tePos.getX(), tePos.getY(), tePos.getZ())) {
            return super.onPartActivate(player, pos);
        }
        if (Platform.isServer()) {
            if (Util.hasPermission(player, SecurityPermissions.INJECT, (IGridHost) this) || Util.hasPermission(player, SecurityPermissions.EXTRACT, (IGridHost) this)) {
                InventoryHandler.openGui(player, te.getWorldObj(), tePos, Objects.requireNonNull(Util.from(getSide())), GuiType.FLUID_PATTERN_TERMINAL_EX);
            }
            else {
                player.addChatComponentMessage(new ChatComponentText("You don't have permission to view."));
            }
        }
        return true;
    }

    @Override
    public void readFromNBT( final NBTTagCompound data )
    {
        super.readFromNBT( data );
        
        this.setSubstitution( data.getBoolean( "substitute" ) );
        this.setCombineMode( data.getBoolean("combine") );
        this.pattern.readFromNBT( data, "pattern" );
        this.output.readFromNBT( data, "outputList" );
        this.crafting.readFromNBT( data, "craftingGrid" );

        this.setSubstitution( data.getBoolean( "substitute" ) );
        this.setInverted( data.getBoolean( "inverted" ) );
        this.setActivePage( data.getInteger( "activePage" ) );
    }

    @Override
    public void writeToNBT( final NBTTagCompound data )
    {
        super.writeToNBT( data );
        
        data.setBoolean( "substitute", this.substitute );
        data.setBoolean( "combine", this.combine );
        this.pattern.writeToNBT( data, "pattern" );
        this.output.writeToNBT( data, "outputList" );
        this.crafting.writeToNBT( data, "craftingGrid" );

        data.setBoolean( "substitute", this.substitute );
        data.setBoolean( "inverted", this.inverted );
        data.setInteger( "activePage", this.activePage );
    }

    @Override
    public GuiBridge getGui(final EntityPlayer p )
    {
        int x = (int) p.posX;
        int y = (int) p.posY;
        int z = (int) p.posZ;
        if( this.getHost().getTile() != null )
        {
            x = this.getTile().xCoord;
            y = this.getTile().yCoord;
            z = this.getTile().zCoord;
        }

        if( GuiBridge.GUI_PATTERN_TERMINAL.hasPermissions( this.getHost().getTile(), x, y, z, this.getSide(), p ) )
        {
            return GuiBridge.GUI_PATTERN_TERMINAL;
        }
        return GuiBridge.GUI_ME;
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
    {
        if (inv == this.pattern && slot == 1) {
            final ItemStack is = inv.getStackInSlot(1);

            if (is != null && is.getItem() instanceof ICraftingPatternItem) {
                final ICraftingPatternItem pattern = (ICraftingPatternItem) is.getItem();
                final ICraftingPatternDetails details = pattern.getPatternForItem(is, this.getHost().getTile().getWorldObj());

                if (details != null) {
                    final IAEItemStack[] inItems = details.getInputs();
                    final IAEItemStack[] outItems = details.getOutputs();
                    int inputsCount = 0;
                    int outputCount = 0;

                    for (int i = 0; i < inItems.length; i++) {
                        if (inItems[i] != null) {
                            inputsCount++;
                        }
                    }

                    for (int i = 0; i < outItems.length; i++) {
                        if (outItems[i] != null) {
                            outputCount++;
                        }
                    }

                    this.setSubstitution(details.canSubstitute());
                    this.setInverted(inputsCount <= 8 && outputCount >= 8);
                    this.setActivePage(0);

                    for (int i = 0; i < this.crafting.getSizeInventory(); i++) {
                        this.crafting.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.output.getSizeInventory(); i++) {
                        this.output.setInventorySlotContents(i, null);
                    }

                    for (int i = 0; i < this.crafting.getSizeInventory() && i < inItems.length; i++) {
                        final IAEItemStack item = inItems[i];
                        if (item != null) {
                            if (item.getItem() instanceof ItemFluidDrop) {
                                ItemStack packet = ItemFluidPacket.newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                this.crafting.setInventorySlotContents(i, packet);
                            } else this.crafting.setInventorySlotContents(i, item.getItemStack());
                        }
                    }

                    if (inverted) {
                        for (int i = 0; i < this.output.getSizeInventory() && i < outItems.length; i++) {
                            final IAEItemStack item = outItems[i];
                            if (item != null) {
                                if (item.getItem() instanceof ItemFluidDrop) {
                                    ItemStack packet = ItemFluidPacket.newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                    this.output.setInventorySlotContents(i, packet);
                                } else this.output.setInventorySlotContents(i, item.getItemStack());
                            }
                        }
                    } else {
                        for (int i = 0; i < outItems.length && i < 8; i++) {
                            final IAEItemStack item = outItems[i];
                            if (item != null) {
                                if (item.getItem() instanceof ItemFluidDrop) {
                                    ItemStack packet = ItemFluidPacket.newStack(ItemFluidDrop.getFluidStack(item.getItemStack()));
                                    this.output.setInventorySlotContents(i >= 4 ? 12 + i : i, packet);
                                } else
                                    this.output.setInventorySlotContents(i >= 4 ? 12 + i : i, item.getItemStack());
                            }
                        }
                    }
                }
            }
        }
        this.getHost().markForSave();
    }

    public boolean shouldCombine()
    {
        return this.combine;
    }

    public void setCombineMode(boolean shouldCombine)
    {
        this.combine = shouldCombine;
    }

    public boolean isSubstitution()
    {
        return this.substitute;
    }

    public void setSubstitution( boolean canSubstitute )
    {
        this.substitute = canSubstitute;
    }

    public void onChangeCrafting(IAEItemStack[] newCrafting, IAEItemStack[] newOutput) {
        IInventory crafting = this.getInventoryByName("crafting");
        IInventory output = this.getInventoryByName("output");
        if (crafting instanceof AppEngInternalInventory && output instanceof AppEngInternalInventory) {
            for (int x = 0; x < crafting.getSizeInventory() && x < newCrafting.length; x++) {
                final IAEItemStack item = newCrafting[x];
                crafting.setInventorySlotContents(x, item == null ? null: item.getItemStack());
            }
            for (int x = 0; x < output.getSizeInventory() && x < newOutput.length; x++) {
                final IAEItemStack item = newOutput[x];
                output.setInventorySlotContents(x, item == null ? null : item.getItemStack());
            }
        }
    }

    @Override
    public IInventory getInventoryByName( final String name )
    {
        if( name.equals( "crafting" ) )
        {
            return this.crafting;
        }

        if( name.equals( "output" ) )
        {
            return this.output;
        }

        if( name.equals( "pattern" ) )
        {
            return this.pattern;
        }

        return super.getInventoryByName( name );
    }

    @Override
    public FCPartsTexture getFrontBright() {
        return FRONT_BRIGHT_ICON;
    }

    @Override
    public FCPartsTexture getFrontColored() {
        return FRONT_COLORED_ICON;
    }

    @Override
    public FCPartsTexture getFrontDark() {
        return FRONT_DARK_ICON;
    }

    @Override
    public boolean isLightSource() {
        return false;
    }

    public boolean isInverted() {
        return inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
    }

    public int getActivePage() {
        return this.activePage;
    }

    public void setActivePage(int activePage) {
        this.activePage = activePage;
    }

}
