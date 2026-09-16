package com.lowdragmc.lowdraglib2.networking.both;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.gui.sync.IUISyncManagerHolder;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

@NoArgsConstructor
public class PacketUIRPCEventReturn implements CustomPacketPayload {
    public static final Identifier ID = LDLib2.id("ui_rpc_event_return");
    public static final Type<PacketUIRPCEventReturn> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketUIRPCEventReturn> CODEC =
            StreamCodec.ofMember(PacketUIRPCEventReturn::write, PacketUIRPCEventReturn::decode);

    public byte[] returnData;

    public PacketUIRPCEventReturn(byte[] returnData) {
        this.returnData = returnData;
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeByteArray(returnData);
    }

    public static PacketUIRPCEventReturn decode(RegistryFriendlyByteBuf buf) {
        var returnData = buf.readByteArray();
        return new PacketUIRPCEventReturn(returnData);
    }

    public static void handle(PacketUIRPCEventReturn packet, net.minecraft.client.player.LocalPlayer player) {
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            var syncManager = syncManagerHolder.getSyncManager();
            if (syncManager == null) return;
            ByteBufUtil.readCustomData(packet.returnData,
                    syncManager::handEventReturn,
                    player.registryAccess());
        }
    }

    public static void handleServer(PacketUIRPCEventReturn packet, net.minecraft.server.level.ServerPlayer player) {
        if (player.containerMenu instanceof IUISyncManagerHolder syncManagerHolder) {
            var syncManager = syncManagerHolder.getSyncManager();
            if (syncManager == null) return;
            ByteBufUtil.readCustomData(packet.returnData,
                    syncManager::handEventReturn,
                    player.registryAccess());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
