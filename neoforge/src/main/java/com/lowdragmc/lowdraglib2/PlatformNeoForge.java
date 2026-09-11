package com.lowdragmc.lowdraglib2;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import net.minecraft.resources.ResourceKey;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

public class PlatformNeoForge extends Platform {

    @Override
    protected String platformNameImpl() {
        return "NeoForge";
    }

    @Override
    protected boolean isForgeImpl() {
        return true;
    }

    @Override
    protected boolean isDevEnvImpl() {
        return !FMLLoader.getCurrent().isProduction();
    }

    @Override
    protected boolean isDatagenImpl() {
        return DatagenModLoader.isRunningDataGen();
    }

    @Override
    protected boolean isModLoadedImpl(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    protected boolean isClientImpl() {
        return FMLEnvironment.getDist() == Dist.CLIENT;
    }

    @Override
    protected boolean isServerImpl() {
        return FMLEnvironment.getDist() == Dist.DEDICATED_SERVER;
    }

    @Override
    protected MinecraftServer getMinecraftServerImpl() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    protected Minecraft getMinecraftClientImpl() {
        return isClient() ? Minecraft.getInstance() : null;
    }

    @Override
    protected Path getGamePathImpl() {
        return FMLLoader.getCurrent().getGameDir();
    }

    // --- Registry access ---

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
        return serverRegistryAccess == null ? getClientRegistryAccessImpl() : serverRegistryAccess;
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
                    Optional<Registry<E>> registry = access.lookup(registryKey);
                    if (registry.isPresent()) {
                        return registry;
                    }
                }
                return Optional.empty();
            }

            @Override
            public Stream<RegistryEntry<?>> registries() {
                return Arrays.stream(accesses).flatMap(RegistryAccess::registries);
            }
        };
    }

    // --- Execution helpers ---

    @Override
    protected void executeOnClientImpl(Runnable runnable) {
        var minecraft = getMinecraftClient();
        if (minecraft != null) {
            minecraft.execute(runnable);
        }
    }

    @Override
    protected void executeOnServerImpl(Runnable runnable) {
        if (isServer()) {
            getMinecraftServer().execute(runnable);
        }
    }

    // --- Safety checks ---

    @Override
    protected boolean isServerNotSafeImpl() {
        if (isClient()) {
            var minecraft = getMinecraftClient();
            return minecraft == null || minecraft.getConnection() == null;
        } else {
            var server = getMinecraftServer();
            return !serverSafe(server) || server.isCurrentlySaving();
        }
    }

    @Override
    protected boolean serverSafeImpl(MinecraftServer server) {
        return server != null && !server.isStopped() && !server.isShutdown() && server.isRunning();
    }
}
