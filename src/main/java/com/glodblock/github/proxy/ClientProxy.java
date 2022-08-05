package com.glodblock.github.proxy;

import com.glodblock.github.loader.ListenerLoader;
import com.glodblock.github.loader.RenderLoader;
import com.glodblock.github.nei.recipes.DefaultExtractorLoader;
import com.glodblock.github.util.ModAndClassUtil;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event){
        super.preInit(event);
    }

    @Override
    public void init(FMLInitializationEvent event){
        super.init(event);
        (new ListenerLoader()).run();
        (new RenderLoader()).run();
    }

    @Override
    public void postInit(FMLPostInitializationEvent event){
        super.postInit(event);
        if (ModAndClassUtil.NEI) {
            (new DefaultExtractorLoader()).run();
        }
    }
}
