package com.lowdragmc.lowdraglib2;

import com.lowdragmc.lowdraglib2.gui.ui.style.PropertyRegistry;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.nodegraphtookit.api.type.TypeHandles;
import com.lowdragmc.lowdraglib2.plugin.ILDLibPlugin;
import com.lowdragmc.lowdraglib2.plugin.LDLibPlugin;
import com.lowdragmc.lowdraglib2.syncdata.AccessorRegistries;
import com.lowdragmc.lowdraglib2.utils.ReflectionUtils;

/**
 * Fabric counterpart to NeoForge's {@code CommonProxy}. Performs the platform-agnostic
 * registration/initialization that the NeoForge proxy owns, using Architectury as the
 * cross-platform medium.
 */
public final class FabricCommonProxy {
    private FabricCommonProxy() {
    }

    public static void init() {
        LDLib2Registries.init();
        AccessorRegistries.init();
        RPCPacketDistributor.init();
        PropertyRegistry.init();
        TypeHandles.init();
        loadPlugins();
    }

    private static void loadPlugins() {
        ReflectionUtils.findAnnotationClasses(LDLibPlugin.class, data -> true, clazz -> {
            try {
                if (clazz.getConstructor().newInstance() instanceof ILDLibPlugin plugin) {
                    plugin.onLoad();
                }
            } catch (Throwable throwable) {
                LDLib2.LOGGER.error("Failed to load plugin {}", clazz.getName(), throwable);
            }
        }, () -> {
        });
    }
}
