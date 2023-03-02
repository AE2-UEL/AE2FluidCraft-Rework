package com.glodblock.github.client.button;

import appeng.client.gui.widgets.ITooltip;
import com.glodblock.github.FluidCraft;
import com.glodblock.github.util.NameConst;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class GuiFCImgButton extends GuiButton implements ITooltip {

    private static final Pattern COMPILE = Pattern.compile( "%s" );
    private static final Pattern PATTERN_NEW_LINE = Pattern.compile( "\\n", Pattern.LITERAL );
    private static Map<EnumPair, ButtonAppearance> appearances;
    private final String buttonSetting;
    private boolean halfSize = false;
    private String fillVar;
    private String currentValue;
    private static final String prefix = NameConst.TT_KEY;
    private static final int SPRITE_SHEET_GRID_SIZE = 4;
    private static final int MAX_INDEX = SPRITE_SHEET_GRID_SIZE * SPRITE_SHEET_GRID_SIZE - 1;

    public GuiFCImgButton( final int x, final int y, final String idx, final String val ) {
        super(0, 0, 16, "");

        this.buttonSetting = idx;
        this.currentValue = val;
        this.x = x;
        this.y = y;
        this.width = 16;
        this.height = 16;

        if( appearances == null ) {
            appearances = new HashMap<>();
            this.registerApp( 0, "NOT_COMBINE", "DONT_COMBINE", "not_combine" );
            this.registerApp( 1, "FORCE_COMBINE", "DO_COMBINE", "combine" );
            this.registerApp( 2, "SEND_FLUID", "REAL_FLUID", "real_fluid" );
            this.registerApp( 3, "SEND_PACKET", "FLUID_PACKET", "fake_packet" );
            this.registerApp( 4, "FLUID_FIRST", "FLUID", "fluid_first" );
            this.registerApp( 5, "ORIGIN_ORDER", "ITEM", "origin_order" );
            this.registerApp( 6, "CRAFT_FLUID", "ENCODE", "craft_fluid" );
            this.registerApp( 7, "SPLITTING", "ALLOW", "allow_splitting" );
            this.registerApp( 8, "SPLITTING", "PREVENT", "prevent_splitting" );
        }
    }

    private void registerApp(final int iconIndex, final String setting, final String val, final String title )
    {
        final ButtonAppearance a = new ButtonAppearance();
        a.displayName = I18n.format(prefix + title);
        a.displayValue = I18n.format(prefix + title + ".hint");
        a.index = iconIndex;
        appearances.put( new EnumPair( setting, val ), a );
    }

    public void setVisibility( final boolean vis )
    {
        this.visible = vis;
        this.enabled = vis;
    }

    private int getIconIndex()
    {
        if( this.buttonSetting != null && this.currentValue != null )
        {
            final ButtonAppearance app = appearances.get(new EnumPair( this.buttonSetting, this.currentValue ));
            if( app == null )
            {
                return MAX_INDEX;
            }
            return app.index;
        }
        return MAX_INDEX;
    }

    public String getSetting()
    {
        return this.buttonSetting;
    }

    public String getCurrentValue()
    {
        return this.currentValue;
    }

    public void set( final String e )
    {
        if(!this.currentValue.equals(e))
        {
            this.currentValue = e;
        }
    }

    public boolean isHalfSize()
    {
        return this.halfSize;
    }

    public void setHalfSize( final boolean halfSize )
    {
        this.halfSize = halfSize;
    }

    public String getFillVar()
    {
        return this.fillVar;
    }

    public void setFillVar( final String fillVar )
    {
        this.fillVar = fillVar;
    }

    @Override
    public int xPos()
    {
        return this.x;
    }

    @Override
    public int yPos()
    {
        return this.y;
    }

    @Override
    public int getWidth()
    {
        return this.halfSize ? 8 : 16;
    }

    @Override
    public int getHeight()
    {
        return this.halfSize ? 8 : 16;
    }

    @Override
    public boolean isVisible()
    {
        return this.visible;
    }

    @Override
    public String getMessage()
    {
        String displayName = null;
        String displayValue = null;

        if( this.buttonSetting != null && this.currentValue != null )
        {
            final ButtonAppearance buttonAppearance = appearances.get( new EnumPair( this.buttonSetting, this.currentValue ) );
            if( buttonAppearance == null )
            {
                return "No Such Message";
            }
            displayName = buttonAppearance.displayName;
            displayValue = buttonAppearance.displayValue;
        }

        if( displayName != null )
        {
            String name = I18n.format( displayName );
            String value = I18n.format( displayValue );

            if(name.isEmpty())
            {
                name = displayName;
            }
            if(value.isEmpty())
            {
                value = displayValue;
            }

            if( this.fillVar != null )
            {
                value = COMPILE.matcher( value ).replaceFirst( this.fillVar );
            }

            value = PATTERN_NEW_LINE.matcher( value ).replaceAll( "\n" );
            final StringBuilder sb = new StringBuilder( value );

            int i = sb.lastIndexOf( "\n" );
            if( i <= 0 )
            {
                i = 0;
            }
            while( i + 30 < sb.length() && ( i = sb.lastIndexOf( " ", i + 30 ) ) != -1 )
            {
                sb.replace( i, i + 1, "\n" );
            }

            return name + '\n' + sb;
        }
        return null;
    }

    @Override
    public void drawButton(@Nonnull final Minecraft par1Minecraft, final int par2, final int par3, float partialTicks)
    {
        if( this.visible )
        {
            final int iconIndex = this.getIconIndex();
            int relativeX = this.x;
            int relativeY = this.y;

            if ( this.halfSize )
            {
                this.width = 8;
                this.height = 8;

                GL11.glPushMatrix();
                GL11.glTranslatef( this.x, this.y, 0.0F );
                GL11.glScalef( 0.5f, 0.5f, 0.5f );

                relativeX = 0;
                relativeY = 0;
            }

            if( this.enabled )
            {
                GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
            }
            else
            {
                GL11.glColor4f( 0.5f, 0.5f, 0.5f, 1.0f );
            }

            par1Minecraft.renderEngine.bindTexture( FluidCraft.resource("textures/gui/states.png") );
            this.hovered = par2 >= this.x && par3 >= this.y && par2 < this.x + this.width && par3 < this.y + this.height;

            // Base button icon will always be bottom right
            final int baseButtonUV = 16 * ( SPRITE_SHEET_GRID_SIZE - 1 );
            final int textureSize = 16 * SPRITE_SHEET_GRID_SIZE;

            final int overlayU = 16 * ( iconIndex % SPRITE_SHEET_GRID_SIZE );
            final int overlayV = 16 * ( iconIndex / SPRITE_SHEET_GRID_SIZE );

            Gui.drawModalRectWithCustomSizedTexture( relativeX, relativeY, baseButtonUV, baseButtonUV, 16, 16, textureSize, textureSize );
            Gui.drawModalRectWithCustomSizedTexture( relativeX, relativeY, overlayU, overlayV, 16, 16, textureSize, textureSize );

            this.mouseDragged( par1Minecraft, par2, par3 );

            if ( this.halfSize )
            {
                GL11.glPopMatrix();
            }
        }

        GL11.glColor4f( 1.0f, 1.0f, 1.0f, 1.0f );
    }

    private static final class EnumPair
    {

        final String setting;
        final String value;

        EnumPair( final String a, final String b )
        {
            this.setting = a;
            this.value = b;
        }

        @Override
        public int hashCode()
        {
            return this.setting.hashCode() ^ this.value.hashCode();
        }

        @Override
        public boolean equals( final Object obj )
        {
            if( obj == null )
            {
                return false;
            }
            if( this.getClass() != obj.getClass() )
            {
                return false;
            }
            final EnumPair other = (EnumPair) obj;
            return other.setting.equals(this.setting) && other.value.equals(this.value);
        }
    }


    private static class ButtonAppearance
    {
        public int index;
        public String displayName;
        public String displayValue;
    }

}

