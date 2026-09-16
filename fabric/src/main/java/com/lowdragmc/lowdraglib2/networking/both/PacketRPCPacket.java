package com.lowdragmc.lowdraglib2.networking.both;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.networking.rpc.RPCPacketDistributor;
import com.lowdragmc.lowdraglib2.syncdata.rpc.RPCSender;
import lombok.NoArgsConstructor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * a packet that contains payload for managed fields
 */
@NoArgsConstructor
public class PacketRPCPacket implements CustomPacketPayload {
    public static final Identifier ID = LDLib2.id("rpc_packet");
    public static final Type<PacketRPCPacket> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, PacketRPCPacket> CODEC = StreamCodec.ofMember(PacketRPCPacket::write, PacketRPCPacket::decode);

    private String packetID;

    private byte[] data;

    public PacketRPCPacket(String packetID, byte[] data) {
        this.packetID = packetID;
        this.data = data;
    }

    public static PacketRPCPacket of(String packetID, byte[] data) {
        return new PacketRPCPacket(packetID, data);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeUtf(packetID);
        buf.writeByteArray(data);
    }

    public static PacketRPCPacket decode(RegistryFriendlyByteBuf buffer) {
        var packetID = buffer.readUtf();
        var data = buffer.readByteArray();
        return new PacketRPCPacket(packetID, data);
    }

    public static void handleOnClient(PacketRPCPacket packet, LocalPlayer player) {
        var handler = RPCPacketDistributor.getPacketHandler(packet.packetID);
        if (handler == null) {
            LDLib2.LOGGER.warn("Received rpc payload packet from server of a non registered handler: {}, which is an inconsistency between client and server.",
                    packet.packetID);
            return;
        }
        var sender = RPCSender.ofServer();
        var data = handler.bytes2Args(packet.data);
        handler.handler(sender, data);
    }

    public static void handleOnServer(PacketRPCPacket packet, ServerPlayer player) {
        var handler = RPCPacketDistributor.getPacketHandler(packet.packetID);
        if (handler == null) {
            LDLib2.LOGGER.warn("Received rpc payload packet from client sender {} of a non registered handler: {}, which may be an inconsistency between client and server, or even a potential attack!",
                    player, packet.packetID);
            return;
        }
        var sender = RPCSender.ofClient(player);
        var data = handler.bytes2Args(packet.data);
        handler.handler(sender, data);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
