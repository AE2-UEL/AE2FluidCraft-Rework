package com.glodblock.github.coremod;

import com.glodblock.github.coremod.transform.*;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassWriter;

public class FCClassTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] code) {
        Transform tform;
        switch (transformedName) {
            case "appeng.crafting.CraftingTreeNode":
                tform = CraftingTreeNodeTransformer.INSTANCE;
                break;
            case "appeng.helpers.DualityInterface":
                tform = DualityInterfaceTransformer.INSTANCE;
                break;
            case "appeng.me.cluster.implementations.CraftingCPUCluster":
                tform = CraftingCpuTransformer.INSTANCE;
                break;
            case "appeng.items.parts.PartType":
                tform = PartTypeTransformer.INSTANCE;
                break;
            case "appeng.core.sync.GuiBridge":
                tform = GuiBridgeTransformer.INSTANCE;
                break;
            case "thelm.packagedauto.integration.appeng.recipe.PackageCraftingPatternHelper":
                tform = PautoCraftingPatternHelperTransformer.TFORM_INPUTS;
                break;
            case "thelm.packagedauto.integration.appeng.recipe.RecipeCraftingPatternHelper":
                tform = PautoCraftingPatternHelperTransformer.TFORM_OUTPUTS;
                break;
            case "thelm.packagedauto.tile.TileUnpackager":
                tform = TileUnpackagerTransformer.INSTANCE;
                break;
            case "mezz.jei.input.GuiContainerWrapper":
                tform = GuiContainerWrapperTransformer.INSTANCE;
                break;
            case "appeng.container.implementations.ContainerInterfaceTerminal":
            case "appeng.container.implementations.ContainerInterfaceConfigurationTerminal":
            case "appeng.container.implementations.ContainerFluidInterfaceConfigurationTerminal":
                tform = ContainerInterfaceTerminalTransformer.INSTANCE;
                break;
            case "appeng.client.gui.implementations.GuiCraftingCPU":
            case "appeng.client.gui.implementations.GuiCraftConfirm":
                tform = GuiCraftingTransformer.INSTANCE;
                break;
            case "appeng.client.gui.implementations.GuiCraftingStatus":
                tform = GuiCraftingStatusTransformer.INSTANCE;
                break;
            case "p455w0rd.wct.client.gui.GuiCraftingCPU":
            case "p455w0rd.wct.client.gui.GuiCraftConfirm":
                tform = WCTGuiCraftingTransformer.INSTANCE;
                break;
            case "appeng.tile.AEBaseTile":
            case "appeng.parts.AEBasePart":
                tform = AEBaseTilePartTransformer.INSTANCE;
                break;
            case "appeng.container.implementations.ContainerPatternEncoder":
                tform = ContainerPatternEncoderTransformer.INSTANCE;
                break;
            case "appeng.me.cache.GridStorageCache":
                tform = GridStorageCacheTransformer.INSTANCE;
                break;
            default:
                return code;
        }
        System.out.println("[AE2FC] Transforming class: " + transformedName);
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