package com.glodblock.github.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.Name("FluidCraftCore")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("com.glodblock.github.coremod")
public class FCCoreMod implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { FCCoreMod.class.getPackage().getName() + ".FCClassTransformer" };
    }

    @Nullable
    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // NO-OP
    }

    @Nullable
    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
