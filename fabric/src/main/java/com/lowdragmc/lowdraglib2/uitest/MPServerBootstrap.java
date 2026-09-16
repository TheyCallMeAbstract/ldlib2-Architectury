package com.lowdragmc.lowdraglib2.uitest;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.uitest.mp.MPRunConfig;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Arms {@link MPServerRunner} when this process is the dedicated server of a multi-process run —
 * that is, when the {@code runMpServer} Gradle run set the mptest role/hub system properties.
 * Inert everywhere else, including ordinary {@code runServer} launches.
 *
 * <p>NeoForge's {@code @EventBusSubscriber} has no Fabric equivalent, so registration is explicit.
 * The Architectury common events used here are verified present in
 * {@code architectury-fabric-20.0.12.jar}:
 * <pre>
 * LifecycleEvent.SERVER_STARTED : Event&lt;ServerState&gt;   // void stateChanged(MinecraftServer)
 * LifecycleEvent.SERVER_STOPPED : Event&lt;ServerState&gt;
 * TickEvent.SERVER_POST         : Event&lt;TickEvent.Server&gt; // void tick(MinecraftServer)
 * </pre>
 *
 * <p>TODO(other agent): call {@code MPServerBootstrap.register()} from the common entrypoint
 * {@code com.lowdragmc.lowdraglib2.fabric.LDLib2Fabric.onInitialize()} (after
 * {@code Platform.setInstance(...)}), which is the only entrypoint that also runs on a dedicated
 * server. This file is intentionally not wired from the client entrypoint, because a dedicated
 * server never runs client initializers.
 */
public final class MPServerBootstrap {

    @Nullable
    private static MPServerRunner runner;

    private MPServerBootstrap() {
    }

    /** Registers the server lifecycle/tick listeners. Idempotent. */
    public static void register() {
        LifecycleEvent.SERVER_STARTED.register(MPServerBootstrap::onServerStarted);
        LifecycleEvent.SERVER_STOPPED.register(MPServerBootstrap::onServerStopped);
        TickEvent.SERVER_POST.register(MPServerBootstrap::onServerTick);
    }

    private static void onServerStarted(MinecraftServer server) {
        if (!Platform.isDevEnv()) return;
        var config = MPRunConfig.fromSystemProperties();
        if (config == null || !config.isServer()) return;
        runner = new MPServerRunner(config, server);
        runner.start();
    }

    private static void onServerTick(MinecraftServer server) {
        if (runner != null) {
            runner.tick();
        }
    }

    private static void onServerStopped(MinecraftServer server) {
        if (runner != null) {
            runner.close();
            runner = null;
        }
    }
}
