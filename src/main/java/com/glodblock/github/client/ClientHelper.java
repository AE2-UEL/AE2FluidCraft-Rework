package com.glodblock.github.client;

import com.glodblock.github.client.textures.FCPartsTexture;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

public class ClientHelper {

    @SubscribeEvent
    public void updateTextureSheet( final TextureStitchEvent.Pre ev )
    {
        if( ev.map.getTextureType() == 0 )
        {
            for( final FCPartsTexture cb : FCPartsTexture.values() )
            {
                cb.registerIcon( ev.map );
            }
        }
    }

    public static void register() {
        ClientHelper handler = new ClientHelper();
        MinecraftForge.EVENT_BUS.register(handler);
        FMLCommonHandler.instance().bus().register(handler);
    }

}
