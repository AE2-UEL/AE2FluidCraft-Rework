package com.glodblock.github.coremod.transform;

import com.glodblock.github.coremod.FCClassTransformer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CraftingTreeNodeTransformer extends FCClassTransformer.ClassMapper {

    public static final CraftingTreeNodeTransformer INSTANCE = new CraftingTreeNodeTransformer();

    private CraftingTreeNodeTransformer() {
        // NO-OP
    }

    @Override
    protected ClassVisitor getClassMapper(ClassVisitor downstream) {
        return new TransformCraftingTreeNode(Opcodes.ASM5, downstream);
    }

    private static class TransformCraftingTreeNode extends ClassVisitor {

        TransformCraftingTreeNode(int api, ClassVisitor cv) {
            super(api, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (name.equals("request")) {
                return new TransformRequest(api, super.visitMethod(access, name, desc, signature, exceptions));
            }
            return super.visitMethod(access, name, desc, signature, exceptions);
        }

    }

    private static class TransformRequest extends MethodVisitor {

        private int writingBytes = 0;
        private boolean fired = false;

        TransformRequest(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (opcode == Opcodes.GETFIELD && owner.equals("appeng/crafting/CraftingTreeNode") && name.equals("bytes")) {
                writingBytes ++;
                fired = true;
            }
            super.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (writingBytes <= 5 && fired && opcode == Opcodes.INVOKEINTERFACE
                    && owner.equals("appeng/api/storage/data/IAEItemStack") && name.equals("getStackSize")) {
                this.fired = false;
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "getCraftingByteCost",
                        "(Lappeng/api/storage/data/IAEItemStack;)J",
                        false);
            } else {
                super.visitMethodInsn(opcode, owner, name, desc, itf);
            }
        }

        @Override
        public void visitInsn(int opcode) {
            if (writingBytes == 6 && fired && opcode == Opcodes.LADD) {
                this.fired = false;
                super.visitVarInsn(Opcodes.ALOAD, 0);
                super.visitFieldInsn(Opcodes.GETFIELD,
                        "appeng/crafting/CraftingTreeNode",
                        "what",
                        "Lappeng/api/storage/data/IAEItemStack;");
                super.visitMethodInsn(Opcodes.INVOKESTATIC,
                        "com/glodblock/github/coremod/CoreModHooks",
                        "getCraftingByteCost",
                        "(JJLappeng/api/storage/data/IAEItemStack;)J",
                        false);
            } else {
                super.visitInsn(opcode);
            }
        }

    }

}
