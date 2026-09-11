package com.lowdragmc.lowdraglib2.fabric;

import com.lowdragmc.lowdraglib2.Platform;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.core.RegistryAccess;
import java.nio.file.Path;

public class PlatformFabric extends Platform {

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
        return null;
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
        return getBlankRegistryAccess();
    }

    @Override
    protected RegistryAccess getServerRegistryAccessImpl() {
        return getBlankRegistryAccess();
    }

    @Override
    protected RegistryAccess getClientRegistryAccessImpl() {
        return getBlankRegistryAccess();
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
