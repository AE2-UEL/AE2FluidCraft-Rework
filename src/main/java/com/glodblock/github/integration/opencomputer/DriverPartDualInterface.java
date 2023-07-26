package com.glodblock.github.integration.opencomputer;

import appeng.api.implementations.tiles.ISegmentedInventory;
import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import appeng.tile.networking.TileCableBus;
import com.glodblock.github.common.part.PartDualInterface;
import com.glodblock.github.loader.FCItems;
import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.integration.ManagedTileEntityEnvironment;
import li.cil.oc.integration.appeng.NetworkControl;
import li.cil.oc.integration.appeng.NetworkControl$class;
import li.cil.oc.integration.appeng.PartEnvironmentBase;
import li.cil.oc.integration.appeng.PartEnvironmentBase$class;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

public class DriverPartDualInterface implements DriverBlock {

    @Override
    public boolean worksWith(World world, BlockPos pos, EnumFacing face) {
        if (world.getTileEntity(pos) instanceof TileCableBus) {
            TileCableBus cable = (TileCableBus) world.getTileEntity(pos);
            for (EnumFacing f : EnumFacing.values()) {
                assert cable != null;
                if (cable.getPart(f) instanceof PartDualInterface) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing face) {
        IPartHost host = (IPartHost) world.getTileEntity(pos);
        return new Environment(host, face.getOpposite());
    }

    @SuppressWarnings("rawtypes")
    public static class Environment extends ManagedTileEntityEnvironment<IPartHost> implements NamedBlock, NetworkControl, PartEnvironmentBase {

        static final String name = "me_dual_interface";
        final AEPartLocation pos;

        public Environment(IPartHost tile, EnumFacing side) {
            super(tile, name);
            this.pos = AEPartLocation.fromFacing(side);
        }

        @Callback(doc = "function(side:number[, slot:number]):table -- Get the configuration of the dual interface pointing in the specified direction.")
        public Object[] getInterfaceConfiguration(Context context, Arguments args) {
            return this.getPartConfig(context, args, ClassTag$.MODULE$.apply(ISegmentedInventory.class));
        }

        @Callback(doc = "function(side:number[, slot:number][, database:address, entry:number[, size:number]]):boolean -- Configure the dual interface pointing in the specified direction.")
        public Object[] setInterfaceConfiguration(Context context, Arguments args) {
            return this.getPartConfig(context, args, ClassTag$.MODULE$.apply(ISegmentedInventory.class));
        }

        @Override
        @Callback(doc = "function():table -- Get a list of tables representing the available CPUs in the network.")
        public Object[] getCpus(Context context, Arguments arguments) {
            return NetworkControl$class.getCpus(this, context, arguments);
        }

        @Override
        @Callback(doc = "function([filter:table]):table -- Get a list of known item recipes. These can be used to issue crafting requests.")
        public Object[] getCraftables(Context context, Arguments arguments) {
            return NetworkControl$class.getCraftables(this, context, arguments);
        }

        @Override
        @Callback(doc = "function([filter:table]):table -- Get a list of the stored items in the network.")
        public Object[] getItemsInNetwork(Context context, Arguments arguments) {
            return NetworkControl$class.getItemsInNetwork(this, context, arguments);
        }

        @Override
        @Callback(doc = "function([filter:table, dbAddress:string, startSlot:number, count:number]): bool -- Store items in the network matching the specified filter in the database with the specified address.")
        public Object[] store(Context context, Arguments arguments) {
            return NetworkControl$class.store(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():table -- Get a list of the stored fluids in the network.")
        public Object[] getFluidsInNetwork(Context context, Arguments arguments) {
            return NetworkControl$class.getFluidsInNetwork(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the average power injection into the network.")
        public Object[] getAvgPowerInjection(Context context, Arguments arguments) {
            return NetworkControl$class.getAvgPowerInjection(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the average power usage of the network.")
        public Object[] getAvgPowerUsage(Context context, Arguments arguments) {
            return NetworkControl$class.getAvgPowerUsage(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the idle power usage of the network.")
        public Object[] getIdlePowerUsage(Context context, Arguments arguments) {
            return NetworkControl$class.getIdlePowerUsage(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the maximum stored power in the network.")
        public Object[] getMaxStoredPower(Context context, Arguments arguments) {
            return NetworkControl$class.getMaxStoredPower(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Get the stored power in the network. ")
        public Object[] getStoredPower(Context context, Arguments arguments) {
            return NetworkControl$class.getStoredPower(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():boolean -- True if the AE network is considered online")
        public Object[] isNetworkPowered(Context context, Arguments arguments) {
            return NetworkControl$class.isNetworkPowered(this, context, arguments);
        }

        @Override
        @Callback(doc = "function():number -- Returns the energy demand on the AE network")
        public Object[] getEnergyDemand(Context context, Arguments arguments) {
            return NetworkControl$class.getEnergyDemand(this, context, arguments);
        }

        @Override
        public <PartType extends ISegmentedInventory> Object[] getPartConfig(Context context, Arguments arguments, ClassTag<PartType> classTag) {
            return PartEnvironmentBase$class.getPartConfig(this, context, arguments, classTag);
        }

        @Override
        public <PartType extends ISegmentedInventory> Object[] setPartConfig(Context context, Arguments arguments, ClassTag<PartType> classTag) {
            return PartEnvironmentBase$class.setPartConfig(this, context, arguments, classTag);
        }

        @Override
        public String preferredName() {
            return name;
        }

        @Override
        public int priority() {
            return 0;
        }

        @Override
        public TileEntity tile() {
            return (TileEntity) this.tileEntity;
        }

        @Override
        public AEPartLocation pos() {
            return this.pos;
        }

        @Override
        public IPartHost host() {
            return this.tileEntity;
        }
    }

    public static class Provider implements EnvironmentProvider {

        @Override
        public Class<?> getEnvironment(ItemStack stack) {
            if (stack.isItemEqual(new ItemStack(FCItems.PART_DUAL_INTERFACE))) {
                return Environment.class;
            }
            return null;
        }
    }

}
