package com.lowdragmc.lowdraglib2;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import com.lowdragmc.lowdraglib2.CommonListeners.ModCreativeModeTab;
import com.lowdragmc.lowdraglib2.client.ClientProxy;
import com.lowdragmc.lowdraglib2.core.mixins.MixinPluginShared;

@Mod(LDLib2.MOD_ID)
public class LDLib2NeoForge {

    public LDLib2NeoForge(IEventBus eventBus, ModContainer modContainer) {
        Platform.setInstance(new PlatformNeoForge());
        LDLib2.init();
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            new ClientProxy(eventBus, modContainer);
        } else {
            new CommonProxy(eventBus);
        }
        if (Platform.isDevEnv()) {
            ModCreativeModeTab.register(eventBus);
        }
    }

    public static boolean isIrisLoaded() {
        return MixinPluginShared.IS_IRIS_LOAD;
    }

    public static boolean isOculusLoaded() {
        return MixinPluginShared.IS_OCULUS_LOAD;
    }

    public static boolean isOptifineLoaded() {
        return MixinPluginShared.IS_OPT_LOAD;
    }
}