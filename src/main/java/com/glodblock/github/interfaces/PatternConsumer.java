package com.glodblock.github.interfaces;

import appeng.api.storage.data.IAEItemStack;

public interface PatternConsumer {

    void acceptPattern(IAEItemStack[] inputs, IAEItemStack[] outputs);

}