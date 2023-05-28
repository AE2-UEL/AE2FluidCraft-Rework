package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AEBaseTilePartTransformer extends FCClassTransformer.ClassMapper {

    public static final AEBaseTilePartTransformer INSTANCE = new AEBaseTilePartTransformer();

    private AEBaseTilePartTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformAEBaseTilePart(Opcodes.ASM5, downstream);
    }

    private static class TransformAEBaseTilePart extends ClassVisitor {

        TransformAEBaseTilePart(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            switch (name) {
                case "uploadSettings":
                    return new TransformUploadSettings(api, super.visitMethod(access, name, desc, signature, exceptions));
                case "downloadSettings":
                    return new TransformDownloadSettings(api, super.visitMethod(access, name, desc, signature, exceptions));
                default:
                    return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }
    }

    private static class TransformUploadSettings extends MethodVisitor {

        TransformUploadSettings(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitVarInsn(Opcodes.ALOAD, 2);
            super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/glodblock/github/coremod/CoreModHooks",
                    "uploadExtraNBT",
                    "(Ljava/lang/Object;Lnet/minecraft/nbt/NBTTagCompound;)V",
                    false
            );
        }
    }

    private static class TransformDownloadSettings extends MethodVisitor {

        boolean checked = true;

        TransformDownloadSettings(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitVarInsn(int opcode, int var) {
            super.visitVarInsn(opcode, var);
            if (checked && opcode == Opcodes.ASTORE && var == 2) {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitVarInsn(Opcodes.ALOAD, 2);
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "downloadExtraNBT",
                        "(Ljava/lang/Object;Lnet/minecraft/nbt/NBTTagCompound;)V",
                        false
                );
                checked = false;
            }
        }
    }



}
