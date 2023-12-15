package baubles.api.cap;

import baubles.api.IBauble;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;

public class BaublesCapabilities {
    @CapabilityInject(IBaublesItemHandler.class)
    public static final Capability<IBaublesItemHandler> CAPABILITY_BAUBLES = null;
    @CapabilityInject(IBauble.class)
    public static final Capability<IBauble> CAPABILITY_ITEM_BAUBLE = null;

    public BaublesCapabilities() {
    }

    public static class CapabilityItemBaubleStorage implements Capability.IStorage<IBauble> {
        public CapabilityItemBaubleStorage() {
        }

        public NBTBase writeNBT(Capability<IBauble> capability, IBauble instance, EnumFacing side) {
            return null;
        }

        public void readNBT(Capability<IBauble> capability, IBauble instance, EnumFacing side, NBTBase nbt) {
        }
    }

    public static class CapabilityBaubles<T extends IBaublesItemHandler> implements Capability.IStorage<IBaublesItemHandler> {
        public CapabilityBaubles() {
        }

        public NBTBase writeNBT(Capability<IBaublesItemHandler> capability, IBaublesItemHandler instance, EnumFacing side) {
            return null;
        }

        public void readNBT(Capability<IBaublesItemHandler> capability, IBaublesItemHandler instance, EnumFacing side, NBTBase nbt) {
        }
    }
}
