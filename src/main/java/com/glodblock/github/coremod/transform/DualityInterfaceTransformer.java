package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.*;

public class DualityInterfaceTransformer extends FCClassTransformer.ClassMapper {

    public static final DualityInterfaceTransformer INSTANCE = new DualityInterfaceTransformer();

    private DualityInterfaceTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        downstream.visitField(Opcodes.ACC_PUBLIC, "fluidPacket", "Z", null, false).visitEnd();
        // Cannot set instance fields directly, injecting initialization code into the constructor is necessary
        downstream.visitField(Opcodes.ACC_PUBLIC, "allowSplitting", "Z", null, false).visitEnd();

        return new TransformDualityInterface(Opcodes.ASM5, downstream);
    }

    private static class TransformDualityInterface extends ClassVisitor {

        TransformDualityInterface(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            switch (name) {
                case "<init>":
                    return new TransformConstructor(api, super.visitMethod(access, name, desc, signature, exceptions));
                case "pushItemsOut":
                case "pushPattern":
                case "isBusy":
                    return new TransformInvAdaptorCalls(api, super.visitMethod(access, name, desc, signature, exceptions));
                case "isCustomInvBlocking":
                    return new TransformBlockAdaptorCalls(api, super.visitMethod(access, name, desc, signature, exceptions));
                case "writeToNBT":
                    return new TransformNBTIO(api, super.visitMethod(access, name, desc, signature, exceptions), false);
                case "readFromNBT":
                    return new TransformNBTIO(api, super.visitMethod(access, name, desc, signature, exceptions), true);
                default:
                    return super.visitMethod(access, name, desc, signature, exceptions);
            }
        }

    }

    private static class TransformConstructor extends MethodVisitor {

        TransformConstructor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            super.visitMethodInsn(opcode, owner, name, desc, itf);
            if (owner.equals("java/lang/Object") && name.equals("<init>") && desc.equals("()V")) {
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitInsn(Opcodes.ICONST_1);
                super.visitFieldInsn(Opcodes.PUTFIELD, "appeng/helpers/DualityInterface", "allowSplitting", "Z");
            }
        }
    }

    private static class TransformNBTIO extends MethodVisitor {

        boolean in;

        TransformNBTIO(int api, MethodVisitor mv, boolean mode) {
            super(api, mv);
            this.in = mode;
        }

        @Override
        public void visitCode() {
            super.visitCode();
            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitVarInsn(Opcodes.ALOAD, 1);
            if (in) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "readExtraNBTInterface",
                        "(Lappeng/helpers/DualityInterface;Lnet/minecraft/nbt/NBTTagCompound;)V",
                        false);
            } else {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "writeExtraNBTInterface",
                        "(Lappeng/helpers/DualityInterface;Lnet/minecraft/nbt/NBTTagCompound;)V",
                        false);
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
                        "com/glodblock/github/coremod/CoreModHooks",
                        "wrapInventory",
                        "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)Lappeng/util/InventoryAdaptor;",
                        false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

    }

    private static class TransformBlockAdaptorCalls extends MethodVisitor {

        TransformBlockAdaptorCalls(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKESTATIC && owner.equals("appeng/util/inv/BlockingInventoryAdaptor") && name.equals("getAdaptor")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "wrapBlockInventory",
                        "(Lnet/minecraft/tileentity/TileEntity;Lnet/minecraft/util/EnumFacing;)Lappeng/util/inv/BlockingInventoryAdaptor;",
                        false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

    }

}