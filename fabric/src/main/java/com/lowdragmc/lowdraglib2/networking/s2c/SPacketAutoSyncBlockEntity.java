package com.lowdragmc.lowdraglib2.networking.s2c;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.BitSet;
import java.util.Objects;

/**
 * A packet that syncs managed block entity fields to the client.
 * Implements CustomPacketPayload for Architectury networking.
 */
public class SPacketAutoSyncBlockEntity implements CustomPacketPayload {
    public static final Identifier ID = LDLib2.id("auto_sync_block_entity");
    public static final Type<SPacketAutoSyncBlockEntity> TYPE = new Type<>(ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, SPacketAutoSyncBlockEntity> CODEC =
            StreamCodec.ofMember(SPacketAutoSyncBlockEntity::write, SPacketAutoSyncBlockEntity::decode);

    public final BlockPos pos;
    private final BlockEntityType<?> blockEntityType;
    private final BitSet changed;
    private final byte[] data;
    private final CompoundTag extra;

    private SPacketAutoSyncBlockEntity(BlockEntityType<?> type, BlockPos pos, BitSet changed, byte[] data, CompoundTag extra) {
        this.pos = pos;
        this.blockEntityType = type;
        this.changed = changed;
        this.data = data;
        this.extra = extra;
    }

    /**
     * Create a packet to sync fields of a block entity.
     */
    public static SPacketAutoSyncBlockEntity of(ISyncBlockEntity tile, BitSet changed, byte[] data, CompoundTag extra) {
        return new SPacketAutoSyncBlockEntity(tile.getSelf().getType(), tile.getSelf().getBlockPos(), changed, data, extra);
    }

    /**
     * Process a received sync packet on the client side.
     */
    public static void processPacket(ISyncBlockEntity blockEntity, SPacketAutoSyncBlockEntity packet) {
        if (blockEntity.getSelf().getType() != packet.blockEntityType) {
            LDLib2.LOGGER.warn("Block entity type mismatch in managed payload packet!");
            return;
        }
        var level = blockEntity.getSelf().getLevel();
        if (level == null) return;
        blockEntity.handleSyncPacket(level.registryAccess(), packet.changed, packet.data, packet.extra);
    }

    /**
     * Handle this packet on the client side — called by the Architectury receiver.
     */
    public static void handleOnClient(SPacketAutoSyncBlockEntity packet) {
        var level = Minecraft.getInstance().level;
        if (level != null) {
            if (level.getBlockEntity(packet.pos) instanceof ISyncBlockEntity syncBlockEntity) {
                processPacket(syncBlockEntity, packet);
            }
        }
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeIdentifier(Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntityType)));
        buf.writeByteArray(changed.toByteArray());
        buf.writeByteArray(data);
        buf.writeNbt(extra);
    }

    public static SPacketAutoSyncBlockEntity decode(RegistryFriendlyByteBuf buffer) {
        var pos = buffer.readBlockPos();
        var blockEntityType = BuiltInRegistries.BLOCK_ENTITY_TYPE.getValue(buffer.readIdentifier());
        var changed = BitSet.valueOf(buffer.readByteArray());
        var data = buffer.readByteArray();
        var extra = buffer.readNbt();
        return new SPacketAutoSyncBlockEntity(blockEntityType, pos, changed, data, extra);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
