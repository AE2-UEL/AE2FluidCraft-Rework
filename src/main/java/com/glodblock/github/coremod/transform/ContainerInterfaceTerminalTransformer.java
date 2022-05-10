package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ContainerInterfaceTerminalTransformer extends FCClassTransformer.ClassMapper {

    public static final ContainerInterfaceTerminalTransformer INSTANCE = new ContainerInterfaceTerminalTransformer();

    private ContainerInterfaceTerminalTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformContainerInterfaceTerminal(Opcodes.ASM5, downstream);
    }

    private static class TransformContainerInterfaceTerminal extends ClassVisitor {

        TransformContainerInterfaceTerminal(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("detectAndSendChanges") || name.equals("func_75142_b") || name.equals("regenList")) {
                return new TransformDetectAndSendChanges(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformDetectAndSendChanges extends MethodVisitor {

        TransformDetectAndSendChanges(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (opcode == Opcodes.INVOKEINTERFACE
                && owner.equals("appeng/api/networking/IGrid") && name.equals("getMachines")) {
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                    "com/glodblock/github/coremod/hooker/CoreModHooks",
                    "getMachines",
                    "(Lappeng/api/networking/IGrid;Ljava/lang/Class;)Lappeng/api/networking/IMachineSet;",
                    false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

    }
}
