package com.lowdragmc.lowdraglib2.networking.both;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.sync.IUISyncManagerHolder;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

@NoArgsConstructor
public class PacketUIRPCEvent {
    public static final Identifier ID = LDLib2.id("ui_rpc_event");
    public byte[] eventData;

    public PacketUIRPCEvent(byte[] eventData) {
        this.eventData = eventData;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeByteArray(eventData);
    }

    public static PacketUIRPCEvent decode(RegistryFriendlyByteBuf buf) {
        var eventData = buf.readByteArray();
        return new PacketUIRPCEvent(eventData);
    }

    public static void handle(PacketUIRPCEvent packet, net.minecraft.server.level.ServerPlayer player) {
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            var syncManager = syncManagerHolder.getSyncManager();
            if (syncManager == null) return;
            ByteBufUtil.readCustomData(packet.eventData,
                    syncManager::handEvent,
                    player.registryAccess());
        }
    }

    public static void handleClient(PacketUIRPCEvent packet, net.minecraft.client.player.LocalPlayer player) {
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            var syncManager = syncManagerHolder.getSyncManager();
            if (syncManager == null) return;
            ByteBufUtil.readCustomData(packet.eventData,
                    syncManager::handEvent,
                    player.registryAccess());
        }
    }
}
