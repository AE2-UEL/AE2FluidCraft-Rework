package com.glodblock.github.client.textures;

import com.glodblock.github.util.NameConst;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

public enum FCPartsTexture {

    PartFluidPatternTerminal_Bright("pattern_terminal_bright"), PartFluidPatternTerminal_Dark("pattern_terminal_dark"),
    PartFluidPatternTerminal_Colored("pattern_terminal_medium"), PartTerminalBroad("terminal_broad"),
    PartFluidImportBus("fluid_import_face"), PartFluidExportBus("fluid_export_face"),
    BlockFluidInterfaceAlternate_Arrow("fluid_interface_arrow"), BlockInterfaceAlternate("fluid_interface_a"),
    BlockInterface_Face("fluid_interface");

    private final String name;
    public net.minecraft.util.IIcon IIcon;

    FCPartsTexture( final String name )
    {
        this.name = name;
    }

    public static ResourceLocation GuiTexture(final String string)
    {
        return null;
    }

    @SideOnly( Side.CLIENT )
    public static IIcon getMissing()
    {
        return ( (TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture( TextureMap.locationBlocksTexture ) ).getAtlasSprite( "missingno" );
    }

    public String getName()
    {
        return this.name;
    }

    public IIcon getIcon()
    {
        return this.IIcon;
    }

    public void registerIcon( final TextureMap map )
    {
        this.IIcon = map.registerIcon( NameConst.RES_KEY + this.name );
    }
}
