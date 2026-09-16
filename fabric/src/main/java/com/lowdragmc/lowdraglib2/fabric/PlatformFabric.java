package com.lowdragmc.lowdraglib2.fabric;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.fabric.client.PlatformFabricClientBridge;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.core.RegistryAccess;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class PlatformFabric extends Platform {

    /** The dedicated/integrated server, captured by {@code FabricCommonListeners}' lifecycle hooks. */
    private static volatile MinecraftServer server;

    public static void setServer(MinecraftServer runningServer) {
        server = runningServer;
    }

    @Override
    protected String platformNameImpl() {
        return "Fabric";
    }

    @Override
    protected boolean isForgeImpl() {
        return false;
    }

    @Override
    protected boolean isDevEnvImpl() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    protected boolean isDatagenImpl() {
        return false;
    }

    @Override
    protected boolean isModLoadedImpl(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    protected boolean isClientImpl() {
        return FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT;
    }

    @Override
    protected boolean isServerImpl() {
        return FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.SERVER;
    }

    @Override
    protected Minecraft getMinecraftClientImpl() {
        return isClient() ? Minecraft.getInstance() : null;
    }

    @Override
    protected MinecraftServer getMinecraftServerImpl() {
        // On a client the live server is the integrated one; there is no static hook to it.
        // Reached through a client-only bridge: naming Minecraft.getSingleplayerServer() here
        // would force the loader to resolve IntegratedServer, which a dedicated server cannot
        // load. isClient() is false there, so the bridge class is never initialised.
        if (isClient()) {
            var integrated = PlatformFabricClientBridge.getIntegratedServer();
            if (integrated != null) return integrated;
        }
        return server;
    }

    @Override
    protected Path getGamePathImpl() {
        return FabricLoader.getInstance().getGameDir();
    }

    public ResourceManager getResourceProvider() {
        return null;
    }

    @Override
    protected RegistryAccess getFrozenRegistryImpl() {
        RegistryAccess.Frozen serverRegistryAccess = SERVER_REGISTRY_ACCESS;
        if (isServerImpl()) {
            return serverRegistryAccess == null ? getBlankRegistryAccess() : serverRegistryAccess;
        } else if (isClientImpl()) {
            var minecraft = getMinecraftClient();
            if (minecraft != null && minecraft.getConnection() != null) {
                return getRegistryFromMultipleSources(minecraft.getConnection().registryAccess(), serverRegistryAccess);
            }
        }
        return serverRegistryAccess != null ? serverRegistryAccess : getClientRegistryAccessImpl();
    }

    @Override
    protected RegistryAccess getServerRegistryAccessImpl() {
        return SERVER_REGISTRY_ACCESS == null ? getBlankRegistryAccess() : SERVER_REGISTRY_ACCESS;
    }

    @Override
    protected RegistryAccess getClientRegistryAccessImpl() {
        var minecraft = getMinecraftClient();
        if (minecraft != null && minecraft.getConnection() != null) {
            var frozen = minecraft.getConnection().registryAccess().freeze();
            return frozen != null ? frozen : getBlankRegistryAccess();
        }
        return SERVER_REGISTRY_ACCESS == null ? getBlankRegistryAccess() : SERVER_REGISTRY_ACCESS;
    }

    private RegistryAccess getRegistryFromMultipleSources(RegistryAccess... accesses) {
        return new RegistryAccess() {
            @Override
            public <E> Optional<Registry<E>> lookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
                for (RegistryAccess access : accesses) {
                    if (access == null) continue;
                    Optional<Registry<E>> registry = access.lookup(registryKey);
                    if (registry.isPresent()) {
                        return registry;
                    }
                }
                return Optional.empty();
            }

            @Override
            public Stream<RegistryEntry<?>> registries() {
                return Arrays.stream(accesses)
                        .filter(access -> access != null)
                        .flatMap(RegistryAccess::registries);
            }
        };
    }

    @Override
    protected void executeOnClientImpl(Runnable runnable) {
        var client = getMinecraftClient();
        if (client != null) {
            client.execute(runnable);
        }
    }

    @Override
    protected void executeOnServerImpl(Runnable runnable) {
        // Fabric: no direct server access from static context
    }

    @Override
    protected boolean isServerNotSafeImpl() {
        return true;
    }

    @Override
    protected boolean serverSafeImpl(MinecraftServer server) {
        return false;
    }
}
