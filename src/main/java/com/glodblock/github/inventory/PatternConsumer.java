package com.glodblock.github.inventory;

import appeng.api.storage.data.IAEItemStack;

public interface PatternConsumer {

    void acceptPattern(IAEItemStack[] inputs, IAEItemStack[] outputs);

}
