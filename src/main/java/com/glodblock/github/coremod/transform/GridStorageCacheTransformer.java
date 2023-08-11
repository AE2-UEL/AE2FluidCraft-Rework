package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class GridStorageCacheTransformer extends FCClassTransformer.ClassMapper {

    public static final GridStorageCacheTransformer INSTANCE = new GridStorageCacheTransformer();

    private GridStorageCacheTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformGridStorageCache(Opcodes.ASM5, downstream);
    }

    private static class TransformGridStorageCache extends ClassVisitor {

        public TransformGridStorageCache(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("buildNetworkStorage")) {
                return new TransformBuildNetworkStorage(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformBuildNetworkStorage extends MethodVisitor {

        public TransformBuildNetworkStorage(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (name.equals("addNewStorage")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "safeMEInventoryCheck",
                        "(Lappeng/me/storage/NetworkInventoryHandler;Lappeng/api/storage/IMEInventoryHandler;)V",
                        false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

    }
}
