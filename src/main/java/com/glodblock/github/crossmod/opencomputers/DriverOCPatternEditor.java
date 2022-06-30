package com.glodblock.github.crossmod.opencomputers;

import appeng.items.misc.ItemEncodedPattern;
import appeng.util.item.AEItemStack;
import com.glodblock.github.common.item.ItemFluidDrop;
import com.glodblock.github.common.item.ItemFluidEncodedPattern;
import com.glodblock.github.common.tile.TileOCPatternEditor;
import com.glodblock.github.loader.ItemAndBlockHolder;
import com.glodblock.github.util.NameConst;
import com.glodblock.github.util.Util;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.prefab.DriverSidedTileEntity;
import li.cil.oc.integration.ManagedTileEntityEnvironment;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

public class DriverOCPatternEditor extends DriverSidedTileEntity {

    public DriverOCPatternEditor() {
    }

    @Override
    public Class<?> getTileEntityClass() {
        return TileOCPatternEditor.class;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, int x, int y, int z, ForgeDirection forgeDirection) {
        return new DriverOCPatternEditor.Environment((TileOCPatternEditor) world.getTileEntity(x, y, z));
    }

    public static class Environment extends ManagedTileEntityEnvironment<TileOCPatternEditor>
        implements NamedBlock {

        public Environment(TileOCPatternEditor tileEntity) {
            super(tileEntity, NameConst.BLOCK_OC_PATTERN_EDITOR);
        }

        private int optSlot(Arguments args, IInventory inv, int index, int def) {
            if (index >= 0 && index < args.count()) {
                int slot = args.checkInteger(index) - 1;
                if (slot < 0 || slot >= inv.getSizeInventory()) {
                    throw new IllegalArgumentException("invalid slot");
                }
                return slot;
            }
            else {
                return def;
            }
        }

        private int checkSlot(Arguments args, IInventory inv, int index) {
            if (index >= 0 && index < args.count()) {
                int slot = args.checkInteger(index) - 1;
                if (slot < 0 || slot >= inv.getSizeInventory()) {
                    throw new IllegalArgumentException("invalid slot");
                }
                return slot;
            }
            else {
                throw new IllegalArgumentException("invalid slot");
            }
        }

        private boolean validPattern(ItemStack pattern) {
            if (pattern == null) {
                throw new IllegalArgumentException("No pattern here!");
            }
            if (pattern.getItem() instanceof ItemEncodedPattern && !(pattern.getItem() instanceof ItemFluidEncodedPattern)) {
                ItemEncodedPattern iep = (ItemEncodedPattern) pattern.getItem();
                return iep.getOutput(pattern) == null;
            }
            return true;
        }

        private ItemStack getStack(Arguments args) {
            if (args.count() > 1) {
                String address;
                int entry;
                int size;
                if (args.isString(0)) {
                    address = args.checkString(0);
                    entry = args.checkInteger(1);
                    size = args.optInteger(2, 1);
                }
                else {
                    address = args.checkString(1);
                    entry = args.checkInteger(2);
                    size = args.optInteger(3, 1);
                }
                Node node = node().network().node(address);
                if (node instanceof Component) {
                    if (node.host() instanceof Database) {
                        ItemStack dbStack = ((Database) node.host()).getStackInSlot(entry - 1);
                        if (dbStack == null || size < 1) {
                            return null;
                        }
                        else {
                            dbStack.stackSize = Math.min(size, dbStack.getMaxStackSize());
                            return dbStack;
                        }
                    }
                    else {
                        throw new IllegalArgumentException("not a database");
                    }
                }
                else {
                    throw new IllegalArgumentException("no such component");
                }
            }
            else {
                return null;
            }
        }

        private Object[] clearInterfacePattern(Context context, Arguments args, String tag) {
            IInventory inv = tileEntity.getInternalInventory();
            ItemStack pattern = inv.getStackInSlot(checkSlot(args, inv, 0));
            if (validPattern(pattern)) {
                NBTTagCompound encodedValue = pattern.getTagCompound();
                int slot = checkSlot(args, inv, 0);
                if (encodedValue == null)
                    throw new IllegalArgumentException("No pattern here!");
                NBTTagList nbt = encodedValue.getTagList(tag, 10);
                int index = args.checkInteger(1) - 1;
                if (index < 0)
                    throw new IllegalArgumentException("Invalid index!");
                nbt.removeTag(index);
                encodedValue.setTag(tag, nbt);
                pattern.setTagCompound(encodedValue);
                inv.setInventorySlotContents(slot, null);
                inv.setInventorySlotContents(slot, pattern);
                context.pause(0.1);
                return new Object[] {true};
            } else {
                throw new IllegalArgumentException("Not Fluid Encoded pattern!");
            }
        }

        private Object[] setPatternSlot(Context context, Arguments args, String tag, boolean fluid) {
            IInventory inv = tileEntity.getInternalInventory();
            int slot = args.isString(0) ? 0 : optSlot(args, inv, 0, 0);
            ItemStack stack = getStack(args);
            if (fluid) {
                FluidStack fluids = Util.getFluidFromItem(stack);
                stack = ItemFluidDrop.newStack(fluids);
            }
            int index = args.checkInteger(4) - 1;
            if (index < 0 || index > 511)
                throw new IllegalArgumentException("Invalid index!");
            ItemStack pattern = inv.getStackInSlot(slot);
            if (validPattern(pattern)) {
                NBTTagCompound encodedValue = pattern.getTagCompound();
                if (encodedValue == null)
                    throw new IllegalArgumentException("No pattern here!");

                NBTTagList inTag = encodedValue.getTagList(tag, 10);
                while (inTag.tagCount() <= index)
                    inTag.appendTag(new NBTTagCompound());
                if (stack != null) {
                    NBTTagCompound nbt = new NBTTagCompound();
                    AEItemStack aeStack = AEItemStack.create(stack);
                    aeStack.writeToNBT(nbt);
                    inTag.func_150304_a(index, nbt);
                }
                else {
                    inTag.removeTag(index);
                }
                encodedValue.setTag(tag, inTag);
                pattern.setTagCompound(encodedValue);
                inv.setInventorySlotContents(slot, null);
                inv.setInventorySlotContents(slot, pattern);
                context.pause(0.1);
                return new Object[] {true};
            } else {
                throw new IllegalArgumentException("Not Fluid Encoded pattern!");
            }
        }

        @Callback(doc = "function([slot:number]):table -- Get the configuration of the interface.")
        public Object[] getInterfaceConfiguration(Context context, Arguments args) {
            IInventory config = tileEntity.getInternalInventory();
            int slot = optSlot(args, config, 0, 0);
            ItemStack stack  = config.getStackInSlot(slot);
            if (!validPattern(stack )) {
                throw new IllegalArgumentException("Not Fluid Encoded pattern!");
            }
            return new Object[]{stack};
        }

        @Callback(doc = "function([slot:number][, database:address, entry:number[, size:number]]):boolean -- Configure the interface.")
        public Object[] setInterfaceConfiguration(Context context, Arguments args) {
            IInventory config = tileEntity.getInternalInventory();
            int slot = args.isString(0) ? 0 : optSlot(args, config, 0, 0);
            config.setInventorySlotContents(slot, getStack(args));
            context.pause(0.5);
            return new Object[] {true};
        }

        @Callback(doc = "function([slot:number]):table -- Get the given pattern in the interface.")
        public Object[] getInterfacePattern(Context context, Arguments args) {
            IInventory inv = tileEntity.getInternalInventory();
            int slot = optSlot(args, inv, 0, 0);
            ItemStack stack = inv.getStackInSlot(slot);
            return new Object[] {stack};
        }

        @Callback(doc = "function(slot:number, database:address, entry:number, size:number, index:number):boolean -- Set the pattern item input at the given index.")
        public Object[] setInterfacePatternItemInput(Context context, Arguments args) {
            return setPatternSlot(context, args, "in", false);
        }

        @Callback(doc = "function(slot:number, database:address, entry:number, size:number, index:number):boolean -- Set the pattern item output at the given index.")
        public Object[] setInterfacePatternItemOutput(Context context, Arguments args) {
            return setPatternSlot(context, args, "out", false);
        }

        @Callback(doc = "function(slot:number, database:address, entry:number, size:number, index:number):boolean -- Set the pattern fluid input at the given index.")
        public Object[] setInterfacePatternFluidInput(Context context, Arguments args) {
            return setPatternSlot(context, args, "in", true);
        }

        @Callback(doc = "function(slot:number, database:address, entry:number, size:number, index:number):boolean -- Set the pattern fluid output at the given index.")
        public Object[] setInterfacePatternFluidOutput(Context context, Arguments args) {
            return setPatternSlot(context, args, "out", true);
        }

        @Callback(doc = "function(slot:number, index:number):boolean -- Clear pattern input at the given index.")
        public Object[] clearInterfacePatternInput(Context context, Arguments args) {
            return clearInterfacePattern(context, args, "in");
        }

        @Callback(doc = "function(slot:number, index:number):boolean -- Clear pattern output at the given index.")
        public Object[] clearInterfacePatternOutput(Context context, Arguments args) {
            return clearInterfacePattern(context, args, "out");
        }

        @Override
        public String preferredName() {
            return NameConst.BLOCK_OC_PATTERN_EDITOR;
        }

        @Override
        public int priority() {
            return 5;
        }
    }

    public static class Provider implements EnvironmentProvider {
        Provider() {
        }

        @Override
        public Class<?> getEnvironment(ItemStack itemStack) {
            if (itemStack != null && itemStack.isItemEqual(ItemAndBlockHolder.OC_EDITOR.stack())) {
                return DriverOCPatternEditor.Environment.class;
            }
            return null;
        }
    }

}
