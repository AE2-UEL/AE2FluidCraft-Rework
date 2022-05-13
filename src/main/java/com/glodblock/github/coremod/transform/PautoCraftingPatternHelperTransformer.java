package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class PautoCraftingPatternHelperTransformer extends FCClassTransformer.ClassMapper {

    public static final PautoCraftingPatternHelperTransformer TFORM_INPUTS = new PautoCraftingPatternHelperTransformer("inputs");
    public static final PautoCraftingPatternHelperTransformer TFORM_OUTPUTS = new PautoCraftingPatternHelperTransformer("outputs");

    private final String fieldName;

    private PautoCraftingPatternHelperTransformer(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformPackageCraftingPatternHelper(Opcodes.ASM5, downstream);
    }

    private class TransformPackageCraftingPatternHelper extends ClassVisitor {

        TransformPackageCraftingPatternHelper(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("<init>")) {
                return new TransformCtor(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private class TransformCtor extends MethodVisitor {

        TransformCtor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (opcode == Opcodes.PUTFIELD && name.equals(fieldName) && desc.equals("[Lappeng/api/storage/data/IAEItemStack;")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/glodblock/github/coremod/CoreModHooks", "flattenFluidPackets",
                        "([Lappeng/api/storage/data/IAEItemStack;)[Lappeng/api/storage/data/IAEItemStack;", false);
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

    }

}
