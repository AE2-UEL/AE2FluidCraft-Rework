package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GuiCraftingTransformer extends FCClassTransformer.ClassMapper {

    public static final GuiCraftingTransformer INSTANCE = new GuiCraftingTransformer();

    private GuiCraftingTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformGuiCrafting(Opcodes.ASM5, downstream);
    }

    private static class TransformGuiCrafting extends ClassVisitor {

        TransformGuiCrafting(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ("drawFG".equals(name)) {
                return new TransformFluidIcon(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformFluidIcon extends MethodVisitor {

        TransformFluidIcon(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESTATIC && owner.equals("appeng/util/Platform") && name.equals("getItemDisplayName")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "displayAEFluid",
                        "(Lappeng/api/storage/data/IAEItemStack;)Lappeng/api/storage/data/IAEItemStack;",
                        false);
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (opcode == Opcodes.INVOKEINTERFACE && owner.equals("appeng/api/storage/data/IAEItemStack") && name.equals("asItemStackRepresentation")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "displayFluid",
                        "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                        false);
            }
        }

    }
}
