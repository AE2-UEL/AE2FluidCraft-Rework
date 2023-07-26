package com.glodblock.github.integration.opencomputer;

import li.cil.oc.api.Driver;

public class OCInit {

    public static void run() {
        Driver.add(new DriverBlockDualInterface());
        Driver.add(new DriverBlockDualInterface.Provider());
        Driver.add(new DriverPartDualInterface());
        Driver.add(new DriverPartDualInterface.Provider());
    }

}
