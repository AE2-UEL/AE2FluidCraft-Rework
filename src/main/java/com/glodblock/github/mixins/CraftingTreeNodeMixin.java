package com.glodblock.github.mixins;

import appeng.api.storage.data.IAEItemStack;
import appeng.crafting.CraftingTreeNode;
import com.glodblock.github.common.item.ItemFluidDrop;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.PUTFIELD;

@Mixin(CraftingTreeNode.class)
public abstract class CraftingTreeNodeMixin {

    @Redirect(
            method = "request",
            at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEItemStack;getStackSize()J", remap = false),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = GETFIELD),
                    to = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = PUTFIELD)
            ),
            remap = false
    )
    private long getCraftingByteCost(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop
                ? (long)Math.ceil(stack.getStackSize() / 1000D) : stack.getStackSize();
    }

    @Redirect(
            method = "request",
            at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEItemStack;getStackSize()J", remap = false),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = GETFIELD, ordinal = 1),
                    to = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = PUTFIELD, ordinal = 1)
            ),
            remap = false
    )
    private long getCraftingByteCost2(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop
                ? (long)Math.ceil(stack.getStackSize() / 1000D) : stack.getStackSize();
    }

    @Redirect(
            method = "request",
            at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEItemStack;getStackSize()J", remap = false),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = GETFIELD, ordinal = 2),
                    to = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = PUTFIELD, ordinal = 2)
            ),
            remap = false
    )
    private long getCraftingByteCost3(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop
                ? (long)Math.ceil(stack.getStackSize() / 1000D) : stack.getStackSize();
    }

    @Redirect(
            method = "request",
            at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEItemStack;getStackSize()J", remap = false),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = GETFIELD, ordinal = 3),
                    to = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = PUTFIELD, ordinal = 3)
            ),
            remap = false
    )
    private long getCraftingByteCost4(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop
                ? (long)Math.ceil(stack.getStackSize() / 1000D) : stack.getStackSize();
    }

    @Redirect(
            method = "request",
            at = @At(value = "INVOKE", target = "Lappeng/api/storage/data/IAEItemStack;getStackSize()J", remap = false),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = GETFIELD, ordinal = 4),
                    to = @At(value = "FIELD", target = "Lappeng/crafting/CraftingTreeNode;bytes:I", opcode = PUTFIELD, ordinal = 4)
            ),
            remap = false
    )
    private long getCraftingByteCost5(IAEItemStack stack) {
        return stack.getItem() instanceof ItemFluidDrop
                ? (long)Math.ceil(stack.getStackSize() / 1000D) : stack.getStackSize();
    }

}
