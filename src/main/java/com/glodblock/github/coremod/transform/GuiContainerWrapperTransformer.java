package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GuiContainerWrapperTransformer extends FCClassTransformer.ClassMapper {

    public static final GuiContainerWrapperTransformer INSTANCE = new GuiContainerWrapperTransformer();

    public GuiContainerWrapperTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformGuiContainerWrapper(Opcodes.ASM5, downstream);
    }

    private static class TransformGuiContainerWrapper extends ClassVisitor {

        public TransformGuiContainerWrapper(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("getIngredientUnderMouse")) {
                return new TransformGetIngredient(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformGetIngredient extends MethodVisitor {

        int target = 0;

        public TransformGetIngredient(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESPECIAL && owner.equals("java/awt/Rectangle") && name.equals("<init>")) {
                if (target == 0) {
                    target = 1;
                }
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            if (target == 1 && opcode == Opcodes.ALOAD && var == 6) {
                target = 2;
                super.visitVarInsn(Opcodes.ALOAD, 6);
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "wrapFluidPacket",
                        "(Lnet/minecraft/item/ItemStack;)Ljava/lang/Object;",
                        false);
                super.visitVarInsn(Opcodes.ASTORE, 6);
                super.visitVarInsn(Opcodes.ALOAD, 6);
            }
            else {
                super.visitVarInsn(opcode, var);
            }
        }

    }

}
