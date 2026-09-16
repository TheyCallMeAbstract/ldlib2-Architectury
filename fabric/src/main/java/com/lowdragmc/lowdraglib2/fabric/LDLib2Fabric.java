package com.lowdragmc.lowdraglib2.fabric;

import com.lowdragmc.lowdraglib2.FabricCommonListeners;
import com.lowdragmc.lowdraglib2.FabricCommonProxy;
import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.event.LDLib2Events;
import com.lowdragmc.lowdraglib2.gui.factory.LDMenuTypes;
import com.lowdragmc.lowdraglib2.networking.both.PacketModularUISync;
import com.lowdragmc.lowdraglib2.networking.both.PacketRPCBlockEntity;
import com.lowdragmc.lowdraglib2.networking.both.PacketRPCPacket;
import com.lowdragmc.lowdraglib2.networking.both.PacketUIRPCEvent;
import com.lowdragmc.lowdraglib2.networking.both.PacketUIRPCEventReturn;
import com.lowdragmc.lowdraglib2.networking.s2c.SPacketAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.holder.ISyncMangedHolder;
import com.lowdragmc.lowdraglib2.uitest.MPServerBootstrap;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.level.ServerPlayer;

public final class LDLib2Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Platform.setInstance(new PlatformFabric());
        LDLib2.init();
        LDLib2Events.init();

        // Mirror NeoForge's CommonProxy + CommonListeners registration/lifecycle hooks.
        FabricCommonProxy.init();
        FabricCommonListeners.init();

        // Arm the multi-process test server when this is the dedicated server of a runMpTest run.
        // Must live in the common entrypoint: a dedicated server never runs client initializers.
        if (Platform.isDevEnv()) {
            MPServerBootstrap.register();
        }

        // Register menu types via Architectury DeferredRegister
        LDMenuTypes.init();

        // Block entity types registration removed for Fabric MC 26.1
        // since BlockEntityType constructor is private and cannot be accessed.

        // S2C payload types — dedicated server only.
        //
        // Architectury's NetworkManager.registerReceiver(Side.S2C, ...) registers the payload type
        // itself, and LDLib2FabricClient calls it for every S2C packet on a physical client.
        // Registering the same type here as well would be a duplicate registration in one process
        // ("Packet type ... is already registered"). A dedicated server runs no client entrypoint,
        // so it is the one place that still needs the types registered explicitly in order to send.
        //
        // The S2C receivers cannot move here instead: their handlers take LocalPlayer, so
        // referencing them from this (main) initializer would load a client class on a server.
        if (Platform.isServer()) {
            NetworkManager.registerS2CPayloadType(SPacketAutoSyncBlockEntity.TYPE, SPacketAutoSyncBlockEntity.CODEC);
            // Register S2C payload types for bidirectional UI packets
            NetworkManager.registerS2CPayloadType(PacketModularUISync.TYPE, PacketModularUISync.CODEC);
            NetworkManager.registerS2CPayloadType(PacketUIRPCEvent.TYPE, PacketUIRPCEvent.CODEC);
            NetworkManager.registerS2CPayloadType(PacketUIRPCEventReturn.TYPE, PacketUIRPCEventReturn.CODEC);
            NetworkManager.registerS2CPayloadType(PacketRPCBlockEntity.TYPE, PacketRPCBlockEntity.CODEC);
            NetworkManager.registerS2CPayloadType(PacketRPCPacket.TYPE, PacketRPCPacket.CODEC);
        }

        // Register C2S receivers for bidirectional UI packets
        NetworkManager.registerReceiver(NetworkManager.Side.C2S,
                PacketModularUISync.TYPE, PacketModularUISync.CODEC,
                (payload, context) -> context.queue(() -> {
                    var player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketModularUISync.handleOnServer(payload, serverPlayer);
                    }
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S,
                PacketUIRPCEvent.TYPE, PacketUIRPCEvent.CODEC,
                (payload, context) -> context.queue(() -> {
                    var player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketUIRPCEvent.handle(payload, serverPlayer);
                    }
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S,
                PacketUIRPCEventReturn.TYPE, PacketUIRPCEventReturn.CODEC,
                (payload, context) -> context.queue(() -> {
                    var player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketUIRPCEventReturn.handleServer(payload, serverPlayer);
                    }
                }));

        // Register C2S receivers for RPC packets
        NetworkManager.registerReceiver(NetworkManager.Side.C2S,
                PacketRPCBlockEntity.TYPE, PacketRPCBlockEntity.CODEC,
                (payload, context) -> context.queue(() -> {
                    var player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketRPCBlockEntity.handleOnServer(payload, serverPlayer);
                    }
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.C2S,
                PacketRPCPacket.TYPE, PacketRPCPacket.CODEC,
                (payload, context) -> context.queue(() -> {
                    var player = context.getPlayer();
                    if (player instanceof ServerPlayer serverPlayer) {
                        PacketRPCPacket.handleOnServer(payload, serverPlayer);
                    }
                }));

        // Register the sync sender: sends to all players within render distance of the block
        ISyncMangedHolder.registerSyncSender((serverLevel, packet) -> {
            // Find the block pos from the packet — SPacketAutoSyncBlockEntity has a public pos field
            if (packet instanceof SPacketAutoSyncBlockEntity syncPacket) {
                var pos = syncPacket.pos;
                int chunkX = pos.getX() >> 4;
                int chunkZ = pos.getZ() >> 4;
                // Send to all players tracking this chunk area
                for (ServerPlayer player : serverLevel.players()) {
                    var playerChunkPos = player.chunkPosition();
                    int viewDist = serverLevel.getServer().getPlayerList().getViewDistance();
                    if (Math.abs(playerChunkPos.x() - chunkX) <= viewDist &&
                        Math.abs(playerChunkPos.z() - chunkZ) <= viewDist) {
                        NetworkManager.sendToPlayer(player, syncPacket);
                    }
                }
            }
        });
    }
}
