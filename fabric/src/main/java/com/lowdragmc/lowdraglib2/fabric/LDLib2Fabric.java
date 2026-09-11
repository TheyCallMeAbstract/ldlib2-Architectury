package com.lowdragmc.lowdraglib2.fabric;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import net.fabricmc.api.ModInitializer;

public final class LDLib2Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Platform.setInstance(new PlatformFabric());
        LDLib2.init();
    }
}
