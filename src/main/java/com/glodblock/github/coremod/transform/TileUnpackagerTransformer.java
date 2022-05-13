package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class TileUnpackagerTransformer extends FCClassTransformer.ClassMapper {

    public static final TileUnpackagerTransformer INSTANCE = new TileUnpackagerTransformer();

    private TileUnpackagerTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformTileUnpackager(Opcodes.ASM5, downstream);
    }

    private static class TransformTileUnpackager extends ClassVisitor {

        TransformTileUnpackager(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("emptyTrackers") && desc.equals("()V")) {
                return new TransformEmptyTrackers(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformEmptyTrackers extends MethodVisitor {

        private boolean gettingItemHandlerCap = false;

        TransformEmptyTrackers(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            super.visitFieldInsn(opcode, owner, name, desc);
            if (opcode == Opcodes.GETSTATIC && desc.equals("Lnet/minecraftforge/common/capabilities/Capability;")) {
                gettingItemHandlerCap = name.equals("ITEM_HANDLER_CAPABILITY");
            }
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKEVIRTUAL && gettingItemHandlerCap) {
                switch (name) {
                    case "hasCapability":
                        if (desc.equals("(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z")) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/glodblock/github/coremod/CoreModHooks", "checkForItemHandler",
                                    "(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;Lnet/minecraftforge/common/capabilities/Capability;" +
                                            "Lnet/minecraft/util/EnumFacing;)Z", false);
                            gettingItemHandlerCap = false;
                        }
                        break;
                    case "getCapability":
                        if (desc.equals("(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;")) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/glodblock/github/coremod/CoreModHooks", "wrapItemHandler",
                                    "(Lnet/minecraftforge/common/capabilities/ICapabilityProvider;Lnet/minecraftforge/common/capabilities/Capability;" +
                                            "Lnet/minecraft/util/EnumFacing;)Lnet/minecraftforge/items/IItemHandler;", false);
                            gettingItemHandlerCap = false;
                        }
                        break;
                    default:
                        super.visitMethodInsn(opcode, owner, name, desc, itf);
                        break;
                }
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

    }

}
