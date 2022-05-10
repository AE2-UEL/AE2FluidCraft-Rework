package com.glodblock.github.loader;

import com.glodblock.github.client.render.ItemDropRender;
import com.glodblock.github.client.render.ItemPacketRender;

public class RenderLoader implements Runnable {

    @Override
    public void run() {
        new ItemDropRender();
        new ItemPacketRender();
    }

}
