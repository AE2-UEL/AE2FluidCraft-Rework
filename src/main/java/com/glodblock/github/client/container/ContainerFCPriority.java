package com.glodblock.github.client.container;

import appeng.api.config.SecurityPermissions;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.container.implementations.ContainerTypeBuilder;
import appeng.helpers.IPriorityHost;
import com.glodblock.github.interfaces.ConfigData;
import com.glodblock.github.util.ConfigSet;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;

public class ContainerFCPriority extends AEBaseContainer implements ConfigData {
    public static final ContainerType<ContainerFCPriority> TYPE = ContainerTypeBuilder.create(ContainerFCPriority::new, IPriorityHost.class)
            .requirePermission(SecurityPermissions.BUILD)
            .withInitialData(
            (host, buffer) -> buffer.writeVarInt(host.getPriority()),
            (host, container, buffer) -> container.priorityValue = buffer.readVarInt())
            .build("fc_priority");
    private final IPriorityHost priHost;
    private final ConfigSet exConfig = new ConfigSet();
    @GuiSync(2)
    public int priorityValue;

    public ContainerFCPriority(int id, PlayerInventory ip, IPriorityHost te) {
        super(TYPE, id, ip, te);
        this.priHost = te;
        this.priorityValue = te.getPriority();
        this.exConfig.addConfig("priority", v -> {
            this.priorityValue = (int) v;
            this.priHost.setPriority((int) v);
        }, () -> this.priorityValue);
    }

    public void setPriority(int newValue) {
        if (newValue != this.priorityValue) {
            if (this.isServer()) {
                this.priHost.setPriority(newValue);
                this.priorityValue = newValue;
            }
        }
    }

    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        this.verifyPermissions(SecurityPermissions.BUILD, false);
        if (this.isServer()) {
            this.priorityValue = this.priHost.getPriority();
        }
    }

    public int getPriorityValue() {
        return this.priorityValue;
    }

    public IPriorityHost getPriorityHost() {
        return this.priHost;
    }

    @Override
    public void set(String id, Object value) {
        this.exConfig.setConfig(id, value);
    }

    @Override
    public Object get(String id) {
        return this.exConfig.getConfig(id);
    }
}
