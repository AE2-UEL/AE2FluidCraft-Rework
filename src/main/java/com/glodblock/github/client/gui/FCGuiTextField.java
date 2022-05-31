package com.glodblock.github.client.gui;

import appeng.client.gui.widgets.ITooltip;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class FCGuiTextField extends GuiTextField {
    private static final int PADDING = 2;
    private String tooltip;

    private final int _xPos;
    private final int _yPos;
    private final int _width;
    private final int _height;

    /**
     * Uses the values to instantiate a padded version of a text field.
     * Pays attention to the '_' caret.
     *
     * @param fontRenderer renderer for the strings
     * @param xPos         absolute left position
     * @param yPos         absolute top position
     * @param width        absolute width
     * @param height       absolute height
     */
    public FCGuiTextField(final FontRenderer fontRenderer, final int xPos, final int yPos, final int width, final int height) {
        super(fontRenderer, xPos + PADDING, yPos + PADDING, width - 2 * PADDING - fontRenderer.getCharWidth('_'), height - 2 * PADDING);

        this._xPos = xPos;
        this._yPos = yPos;
        this._width = width;
        this._height = height;
    }

    @Override
    public void mouseClicked(final int xPos, final int yPos, final int button) {
        super.mouseClicked(xPos, yPos, button);

        final boolean requiresFocus = this.isMouseIn(xPos, yPos);

        this.setFocused(requiresFocus);
    }

    /**
     * Checks if the mouse is within the element
     *
     * @param xCoord current x coord of the mouse
     * @param yCoord current y coord of the mouse
     * @return true if mouse position is within the text field area
     */
    public boolean isMouseIn(final int xCoord, final int yCoord) {
        final boolean withinXRange = this._xPos <= xCoord && xCoord < this._xPos + this._width;
        final boolean withinYRange = this._yPos <= yCoord && yCoord < this._yPos + this._height;

        return withinXRange && withinYRange;
    }

    public void setMessage(String t)
    {
        tooltip = t;
    }

    public class TooltipProvider implements ITooltip
    {
        @Override
        public String getMessage() {
            return tooltip;
        }

        @Override
        public int xPos() {
            return _xPos;
        }

        @Override
        public int yPos() {
            return _yPos;
        }

        @Override
        public int getHeight() {
            return _height;
        }

        @Override
        public int getWidth() { return _width; }

        @Override
        public boolean isVisible() {
            return getVisible();
        }
    }
}
