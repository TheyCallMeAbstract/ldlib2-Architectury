package com.lowdragmc.lowdraglib2;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Platform abstraction for multi-loader support (NeoForge + Fabric).
 * Each platform module sets the implementation via {@link #setInstance(Platform)}.
 *
 * Design:
 * - Protected instance methods: overridden by PlatformFabric / PlatformNeoForge
 * - Public static methods: convenience delegates that call {@code get().xxx()}
 *   so existing code like {@code Platform.isClient()} continues to work unchanged.
 */
public class Platform {

    private static Platform instance;
    private static final Platform FALLBACK = new Platform();

    public static void setInstance(Platform impl) {
        Platform.instance = impl;
    }

    public static Platform get() {
        return instance != null ? instance : FALLBACK;
    }

    // --- Registry access (set by platform modules) ---

    public static RegistryAccess.Frozen SERVER_REGISTRY_ACCESS = null;
    public static ResourceManager RESOURCE_MANAGER = null;

    // ============================================================
    // Protected instance methods — override in subclasses
    // ============================================================

    protected String platformNameImpl() {
        return "Unknown";
    }

    protected boolean isForgeImpl() {
        return false;
    }

    protected boolean isDevEnvImpl() {
        return false;
    }

    protected boolean isDatagenImpl() {
        return false;
    }

    protected boolean isClientImpl() {
        return false;
    }

    protected boolean isServerImpl() {
        return false;
    }

    protected boolean isModLoadedImpl(String modId) {
        return false;
    }

    protected Path getGamePathImpl() {
        return null;
    }

    protected Minecraft getMinecraftClientImpl() {
        return null;
    }

    protected MinecraftServer getMinecraftServerImpl() {
        return null;
    }

    protected RegistryAccess getFrozenRegistryImpl() {
        return getBlankRegistryAccess();
    }

    protected RegistryAccess getServerRegistryAccessImpl() {
        return getBlankRegistryAccess();
    }

    protected RegistryAccess getClientRegistryAccessImpl() {
        return getBlankRegistryAccess();
    }

    protected void executeOnClientImpl(Runnable runnable) {
        var client = getMinecraftClient();
        if (client != null) {
            client.execute(runnable);
        }
    }

    protected void executeOnServerImpl(Runnable runnable) {
        if (isServer()) {
            getMinecraftServer().execute(runnable);
        }
    }

    protected boolean isServerNotSafeImpl() {
        if (isClient()) {
            var minecraft = getMinecraftClient();
            return minecraft == null || minecraft.getConnection() == null;
        } else {
            var server = getMinecraftServer();
            return !serverSafe(server) || server.isCurrentlySaving();
        }
    }

    protected boolean serverSafeImpl(MinecraftServer server) {
        return server != null && !server.isStopped() && !server.isShutdown() && server.isRunning();
    }

    // ============================================================
    // Public static delegates — call through to instance methods
    // ============================================================

    public static String platformName() {
        return get().platformNameImpl();
    }

    public static boolean isForge() {
        return get().isForgeImpl();
    }

    public static boolean isDevEnv() {
        return get().isDevEnvImpl();
    }

    public static boolean isDatagen() {
        return get().isDatagenImpl();
    }

    public static boolean isClient() {
        return get().isClientImpl();
    }

    public static boolean isServer() {
        return get().isServerImpl();
    }

    public static boolean isModLoaded(String modId) {
        return get().isModLoadedImpl(modId);
    }

    public static Path getGamePath() {
        return get().getGamePathImpl();
    }

    public static Minecraft getMinecraftClient() {
        return get().getMinecraftClientImpl();
    }

    public static MinecraftServer getMinecraftServer() {
        return get().getMinecraftServerImpl();
    }

    public static RegistryAccess getFrozenRegistry() {
        return get().getFrozenRegistryImpl();
    }

    public static RegistryAccess getServerRegistryAccess() {
        return get().getServerRegistryAccessImpl();
    }

    public static RegistryAccess getClientRegistryAccess() {
        return get().getClientRegistryAccessImpl();
    }

    public static void executeOnClient(Runnable runnable) {
        get().executeOnClientImpl(runnable);
    }

    public static void executeOnServer(Runnable runnable) {
        get().executeOnServerImpl(runnable);
    }

    public static boolean isServerNotSafe() {
        return get().isServerNotSafeImpl();
    }

    public static boolean serverSafe(MinecraftServer server) {
        return get().serverSafeImpl(server);
    }

    public static boolean serverSafe() {
        return serverSafe(getMinecraftServer());
    }

    // ============================================================
    // Internal
    // ============================================================

    protected RegistryAccess getBlankRegistryAccess() {
        try {
            return RegistryAccess.fromRegistryOfRegistries(net.minecraft.core.registries.BuiltInRegistries.REGISTRY);
        } catch (Throwable e) {
            return new RegistryAccess.Frozen() {
                @Override
                public <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
                    return Optional.empty();
                }

                @Override
                public Stream<RegistryEntry<?>> registries() {
                    return Stream.empty();
                }
            };
        }
    }
}
