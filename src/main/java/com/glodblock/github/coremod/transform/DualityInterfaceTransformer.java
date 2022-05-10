package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DualityInterfaceTransformer extends FCClassTransformer.ClassMapper {

    public static final DualityInterfaceTransformer INSTANCE = new DualityInterfaceTransformer();

    private DualityInterfaceTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformDualityInterface(Opcodes.ASM5, downstream);
    }

    private static class TransformDualityInterface extends ClassVisitor {

        TransformDualityInterface(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            switch (name) {
                case "pushItemsOut":
                case "pushPattern":
                case "isBusy":
                    return new TransformInvAdaptorCalls(api, super.visitMethod(access, name, desc, signature, exceptions));
                default:
                    return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }

    }

    private static class TransformInvAdaptorCalls extends MethodVisitor {

        TransformInvAdaptorCalls(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESTATIC && owner.equals("appeng/util/InventoryAdaptor") && name.equals("getAdaptor")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/glodblock/github/coremod/hooker/CoreModHooks",
                    "wrapInventory",
                    "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraftforge/common/util/ForgeDirection;)Lappeng/util/InventoryAdaptor;",
                    false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

    }

}
