package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ContainerPatternEncoderTransformer extends FCClassTransformer.ClassMapper {

    public static final ContainerPatternEncoderTransformer INSTANCE = new ContainerPatternEncoderTransformer();

    private ContainerPatternEncoderTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformContainerPatternEncoder(Opcodes.ASM5, downstream);
    }

    private static class TransformContainerPatternEncoder extends ClassVisitor {

        TransformContainerPatternEncoder(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("encode")) {
                return new EncodeTransformer(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class EncodeTransformer extends MethodVisitor {

        EncodeTransformer(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (name.equals("isPattern")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "transformPattern",
                        "(Lappeng/container/implementations/ContainerPatternEncoder;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/item/ItemStack;",
                        false
                );
                super.visitVarInsn(Opcodes.ASTORE, 1);
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ALOAD, 1);
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

    }
}
