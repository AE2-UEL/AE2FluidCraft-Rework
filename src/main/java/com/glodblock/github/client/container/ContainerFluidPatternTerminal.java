package com.glodblock.github.client.container;

import appeng.api.config.Actionable;
import appeng.api.crafting.ICraftingHelper;
import appeng.api.definitions.IDefinitions;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.container.SlotSemantic;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.container.me.items.ItemTerminalContainer;
import appeng.container.slot.*;
import appeng.core.Api;
import appeng.core.sync.packets.PatternSlotPacket;
import appeng.helpers.IContainerCraftingPacket;
import appeng.helpers.InventoryAction;
import appeng.items.storage.ViewCellItem;
import appeng.me.helpers.MachineSource;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.item.ItemFluidPacket;
import com.glodblock.github.common.part.PartFluidPatternTerminal;
import com.glodblock.github.interfaces.ConfigData;
import com.glodblock.github.interfaces.PatternConsumer;
import com.glodblock.github.loader.FCItems;
import com.glodblock.github.util.ConfigSet;
import com.glodblock.github.util.FCUtil;
import com.glodblock.github.util.HashUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.inventory.container.IContainerListener;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.PlayerInvWrapper;

import javax.annotation.Nonnull;

public class ContainerFluidPatternTerminal extends ItemTerminalContainer implements IOptionalSlotHost, IContainerCraftingPacket, PatternConsumer, ConfigData {

    private final PartFluidPatternTerminal patternTerminal;
    private final IItemHandler craftingGridInv;
    private final FakeCraftingMatrixSlot[] craftingGridSlots = new FakeCraftingMatrixSlot[9];
    private final OptionalFakeSlot[] processingOutputSlots = new OptionalFakeSlot[3];
    private final PatternTermSlot craftOutputSlot;
    private final RestrictedInputSlot blankPatternSlot;
    private final RestrictedInputSlot encodedPatternSlot;
    private final ICraftingHelper craftingHelper;
    private final ConfigSet config = new ConfigSet();
    private ICraftingRecipe currentRecipe;
    // Fix lag from ae's dumb fuck code
    private static final Cache<HashUtil.ItemHandlerHash, ICraftingRecipe> cache = CacheBuilder.newBuilder()
            .initialCapacity(10)
            .maximumSize(120)
            .concurrencyLevel(10)
            .build();
    private boolean currentRecipeCraftingMode;
    public static ContainerType<ContainerFluidPatternTerminal> TYPE = ContainerTypeBuilder
            .create(ContainerFluidPatternTerminal::new, ITerminalHost.class)
            .build("fluid_pattern_terminal");
    @GuiSync(97)
    public boolean craftingMode;
    @GuiSync(96)
    public boolean substitute;
    @GuiSync(95)
    public boolean combine;
    @GuiSync(94)
    public boolean fluidFirst;

