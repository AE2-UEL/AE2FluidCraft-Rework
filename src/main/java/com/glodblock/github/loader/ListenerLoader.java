package com.glodblock.github.loader;

import com.glodblock.github.client.ClientHelper;

public class ListenerLoader implements Runnable {

    @Override
    public void run() {
        ClientHelper.register();
    }

}
