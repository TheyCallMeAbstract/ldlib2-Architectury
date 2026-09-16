package com.lowdragmc.lowdraglib2;

import com.lowdragmc.lowdraglib2.async.AsyncThreadData;
import com.lowdragmc.lowdraglib2.fabric.PlatformFabric;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import net.minecraft.server.MinecraftServer;

/**
 * Fabric counterpart to NeoForge's {@code CommonListeners}. Registers the server-command
 * callback and the server/world lifecycle callbacks that release {@link AsyncThreadData}
 * executor services.
 *
 * <p>{@code Platform.SERVER_REGISTRY_ACCESS} is intentionally not touched here: on Fabric it
 * is owned by {@code ReloadableServerResourcesMixin}.
 */
public final class FabricCommonListeners {
    private FabricCommonListeners() {
    }

    public static void init() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) ->
                ServerCommands.createServerCommands().forEach(dispatcher::register));

        LifecycleEvent.SERVER_STARTING.register(PlatformFabric::setServer);
        LifecycleEvent.SERVER_STOPPING.register(FabricCommonListeners::releaseLevelExecutors);
        LifecycleEvent.SERVER_STOPPING.register(server -> PlatformFabric.setServer(null));
        LifecycleEvent.SERVER_STOPPED.register(FabricCommonListeners::releaseLevelExecutors);
        LifecycleEvent.SERVER_LEVEL_UNLOAD.register(level -> {
            if (!level.isClientSide()) {
                AsyncThreadData.getOrCreate(level).releaseExecutorService();
            }
        });
    }

    private static void releaseLevelExecutors(MinecraftServer server) {
        for (var level : server.getAllLevels()) {
            if (!level.isClientSide()) {
                AsyncThreadData.getOrCreate(level).releaseExecutorService();
            }
        }
    }
}
