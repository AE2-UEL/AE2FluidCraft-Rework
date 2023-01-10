package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class WCTGuiCraftingTransformer extends FCClassTransformer.ClassMapper {

    public static final WCTGuiCraftingTransformer INSTANCE = new WCTGuiCraftingTransformer();

    private WCTGuiCraftingTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new WCTGuiCraftingTransformer.TransformGuiCrafting(Opcodes.ASM5, downstream);
    }

    private static class TransformGuiCrafting extends ClassVisitor {

        TransformGuiCrafting(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if ("drawFG".equals(name)) {
                return new WCTGuiCraftingTransformer.TransformFluidIcon(api, super.visitMethod(access, name, desc, signature, exceptions));
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
            if (opcode == Opcodes.INVOKEINTERFACE && owner.equals("appeng/api/storage/data/IAEItemStack") && name.equals("createItemStack")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "displayFluid",
                        "(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                        false);
            }
        }

    }
}