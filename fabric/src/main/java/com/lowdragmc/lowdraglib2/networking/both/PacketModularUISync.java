package com.lowdragmc.lowdraglib2.networking.both;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.sync.IUISyncManagerHolder;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * A packet that contains payload for managed fields.
 */
@NoArgsConstructor
public class PacketModularUISync implements CustomPacketPayload {
    public static final Identifier ID = LDLib2.id("modular_ui_sync");
    public static final Type<PacketModularUISync> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketModularUISync> CODEC =
            StreamCodec.ofMember(PacketModularUISync::write, PacketModularUISync::decode);

    private byte[] data;

    public PacketModularUISync(byte[] data) {
        this.data = data;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeByteArray(data);
    }

    public static PacketModularUISync decode(RegistryFriendlyByteBuf buffer) {
        var data = buffer.readByteArray();
        return new PacketModularUISync(data);
    }

    public static void handleOnClient(PacketModularUISync packet, net.minecraft.client.player.LocalPlayer player) {
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            var syncManager = syncManagerHolder.getSyncManager();
            if (syncManager == null) return;
            ByteBufUtil.readCustomData(packet.data,
                    syncManager::handleSyncPacket,
                    player.registryAccess());
        }
    }

    public static void handleOnServer(PacketModularUISync packet, net.minecraft.server.level.ServerPlayer player) {
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            var syncManager = syncManagerHolder.getSyncManager();
            if (syncManager == null) return;
            ByteBufUtil.readCustomData(packet.data,
                    syncManager::handleSyncPacket,
                    player.registryAccess());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
