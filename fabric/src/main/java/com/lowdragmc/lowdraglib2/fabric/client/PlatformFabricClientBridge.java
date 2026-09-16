package com.lowdragmc.lowdraglib2.fabric.client;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

/**
 * Client-only access to the integrated server.
 *
 * <p>Kept out of {@link com.lowdragmc.lowdraglib2.fabric.PlatformFabric} on purpose: resolving
 * {@code Minecraft.getSingleplayerServer()} forces the loader to load
 * {@code net.minecraft.client.server.IntegratedServer}, which the dedicated-server environment
 * cannot. {@code PlatformFabric} only calls this when {@code isClient()} is true, so a dedicated
 * server never class-loads it.
 */
public final class PlatformFabricClientBridge {
    private PlatformFabricClientBridge() {
    }

    @Nullable
    public static MinecraftServer getIntegratedServer() {
        var client = Minecraft.getInstance();
        return client == null ? null : client.getSingleplayerServer();
    }
}
