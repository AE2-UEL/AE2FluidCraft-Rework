package com.glodblock.github.client.button;

import appeng.client.gui.style.Blitter;
import com.glodblock.github.util.NameConst;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum BlitMap {

    NOT_COMBINE(0, 0),
    COMBINE(16, 0),
    SEND_FLUID(2*16, 0),
    SEND_PACKET(3*16, 0),
    FLUID_FIRST(0, 16),
    ITEM_FIRST(16, 16),
    CRAFT_FLUID(2*16, 16),
    SPLITTING(3*16, 16),
    NOT_SPLITTING(0, 2*16),
    BLOCK_ALL(16, 2*16),
    BLOCK_ITEM(2*16, 2*16),
    BLOCK_FLUID(3*16, 2*16);


    private final int x;
    private final int y;
    private final int width;
    private final int height;

    BlitMap(int x, int y) {
        this(x, y, 16, 16);
    }

    BlitMap(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Blitter getBlitter() {
        return Blitter.texture("gui/states.png", 64, 64).src(this.x, this.y, this.width, this.height);
    }

    public ITextComponent getDisplayName() {
        return new TranslationTextComponent(NameConst.TT_KEY + this.toString().toLowerCase());
    }

    public ITextComponent getHint() {
        return new TranslationTextComponent(NameConst.TT_KEY + this.toString().toLowerCase() + ".hint");
    }

}
