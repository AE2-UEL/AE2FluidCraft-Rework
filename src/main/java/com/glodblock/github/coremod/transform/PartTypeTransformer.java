package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class PartTypeTransformer extends FCClassTransformer.ClassMapper {

    public static final PartTypeTransformer INSTANCE = new PartTypeTransformer();

    private PartTypeTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new PartTypeTransformer.TransformPartType(Opcodes.ASM5, downstream);
    }

    private static class TransformPartType extends ClassVisitor {

        TransformPartType(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("<clinit>")) {
                return new FluidExportBusTransformer(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class FluidExportBusTransformer extends MethodVisitor {

        FluidExportBusTransformer(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            if (Type.getType("Lappeng/fluids/parts/PartFluidExportBus;").equals(cst)) {
                super.visitLdcInsn(Type.getType("Lcom/glodblock/github/common/part/PartFluidExportBus;"));
            } else {
                super.visitLdcInsn(cst);
            }
        }

    }

}
