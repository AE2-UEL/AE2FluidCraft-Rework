package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GuiCraftingStatusTransformer extends FCClassTransformer.ClassMapper {

    public static final GuiCraftingStatusTransformer INSTANCE = new GuiCraftingStatusTransformer();

    private GuiCraftingStatusTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformGuiCraftingStatus(Opcodes.ASM5, downstream);
    }

    private static class TransformGuiCraftingStatus extends ClassVisitor {

        TransformGuiCraftingStatus(int api, ClassVisitor cv) {
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
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (opcode == Opcodes.INVOKEVIRTUAL && name.equals("getCrafting")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "displayAEFluidAmount",
                        "(Lappeng/api/storage/data/IAEItemStack;)Lappeng/api/storage/data/IAEItemStack;",
                        false);
            }
        }

    }

}
