package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ContainerCraftConfirmTransformer extends FCClassTransformer.ClassMapper {

    public static ContainerCraftConfirmTransformer INSTANCE = new ContainerCraftConfirmTransformer();

    private ContainerCraftConfirmTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformContainerCraftConfirm(Opcodes.ASM5, downstream);
    }

    @Override
    protected int getWriteFlags() {
        return ClassWriter.COMPUTE_FRAMES;
    }

    private static class TransformContainerCraftConfirm extends ClassVisitor {

        public TransformContainerCraftConfirm(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("startJob")) {
                return new TransformStartJob(api, super.visitMethod(access, name, desc, signature, exceptions));
            } else {
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }

    }

    private static class TransformStartJob extends MethodVisitor {

        Label L = new Label();

        public TransformStartJob(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitInsn(Opcodes.DUP);
            super.visitFieldInsn(Opcodes.GETFIELD, "appeng/container/implementations/ContainerCraftConfirm", "cpus", "Ljava/util/ArrayList;");
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitFieldInsn(Opcodes.GETFIELD, "appeng/container/implementations/ContainerCraftConfirm", "result", "Lappeng/api/networking/crafting/ICraftingJob;");
            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/glodblock/github/coremod/CoreModHooks",
                    "startJob",
                    "(Lappeng/container/implementations/ContainerCraftConfirm;Ljava/util/ArrayList;Lappeng/api/networking/crafting/ICraftingJob;)Z",
                    false);
            super.visitJumpInsn(Opcodes.IFEQ, L);
            super.visitInsn(Opcodes.RETURN);
            super.visitLabel(L);
        }

    }

}
