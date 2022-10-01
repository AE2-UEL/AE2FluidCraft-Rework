package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class GuiBridgeTransformer extends FCClassTransformer.ClassMapper {

    public static final GuiBridgeTransformer INSTANCE = new GuiBridgeTransformer();

    private GuiBridgeTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformerGuiBridge(Opcodes.ASM5, downstream);
    }

    private static class TransformerGuiBridge extends ClassVisitor {

        TransformerGuiBridge(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("<clinit>")) {
                return new InterfaceContainerTransformer(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class InterfaceContainerTransformer extends MethodVisitor {

        InterfaceContainerTransformer(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitLdcInsn(Object cst) {
            if (Type.getType("Lappeng/container/implementations/ContainerInterface;").equals(cst)) {
                super.visitLdcInsn(Type.getType("Lcom/glodblock/github/client/container/ContainerWrapInterface;"));
            } else {
                super.visitLdcInsn(cst);
            }
        }

    }


}
