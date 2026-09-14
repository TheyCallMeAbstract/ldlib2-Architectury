package com.lowdragmc.lowdraglib2.fabric;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.networking.s2c.SPacketAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.holder.ISyncMangedHolder;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.level.ServerPlayer;

public final class LDLib2Fabric implements ModInitializer {
    @Override
    public void onInitialize() {
        Platform.setInstance(new PlatformFabric());
        LDLib2.init();

        // Register S2C payload type (server needs to know the type to send it)
        NetworkManager.registerS2CPayloadType(SPacketAutoSyncBlockEntity.TYPE, SPacketAutoSyncBlockEntity.CODEC);

        // Register the sync sender: sends to all players within render distance of the block
        ISyncMangedHolder.registerSyncSender((serverLevel, packet) -> {
            // Find the block pos from the packet — SPacketAutoSyncBlockEntity has a public pos field
            if (packet instanceof SPacketAutoSyncBlockEntity syncPacket) {
                var pos = syncPacket.pos;
                int chunkX = pos.getX() >> 4;
                int chunkZ = pos.getZ() >> 4;
                // Send to all players tracking this chunk area
                for (ServerPlayer player : serverLevel.players()) {
                    // Check if player is tracking this chunk (within server view distance)
                    var watchedChunk = player.getWatchedChunk();
                    if (watchedChunk != null) {
                        int viewDist = serverLevel.getServer().getPlayerList().getViewDistance();
                        if (Math.abs(watchedChunk.x - chunkX) <= viewDist &&
                            Math.abs(watchedChunk.z - chunkZ) <= viewDist) {
                            NetworkManager.sendToPlayer(player, syncPacket);
                        }
                    }
                }
            }
        });
    }
}
