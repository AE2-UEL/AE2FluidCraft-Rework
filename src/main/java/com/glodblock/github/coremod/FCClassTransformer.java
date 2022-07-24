package com.glodblock.github.coremod;

import com.glodblock.github.coremod.transform.*;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

public class FCClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        Transform tform;
        switch (transformedName) {
            case "appeng.crafting.CraftingTreeNode":
                tform = CraftingTreeNodeTransformer.INSTANCE;
                break;
            case "appeng.me.cluster.implementations.CraftingCPUCluster":
                tform = CraftingCpuTransformer.INSTANCE;
                break;
            case "appeng.helpers.DualityInterface":
                tform = DualityInterfaceTransformer.INSTANCE;
                break;
            case "appeng.container.implementations.ContainerInterfaceTerminal":
                tform = ContainerInterfaceTerminalTransformer.INSTANCE;
                break;
            case "appeng.client.gui.implementations.GuiCraftingCPU":
            case "appeng.client.gui.implementations.GuiCraftConfirm":
            case "net.p455w0rd.wirelesscraftingterminal.client.gui.GuiCraftConfirm":
                tform = GuiCraftingTransformer.INSTANCE;
                break;
            default:
                return code;
        }
        System.out.println("[FCAE2] Transforming class: " + transformedName);
        return tform.transformClass(code);
    }

    public interface Transform {

        byte[] transformClass(byte[] code);

    }

    public static abstract class ClassMapper implements Transform {

        @Override
        public byte[] transformClass(byte[] code) {
            ClassReader reader = new ClassReader(code);
            ClassWriter writer = new ClassWriter(reader, getWriteFlags());
            reader.accept(getClassMapper(writer), 0);
            return writer.toByteArray();
        }

        protected int getWriteFlags() {
            return 0;
        }

        protected abstract ClassVisitor getClassMapper(ClassVisitor downstream);

    }
}
