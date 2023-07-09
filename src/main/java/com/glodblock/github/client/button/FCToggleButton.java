package com.glodblock.github.client.button;

import appeng.client.gui.Icon;
import appeng.client.gui.widgets.ITooltip;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FCToggleButton extends Button implements ITooltip {
    private final BlitMap[] modes;
    private final int length;
    private int active;
    private boolean halfSize = false;

    public FCToggleButton(Button.IPressable onPress, BlitMap... modes) {
        super(0, 0, 16, 16, StringTextComponent.EMPTY, onPress);
        this.modes = modes;
        this.length = modes.length;
    }

    public void next() {
        active = (active + 1) % length;
    }

    public void forceActive(int value) {
        active = value % length;
    }

    @Override
    public void renderWidget(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
        if (this.visible) {
            if (this.halfSize) {
                matrixStack.push();
                matrixStack.translate(this.x, this.y, 0.0);
                matrixStack.scale(0.5F, 0.5F, 1.0F);
                Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(0, 0).blit(matrixStack, this.getBlitOffset());
                this.getIcon().getBlitter().dest(0, 0).blit(matrixStack, this.getBlitOffset());
                matrixStack.pop();
            } else {
                Icon.TOOLBAR_BUTTON_BACKGROUND.getBlitter().dest(this.x, this.y).blit(matrixStack, this.getBlitOffset());
                this.getIcon().getBlitter().dest(this.x, this.y).blit(matrixStack, this.getBlitOffset());
            }
        }
    }

    public void setHalfSize(boolean value) {
        this.halfSize = value;
        if (value) {
            this.width = 8;
            this.height = 8;
        }
    }

    @Override
    public void onPress() {
        this.next();
        this.onPress.onPress(this);
    }

    private BlitMap getIcon() {
        return this.modes[active];
    }

    public int getActive() {
        return this.active;
    }

    @Nonnull
    public List<ITextComponent> getTooltipMessage() {
        return Arrays.asList(this.getIcon().getDisplayName(), this.getIcon().getHint());
    }

    public int getTooltipAreaX() {
        return this.x;
    }

    public int getTooltipAreaY() {
        return this.y;
    }

    public int getTooltipAreaWidth() {
        return this.width;
    }

    public int getTooltipAreaHeight() {
        return this.height;
    }

    public boolean isTooltipAreaVisible() {
        return this.visible;
    }
}
