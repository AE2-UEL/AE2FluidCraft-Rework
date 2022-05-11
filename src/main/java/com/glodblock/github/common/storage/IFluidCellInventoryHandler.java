package com.glodblock.github.common.storage;

import appeng.api.config.IncludeExclude;

public interface IFluidCellInventoryHandler {

    IFluidCellInventory getCellInv();

    boolean isPreformatted();

    IncludeExclude getIncludeExcludeMode();

}