    public ContainerFluidPatternTerminal(int id, PlayerInventory ip, ITerminalHost monitorable) {
        super(TYPE, id, ip, monitorable, false);
        this.craftingHelper = Api.INSTANCE.crafting();
        this.craftingMode = true;
        this.substitute = false;
        this.patternTerminal = (PartFluidPatternTerminal) monitorable;
        IItemHandler patternInv = this.patternTerminal.getInventoryByName("pattern");
        IItemHandler output = this.patternTerminal.getInventoryByName("output");
        this.craftingGridInv = this.patternTerminal.getInventoryByName("crafting");
        int i;
        for(i = 0; i < 9; ++i) {
            this.addSlot(this.craftingGridSlots[i] = new FakeCraftingMatrixSlot(this.craftingGridInv, i), SlotSemantic.CRAFTING_GRID);
        }

        this.addSlot(this.craftOutputSlot = new PatternTermSlot(ip.player, this.getActionSource(), this.powerSource, monitorable, this.craftingGridInv, patternInv, this, 2, this), SlotSemantic.CRAFTING_RESULT);
        this.craftOutputSlot.setIcon(null);

        for(i = 0; i < 3; ++i) {
            this.addSlot(this.processingOutputSlots[i] = new PatternOutputsSlot(output, this, i, 1), SlotSemantic.PROCESSING_RESULT);
            this.processingOutputSlots[i].setRenderDisabled(false);
            this.processingOutputSlots[i].setIcon(null);
        }

        this.addSlot(this.blankPatternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.BLANK_PATTERN, patternInv, 0), SlotSemantic.BLANK_PATTERN);
        this.addSlot(this.encodedPatternSlot = new RestrictedInputSlot(RestrictedInputSlot.PlacableItemType.ENCODED_PATTERN, patternInv, 1), SlotSemantic.ENCODED_PATTERN);
        this.encodedPatternSlot.setStackLimit(1);
        this.createPlayerInventorySlots(ip);
        this.config
                .addConfig("combine", v -> {
                    this.combine = (boolean) v;
                    this.patternTerminal.setCombineMode((boolean) v);
                }, () -> this.combine)
                .addConfig("fluidFirst", v -> {
                    this.fluidFirst = (boolean) v;
                    this.patternTerminal.setFluidPlaceMode((boolean) v);
                }, () -> this.fluidFirst)
                .addConfig("craft", v -> {
                    this.craftingMode = (boolean) v;
                    this.patternTerminal.setCraftingRecipe((boolean) v);
                }, () -> this.craftingMode)
                .addConfig("substitute", v -> {
                    this.substitute = (boolean) v;
                    this.patternTerminal.setSubstitution((boolean) v);
                }, () -> this.substitute)
                .addConfig("encode", v -> this.encode(),
                        () -> {throw new IllegalArgumentException("Doesn't support operation!");})
                .addConfig("clear", v -> this.clear(),
                        () -> {throw new IllegalArgumentException("Doesn't support operation!");});

    }

    @Override
    public void putStackInSlot(int slotID, @Nonnull ItemStack stack) {
        super.putStackInSlot(slotID, stack);
        if (this.getSlot(slotID) instanceof FakeCraftingMatrixSlot) {
            this.getAndUpdateOutput();
        }
    }

    private ICraftingRecipe lookupRecipe(World world, CraftingInventory ic) {
        HashUtil.ItemHandlerHash hash = HashUtil.hashItemHandler(this.craftingGridInv);
        try {
            ICraftingRecipe recipe = cache.get(hash, () -> world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, world).orElse(NullRecipe.NULL));
            return recipe == NullRecipe.NULL ? null : recipe;
        } catch (Exception ignored) {
        }
        return null;
    }

    private ItemStack getAndUpdateOutput() {
        final World world = this.getPlayerInventory().player.world;
        final CraftingInventory ic = new CraftingInventory(this, 3, 3);

        for (int x = 0; x < ic.getSizeInventory(); x++) {
            ic.setInventorySlotContents(x, this.craftingGridInv.getStackInSlot(x));
        }

        if (this.currentRecipe == null || !this.currentRecipe.matches(ic, world)) {
            this.currentRecipe = lookupRecipe(world, ic);
            this.currentRecipeCraftingMode = this.craftingMode;
        }

        final ItemStack is;

        if (this.currentRecipe == null) {
            is = ItemStack.EMPTY;
        } else {
            is = this.currentRecipe.getCraftingResult(ic);
        }

        this.craftOutputSlot.setDisplayedCraftingOutput(is);
        return is;
    }

    public void encode() {
        if (checkHasFluidPattern()) {
            this.encodeFluidPattern();
        } else {
            this.encodeItemPattern();
        }
    }

    public void encodeItemPattern() {
        ItemStack output = this.encodedPatternSlot.getStack();
        ItemStack[] in = this.getInputs();
        ItemStack[] out = this.getOutputs();
        if (in != null && out != null && (!this.isCraftingMode() || this.currentRecipe != null)) {
            if (output.isEmpty() || this.craftingHelper.isEncodedPattern(output)) {
                if (output.isEmpty()) {
                    output = this.blankPatternSlot.getStack();
                    if (output.isEmpty() || !isPattern(output)) {
                        return;
                    }
                    output.setCount(output.getCount() - 1);
                    if (output.getCount() == 0) {
                        this.blankPatternSlot.putStack(ItemStack.EMPTY);
                    }
                    output = null;
                } else if (output.getItem() instanceof ItemFluidEncodedPattern) {
                    output = null;
                }
                if (this.isCraftingMode()) {
                    output = this.craftingHelper.encodeCraftingPattern(output, this.currentRecipe, in, out[0], this.isSubstitute());
                } else {
                    output = this.craftingHelper.encodeProcessingPattern(output, in, out);
                }
                this.encodedPatternSlot.putStack(output);
            }
        }
    }

    private void encodeFluidPattern() {
        ItemStack output = this.encodedPatternSlot.getStack();
        ItemStack[] in = this.getInputs();
        ItemStack[] out = this.getOutputs();
        if (in != null && out != null && (!this.isCraftingMode() || this.currentRecipe != null)) {
            if (output.isEmpty() || this.craftingHelper.isEncodedPattern(output)) {
                if (output.isEmpty()) {
                    output = this.blankPatternSlot.getStack();
                    if (output.isEmpty() || !isPattern(output)) {
                        return;
                    }
                    output.setCount(output.getCount() - 1);
                    if (output.getCount() == 0) {
                        this.blankPatternSlot.putStack(ItemStack.EMPTY);
                    }
                }
                output = FCItems.DENSE_ENCODED_PATTERN.encodeStack(in, out);
                this.encodedPatternSlot.putStack(output);
            }
        }
    }

    private static boolean isPattern(final ItemStack output) {
        if (output.isEmpty()) {
            return false;
        }
        if (output.getItem() instanceof ItemFluidEncodedPattern) {
            return true;
        }
        final IDefinitions defs = Api.instance().definitions();
        return defs.items().encodedPattern().isSameAs(output) || defs.materials().blankPattern().isSameAs(output);
    }

    private boolean checkHasFluidPattern() {
        if (this.craftingMode) {
            return false;
        }
        boolean hasFluid = false, search = false;
        for (Slot craftingSlot : this.craftingGridSlots) {
            final ItemStack crafting = craftingSlot.getStack();
            if (crafting.isEmpty()) {
                continue;
            }
            search = true;
            if (crafting.getItem() instanceof ItemFluidPacket) {
                hasFluid = true;
                break;
            }
        }
        if (!search) { // search=false -> inputs were empty
            return false;
        }
        // `search` should be true at this point
        for (Slot outputSlot : this.processingOutputSlots) {
            final ItemStack out = outputSlot.getStack();
            if (out.isEmpty()) {
                continue;
            }
            search = false;
            if (hasFluid) {
                break;
            } else if (out.getItem() instanceof ItemFluidPacket) {
                hasFluid = true;
                break;
            }
        }
        return hasFluid && !search; // search=true -> outputs were empty
    }

    private ItemStack[] getInputs() {
        ItemStack[] input = new ItemStack[9];
        boolean hasValue = false;
        for(int x = 0; x < this.craftingGridSlots.length; ++x) {
            input[x] = this.craftingGridSlots[x].getStack();
            if (!input[x].isEmpty()) {
                hasValue = true;
            }
        }
        return hasValue ? input : null;
    }

    private ItemStack[] getOutputs() {
        if (this.isCraftingMode()) {
            ItemStack out = this.getAndUpdateOutput();
            if (!out.isEmpty() && out.getCount() > 0) {
                return new ItemStack[] {out};
            }
        } else {
            boolean hasValue = false;
            ItemStack[] list = new ItemStack[3];
            for(int i = 0; i < this.processingOutputSlots.length; ++i) {
                ItemStack out = this.processingOutputSlots[i].getStack();
                list[i] = out;
                if (!out.isEmpty()) {
                    hasValue = true;
                }
            }
            if (hasValue) {
                return list;
            }
        }
        return null;
    }

    public boolean isSlotEnabled(int idx) {
        if (idx == 1) {
            return this.isServer() ? !this.patternTerminal.isCraftingRecipe() : !this.isCraftingMode();
        } else if (idx == 2) {
            return this.isServer() ? this.patternTerminal.isCraftingRecipe() : this.isCraftingMode();
        } else {
            return false;
        }
    }

    public void craftOrGetItem(PatternSlotPacket packetPatternSlot) {
        if (packetPatternSlot.slotItem != null && this.monitor != null) {
            IAEItemStack out = packetPatternSlot.slotItem.copy();
            InventoryAdaptor inv = new AdaptorItemHandler(new WrapperCursorItemHandler(this.getPlayerInventory().player.inventory));
            InventoryAdaptor playerInv = InventoryAdaptor.getAdaptor(this.getPlayerInventory().player);
            if (packetPatternSlot.shift) {
                inv = playerInv;
            }

            if (!inv.simulateAdd(out.createItemStack()).isEmpty()) {
                return;
            }

            IAEItemStack extracted = Platform.poweredExtraction(this.powerSource, this.monitor, out, this.getActionSource());
            PlayerEntity p = this.getPlayerInventory().player;
            if (extracted != null) {
                inv.addItems(extracted.createItemStack());
                if (p instanceof ServerPlayerEntity) {
                    this.updateHeld((ServerPlayerEntity)p);
                }

                this.detectAndSendChanges();
                return;
            }

            CraftingInventory ic = new CraftingInventory(new ContainerNull(), 3, 3);
            CraftingInventory real = new CraftingInventory(new ContainerNull(), 3, 3);

            for(int x = 0; x < 9; ++x) {
                ic.setInventorySlotContents(x, packetPatternSlot.pattern[x] == null ? ItemStack.EMPTY : packetPatternSlot.pattern[x].createItemStack());
            }

            IRecipe<CraftingInventory> r = p.world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, ic, p.world).orElse(null);
            if (r == null) {
                return;
            }

            IMEMonitor<IAEItemStack> storage = this.patternTerminal.getInventory(Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
            IItemList<IAEItemStack> all = storage.getStorageList();
            ItemStack is = r.getCraftingResult(ic);

            for(int x = 0; x < ic.getSizeInventory(); ++x) {
                if (!ic.getStackInSlot(x).isEmpty()) {
                    ItemStack pulled = Platform.extractItemsByRecipe(this.powerSource, this.getActionSource(), storage, p.world, r, is, ic, ic.getStackInSlot(x), x, all, Actionable.MODULATE, ViewCellItem.createFilter(this.getViewCells()));
                    real.setInventorySlotContents(x, pulled);
                }
            }

            IRecipe<CraftingInventory> rr = p.world.getRecipeManager().getRecipe(IRecipeType.CRAFTING, real, p.world).orElse(null);
            if (rr == r && Platform.itemComparisons().isSameItem(rr.getCraftingResult(real), is)) {
                CraftResultInventory craftingResult = new CraftResultInventory();
                craftingResult.setRecipeUsed(rr);
                CraftingResultSlot sc = new CraftingResultSlot(p, real, craftingResult, 0, 0, 0);
                sc.onTake(p, is);

                for(int x = 0; x < real.getSizeInventory(); ++x) {
                    ItemStack failed = playerInv.addItems(real.getStackInSlot(x));
                    if (!failed.isEmpty()) {
                        p.dropItem(failed, false);
                    }
                }

                inv.addItems(is);
                if (p instanceof ServerPlayerEntity) {
                    this.updateHeld((ServerPlayerEntity)p);
                }

                this.detectAndSendChanges();
            } else {
                for(int x = 0; x < real.getSizeInventory(); ++x) {
                    ItemStack failed = real.getStackInSlot(x);
                    if (!failed.isEmpty()) {
                        this.monitor.injectItems(AEItemStack.fromItemStack(failed), Actionable.MODULATE, new MachineSource(this.patternTerminal));
                    }
                }
            }
        }
    }

    public boolean isCraftingMode() {
        return this.craftingMode;
    }

    @Override
    public void acceptPattern(Int2ObjectMap<ItemStack[]> inputs, ItemStack[] outputs, boolean combine) {
        this.patternTerminal.onChangeCrafting(inputs, outputs, combine);
    }

    @Override
    public void doAction(ServerPlayerEntity player, InventoryAction action, int slotId, long id) {
        if (this.isCraftingMode()) {
            super.doAction(player, action, slotId, id);
            return;
        }
        if (slotId < 0 || slotId >= this.inventorySlots.size()) {
            super.doAction(player, action, slotId, id);
            return;
        }
        Slot slot = getSlot(slotId);
        ItemStack stack = player.inventory.getItemStack();
        if ((slot instanceof FakeCraftingMatrixSlot || slot instanceof PatternOutputsSlot) && !stack.isEmpty()
                && stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).isPresent() && !FCUtil.getFluidFromItem(stack).isEmpty()) {
            FluidStack fluid = FluidStack.EMPTY;
            switch (action) {
                case PICKUP_OR_SET_DOWN:
                    fluid = FCUtil.getFluidFromItem(stack);
                    slot.putStack(ItemFluidPacket.newStack(fluid));
                    break;
                case SPLIT_OR_PLACE_SINGLE:
                    fluid = FCUtil.getFluidFromItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
                    FluidStack origin = ItemFluidPacket.getFluidStack(slot.getStack());
                    if (!fluid.isEmpty() && fluid.equals(origin)) {
                        fluid.grow(origin.getAmount());
                        if (fluid.getAmount() <= 0)
                            fluid = FluidStack.EMPTY;
                    }
                    slot.putStack(ItemFluidPacket.newStack(fluid));
                    break;
            }
            if (fluid.isEmpty()) {
                super.doAction(player, action, slotId, id);
                return;
            }
            return;
        }
        if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE) {
            if (stack.isEmpty() && !slot.getStack().isEmpty() && slot.getStack().getItem() instanceof ItemFluidPacket) {
                FluidStack fluid = ItemFluidPacket.getFluidStack(slot.getStack());
                if (!fluid.isEmpty() && fluid.getAmount() - 1000 >= 1) {
                    fluid.shrink(1000);
                    slot.putStack(ItemFluidPacket.newStack(fluid));
                }
            }
        }
        super.doAction(player, action, slotId, id);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (Platform.isServer()) {
            if (this.isCraftingMode() != this.patternTerminal.isCraftingRecipe()) {
                this.setCraftingMode(this.patternTerminal.isCraftingRecipe());
            }
            if (this.substitute != this.patternTerminal.isSubstitution()) {
                this.substitute = this.patternTerminal.isSubstitution();
            }
            if (this.combine != this.patternTerminal.getCombineMode()) {
                this.combine = this.patternTerminal.getCombineMode();
            }
            if (this.fluidFirst != this.patternTerminal.getFluidPlaceMode()) {
                this.fluidFirst = this.patternTerminal.getFluidPlaceMode();
            }
        }
    }

    @Override
    public void onServerDataSync() {
        super.onServerDataSync();
        if (this.currentRecipeCraftingMode != this.craftingMode) {
            this.getAndUpdateOutput();
        }
    }

    @Override
    public void onSlotChange(Slot s) {
        if (s == this.encodedPatternSlot && isServer()) {
            for (final IContainerListener listener : this.listeners) {
                for (final Slot slot : this.inventorySlots) {
                    if (slot instanceof OptionalFakeSlot || slot instanceof FakeCraftingMatrixSlot) {
                        listener.sendSlotContents(this, slot.slotNumber, slot.getStack());
                    }
                }
                if (listener instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) listener).isChangingQuantityOnly = false;
                }
            }
            this.detectAndSendChanges();
        }

        if (s == this.craftOutputSlot && isClient()) {
            this.getAndUpdateOutput();
        }
    }

    public void clear() {
        for (final Slot s : this.craftingGridSlots) {
            s.putStack(ItemStack.EMPTY);
        }

        for (final Slot s : this.processingOutputSlots) {
            s.putStack(ItemStack.EMPTY);
        }

        this.detectAndSendChanges();
        this.getAndUpdateOutput();
    }

    @Override
    public IItemHandler getInventoryByName(final String name) {
        if (name.equals("player")) {
            return new PlayerInvWrapper(this.getPlayerInventory());
        }
        return this.patternTerminal.getInventoryByName(name);
    }

    @Override
    public boolean useRealItems() {
        return false;
    }

    private boolean isSubstitute() {
        return this.substitute;
    }

    private void setCraftingMode(boolean craftingMode) {
        this.craftingMode = craftingMode;
    }

    public void setSubstitute(boolean substitute) {
        this.substitute = substitute;
    }

    public FakeCraftingMatrixSlot[] getCraftingGridSlots() {
        return this.craftingGridSlots;
    }

    public OptionalFakeSlot[] getProcessingOutputSlots() {
        return this.processingOutputSlots;
    }

    public PatternTermSlot getCraftOutputSlot() {
        return this.craftOutputSlot;
    }

    public PartFluidPatternTerminal getPart() {
        return this.patternTerminal;
    }

    @Override
    public void set(String id, Object value) {
        this.config.setConfig(id, value);
    }

    @Override
    public Object get(String id) {
        return this.config.getConfig(id);
    }

    @SuppressWarnings("all")
    private static final class NullRecipe implements ICraftingRecipe {

        private static final NullRecipe NULL = new NullRecipe();

        @Override
        public boolean matches(CraftingInventory inv, World worldIn) {
            return false;
        }

        @Override
        public ItemStack getCraftingResult(CraftingInventory inv) {
            return null;
        }

        @Override
        public boolean canFit(int width, int height) {
            return false;
        }

        @Override
        public ItemStack getRecipeOutput() {
            return null;
        }

        @Override
        public ResourceLocation getId() {
            return null;
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return null;
        }
    }
}
