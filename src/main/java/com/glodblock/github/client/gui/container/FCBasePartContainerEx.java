package com.glodblock.github.client.gui.container;

import appeng.api.AEApi;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.ITerminalHost;
import appeng.container.guisync.GuiSync;
import appeng.container.slot.*;
import appeng.helpers.IContainerCraftingPacket;
import appeng.tile.inventory.IAEAppEngInventory;
import appeng.tile.inventory.InvOperation;
import appeng.util.Platform;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.parts.PartFluidPatternTerminalEx;
import com.glodblock.github.util.Util;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class FCBasePartContainerEx extends FCBaseMonitorContain implements IAEAppEngInventory, IOptionalSlotHost, IContainerCraftingPacket {

    private static final int CRAFTING_GRID_PAGES = 2;
    private static final int CRAFTING_GRID_WIDTH = 4;
    private static final int CRAFTING_GRID_HEIGHT = 4;
    private static final int CRAFTING_GRID_SLOTS = CRAFTING_GRID_WIDTH * CRAFTING_GRID_HEIGHT;

    private static class ProcessingSlotFake extends OptionalSlotFake {

        private static final int POSITION_SHIFT = 9000;
        private boolean hidden = false;

        public ProcessingSlotFake(IInventory inv, IOptionalSlotHost containerBus, int idx, int x, int y, int offX, int offY, int groupNum) {
            super(inv, containerBus, idx, x, y, offX, offY, groupNum);
            this.setRenderDisabled(false);
        }

        public void setHidden(boolean hide) {
            if (this.hidden != hide) {
                this.hidden = hide;
                this.xDisplayPosition += (hide ? -1 : 1) * POSITION_SHIFT;
            }
        }
    }

    private final PartFluidPatternTerminalEx patternTerminal;
    protected final ProcessingSlotFake[] craftingSlots = new ProcessingSlotFake[CRAFTING_GRID_SLOTS * CRAFTING_GRID_PAGES];
    protected final ProcessingSlotFake[] outputSlots = new ProcessingSlotFake[CRAFTING_GRID_SLOTS * CRAFTING_GRID_PAGES];
    protected final SlotRestrictedInput patternSlotIN;
    protected final SlotRestrictedInput patternSlotOUT;
    @GuiSync( 96 + (17-9) + 11 )
    public boolean combine = false;
    @GuiSync( 96 + (17-9) + 12 )
    public boolean substitute = false;
    @GuiSync( 96 + (17-9) + 16 )
    public boolean inverted;
    @GuiSync( 96 + (17-9) + 17 )
    public int activePage = 0;

    public FCBasePartContainerEx(final InventoryPlayer ip, final ITerminalHost monitorable )
    {
        super( ip, monitorable, false );
        this.patternTerminal = (PartFluidPatternTerminalEx) monitorable;
        this.inverted = patternTerminal.isInverted();

        final IInventory patternInv = this.getPatternTerminal().getInventoryByName( "pattern" );
        final IInventory output = this.getPatternTerminal().getInventoryByName( "output" );
        IInventory crafting = this.getPatternTerminal().getInventoryByName("crafting");

        for (int page = 0; page < CRAFTING_GRID_PAGES; page++) {
            for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                    this.addSlotToContainer(this.craftingSlots[x + y * CRAFTING_GRID_WIDTH + page * CRAFTING_GRID_SLOTS] = new ProcessingSlotFake(crafting, this, x + y * CRAFTING_GRID_WIDTH + page * CRAFTING_GRID_SLOTS, 15, -83, x, y, x + 4));
                }
            }
            for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                    this.addSlotToContainer(this.outputSlots[x * CRAFTING_GRID_HEIGHT + y + page * CRAFTING_GRID_SLOTS] = new ProcessingSlotFake(output, this, x * CRAFTING_GRID_HEIGHT + y + page * CRAFTING_GRID_SLOTS, 112, -83, -x, y, x));
                }
            }
        }

        this.addSlotToContainer( this.patternSlotIN = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, patternInv, 0, 147, -72 - 9, this.getInventoryPlayer() ) );
        this.addSlotToContainer( this.patternSlotOUT = new SlotRestrictedInput( SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, patternInv, 1, 147, -72 + 34, this.getInventoryPlayer() ) );

        this.patternSlotOUT.setStackLimit( 1 );

        this.bindPlayerInventory( ip, 0, 0 );
    }

    @Override
    public void saveChanges()
    {

    }

    public void clear()
    {
        for( final Slot s : this.craftingSlots )
        {
            s.putStack( null );
        }

        for( final Slot s : this.outputSlots )
        {
            s.putStack( null );
        }

        this.detectAndSendChanges();
    }

    @Override
    public void onChangeInventory(final IInventory inv, final int slot, final InvOperation mc, final ItemStack removedStack, final ItemStack newStack )
    {

    }

    public void encodeAndMoveToInventory()
    {
        encode();
        ItemStack output = this.patternSlotOUT.getStack();
        if ( output != null )
        {
            if (!getPlayerInv().addItemStackToInventory( output )){
                getPlayerInv().player.entityDropItem(output, 0);
            }
            this.patternSlotOUT.putStack( null );
        }
    }

    public void encode()
    {
        ItemStack output = this.patternSlotOUT.getStack();

        final ItemStack[] in = this.getInputs();
        final ItemStack[] out = this.getOutputs();

        // if there is no input, this would be silly.
        if( in == null || out == null )
        {
            return;
        }

        // first check the output slots, should either be null, or a pattern
        if( output != null && !this.isPattern( output ) )
        {
            return;
        }// if nothing is there we should snag a new pattern.
        else if( output == null )
        {
            output = this.patternSlotIN.getStack();
            if (!this.isPattern( output ))
            {
                return; // no blanks.
            }

            // remove one, and clear the input slot.
            output.stackSize--;
            if( output.stackSize == 0 )
            {
                this.patternSlotIN.putStack( null );
            }

            // add a new encoded pattern.
            for( final ItemStack encodedPatternStack : AEApi.instance().definitions().items().encodedPattern().maybeStack( 1 ).asSet() )
            {
                output = encodedPatternStack;
                this.patternSlotOUT.putStack( output );
            }
        } else if ( output.getItem() instanceof ItemFluidEncodedPattern ) {
            for( final ItemStack encodedPatternStack : AEApi.instance().definitions().items().encodedPattern().maybeStack( 1 ).asSet() )
            {
                output = encodedPatternStack;
                this.patternSlotOUT.putStack( output );
            }
        }

        // encode the slot.
        final NBTTagCompound encodedValue = new NBTTagCompound();

        final NBTTagList tagIn = new NBTTagList();
        final NBTTagList tagOut = new NBTTagList();

        for( final ItemStack i : in )
        {
            tagIn.appendTag( this.createItemTag( i ) );
        }

        for( final ItemStack i : out )
        {
            tagOut.appendTag( this.createItemTag( i ) );
        }

        encodedValue.setTag( "in", tagIn );
        encodedValue.setTag( "out", tagOut );
        encodedValue.setBoolean( "substitute", this.isSubstitute() );

        output.setTagCompound( encodedValue );
    }

    private ItemStack[] getInputs()
    {
        final ItemStack[] input = new ItemStack[CRAFTING_GRID_SLOTS * CRAFTING_GRID_PAGES];
        boolean hasValue = false;

        for( int x = 0; x < this.craftingSlots.length; x++ )
        {
            input[x] = this.craftingSlots[x].getStack();
            if( input[x] != null )
            {
                hasValue = true;
            }
        }

        if( hasValue )
        {
            return input;
        }

        return null;
    }

    private ItemStack[] getOutputs()
    {
        final List<ItemStack> list = new ArrayList<>( CRAFTING_GRID_SLOTS * CRAFTING_GRID_PAGES );
        boolean hasValue = false;

        for( final OptionalSlotFake outputSlot : this.outputSlots )
        {
            final ItemStack out = outputSlot.getStack();

            if( out != null && out.stackSize > 0 )
            {
                list.add( out );
                hasValue = true;
            }
        }

        if( hasValue )
        {
            return list.toArray(new ItemStack[0]);
        }

        return null;
    }

    private boolean isPattern( final ItemStack output )
    {
        if( output == null )
        {
            return false;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern) {
            return true;
        }
        final IDefinitions definitions = AEApi.instance().definitions();

        boolean isPattern = definitions.items().encodedPattern().isSameAs( output );
        isPattern |= definitions.materials().blankPattern().isSameAs( output );

        return isPattern;
    }

    private NBTBase createItemTag(final ItemStack i )
    {
        final NBTTagCompound c = new NBTTagCompound();

        if( i != null )
        {
            i.writeToNBT( c );
        }

        return c;
    }

    @Override
    public boolean isSlotEnabled( final int idx )
    {
        if (idx < 4) // outputs
        {
            return inverted || idx == 0;
        }
        else
        {
            return !inverted || idx == 4;
        }
    }

    @Override
    public void detectAndSendChanges()
    {
        super.detectAndSendChanges();
        if( Platform.isServer() )
        {
            this.substitute = this.patternTerminal.isSubstitution();
            this.combine = this.patternTerminal.shouldCombine();
            if (inverted != patternTerminal.isInverted() || activePage != patternTerminal.getActivePage()) {
                inverted = patternTerminal.isInverted();
                activePage = patternTerminal.getActivePage();
                offsetSlots();
            }
        }
    }

    private void offsetSlots()
    {
        for (int page = 0; page < CRAFTING_GRID_PAGES; page++) {
            for (int y = 0; y < CRAFTING_GRID_HEIGHT; y++) {
                for (int x = 0; x < CRAFTING_GRID_WIDTH; x++) {
                    this.craftingSlots[x + y * CRAFTING_GRID_WIDTH + page * CRAFTING_GRID_SLOTS].setHidden(page != activePage || x > 0 && inverted);
                    this.outputSlots[x * CRAFTING_GRID_HEIGHT + y + page * CRAFTING_GRID_SLOTS].setHidden(page != activePage || x > 0 && !inverted);
                }
            }
        }
    }

    @Override
    public void onUpdate(String field, Object oldValue, Object newValue) {
        super.onUpdate( field, oldValue, newValue );

        if (field.equals( "inverted" ) || field.equals( "activePage" )) {
            offsetSlots();
        }
    }

    @Override
    public void onSlotChange( final Slot s )
    {
        if( s == this.patternSlotOUT && Platform.isServer() )
        {
            inverted = patternTerminal.isInverted();

            for( final Object crafter : this.crafters )
            {
                final ICrafting icrafting = (ICrafting) crafter;

                for( final Object g : this.inventorySlots )
                {
                    if( g instanceof OptionalSlotFake )
                    {
                        final Slot sri = (Slot) g;
                        icrafting.sendSlotContents( this, sri.slotNumber, sri.getStack() );
                    }
                }
                ( (EntityPlayerMP) icrafting ).isChangingQuantityOnly = false;
            }
            this.detectAndSendChanges();
        }
    }

    @Override
    public IInventory getInventoryByName( final String name )
    {
        if( name.equals( "player" ) )
        {
            return this.getInventoryPlayer();
        }
        return this.getPatternTerminal().getInventoryByName( name );
    }

    @Override
    public boolean useRealItems()
    {
        return false;
    }

    public PartFluidPatternTerminalEx getPatternTerminal()
    {
        return this.patternTerminal;
    }

    private boolean isSubstitute()
    {
        return this.substitute;
    }

    static boolean canDoubleStacks(SlotFake[] slots)
    {
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        long emptyStots = enabledSlots.stream().filter(s -> s.getStack() == null).count();
        long fullSlots = enabledSlots.stream().filter(s-> s.getStack() != null && s.getStack().stackSize * 2 > 127).count();
        return fullSlots <= emptyStots;
    }

    static void doubleStacksInternal(SlotFake[] slots)
    {
        List<ItemStack> overFlowStacks = new ArrayList<>();
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        for (final Slot s : enabledSlots)
        {
            ItemStack st = s.getStack();
            if (st == null)
                continue;
            if (Util.isFluidPacket(st)) {
                FluidStack fluidStack = ItemFluidPacket.getFluidStack(st);
                if (fluidStack != null) {
                    fluidStack = ItemFluidPacket.getFluidStack(st).copy();
                    if (fluidStack.amount < Integer.MAX_VALUE / 2)
                        fluidStack.amount *= 2;
                }
                s.putStack(ItemFluidPacket.newStack(fluidStack));
            }
            else if (st.stackSize * 2 > 127)
            {
                overFlowStacks.add(st.copy());
            }
            else
            {
                st.stackSize *= 2;
                s.putStack(st);
            }
        }
        Iterator<ItemStack> ow = overFlowStacks.iterator();
        for (final Slot s : enabledSlots) {
            if (!ow.hasNext())
                break;
            if (s.getStack() != null)
                continue;
            s.putStack(ow.next());
        }
        assert !ow.hasNext();
    }

    public void doubleStacks(boolean isShift)
    {
        if (canDoubleStacks(craftingSlots) && canDoubleStacks(outputSlots))
        {
            doubleStacksInternal(this.craftingSlots);
            doubleStacksInternal(this.outputSlots);
            if (isShift && !containsFluid(outputSlots) && !containsFluid(craftingSlots))
            {
                while (canDoubleStacks(craftingSlots) && canDoubleStacks(outputSlots))
                {
                    doubleStacksInternal(this.craftingSlots);
                    doubleStacksInternal(this.outputSlots);
                }
            }
            this.detectAndSendChanges();
        }
    }

    static boolean containsFluid(SlotFake[] slots) {
        List<SlotFake> enabledSlots = Arrays.stream(slots).filter(SlotFake::isEnabled).collect(Collectors.toList());
        long fluid = enabledSlots.stream().filter(s -> Util.isFluidPacket(s.getStack())).count();
        return fluid > 0;
    }

    public void setActivePage(final int activePage) {
        this.activePage = activePage;
    }

    public int getActivePage() {
        return this.activePage;
    }

}
