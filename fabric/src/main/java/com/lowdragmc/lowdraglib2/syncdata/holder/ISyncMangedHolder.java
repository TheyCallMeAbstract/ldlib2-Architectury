package com.lowdragmc.lowdraglib2.syncdata.holder;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.async.AsyncThreadData;
import com.lowdragmc.lowdraglib2.async.IAsyncLogic;
import com.lowdragmc.lowdraglib2.syncdata.ref.IRef;
import com.lowdragmc.lowdraglib2.utils.ByteBufUtil;
import com.lowdragmc.lowdraglib2.utils.TagBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;

import java.util.BitSet;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiConsumer;

/**
 * A holder that can be automatically synced with the client.
 */
public interface ISyncMangedHolder extends IManagedHolder, IAsyncLogic {

    /**
     * Platform-specific callback to send a payload to all players tracking a chunk.
     * Registered by each platform's initializer (NeoForge: PacketDistributor, Fabric: Architectury NetworkManager).
     */
    BiConsumer<ServerLevel, CustomPacketPayload> SYNC_SENDER = null;

    /**
     * Register the platform-specific sync packet sender.
     */
    static void registerSyncSender(BiConsumer<ServerLevel, CustomPacketPayload> sender) {
        // Use Unsafe or a holder pattern — we use a mutable holder below
        SyncSenderHolder.SENDER = sender;
    }

    /**
     * Internal holder for the sync sender callback.
     */
    final class SyncSenderHolder {
        static BiConsumer<ServerLevel, CustomPacketPayload> SENDER;
    }

    /**
     * do a sync now.
     */
    default void sync(boolean force) {
        var rootStorage = this.getRootStorage();
        for (var field : rootStorage.getNonLazyFields()) {
            field.update();
        }
        if (rootStorage.hasDirtySyncFields()) {
            var changed = new BitSet();
            var syncedFields = rootStorage.getSyncFields();
            var serverLevel = getServerLevel();
            var server = serverLevel.getServer();
            if (!Platform.serverSafe(server)) return;
            var data = ByteBufUtil.writeCustomData(buffer -> {
                for (int i = 0; i < syncedFields.length; i++) {
                    var field = syncedFields[i];
                    if (force || field.isSyncDirty()) {
                        changed.set(i);
                        field.readSyncToStream(buffer);
                        field.clearSyncDirty();
                    }
                }
            }, serverLevel.registryAccess());
            try {
                server.executeIfPossible(() -> {
                    if (!Platform.serverSafe(server)) return;
                    var extra = new CompoundTag();
                    writeCustomSyncData(serverLevel.registryAccess(), extra);
                    var packet = createSyncPacket(changed, data, extra);
                    var sender = SyncSenderHolder.SENDER;
                    if (sender != null) {
                        sender.accept(serverLevel, packet);
                    }
                });
            } catch (RejectedExecutionException ignored) {
                // The server can begin shutting down between the safety check and task submission.
            }
        }
    }

    /**
     * Create the platform-specific sync packet.
     * Must return a CustomPacketPayload that can be sent via the registered SYNC_SENDER.
     */
    CustomPacketPayload createSyncPacket(BitSet changed, byte[] data, CompoundTag extra);

    default void passivelySync() {
        sync(false);
    }

    default void writeCustomSyncData(HolderLookup.Provider provider, CompoundTag tag) {
    }

    default void readCustomSyncData(HolderLookup.Provider provider, CompoundTag tag) {
    }

    default String getSyncTag() {
        return "sync";
    }

    default CompoundTag serializeInitialData(HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        var customTag = new CompoundTag();
        writeCustomSyncData(provider, customTag);
        if (!customTag.isEmpty()) {
            tag.put("custom", customTag);
        }

        var list = new ListTag();
        var syncedFields = getRootStorage().getSyncFields();
        var ctx = provider.createSerializationContext(NbtOps.INSTANCE);
        for (IRef<?> syncedField : syncedFields) {
            list.add(TagBuilder.compound().add("d", syncedField.readInitialSync(ctx)).build());
        }
        if (!list.isEmpty()) {
            tag.put("managed", list);
        }
        return tag;
    }

    default void deserializeInitialData(HolderLookup.Provider provider, CompoundTag tag) {
        var customTag = tag.getCompoundOrEmpty("custom");
        readCustomSyncData(provider, customTag);

        var list = tag.getListOrEmpty("managed");
        var syncedFields = getRootStorage().getSyncFields();
        if (syncedFields.length != list.size()) {
            throw new IllegalStateException("Synced fields count mismatch");
        }
        var ctx = provider.createSerializationContext(NbtOps.INSTANCE);
        for (int i = 0; i < list.size(); i++) {
            var data = list.getCompoundOrEmpty(i).get("d");
            syncedFields[i].writeInitialSync(ctx, data);
        }
    }

    default void handleSyncPacket(RegistryAccess registryAccess, BitSet changed, byte[] data, CompoundTag extra) {
        ByteBufUtil.readCustomData(data, buffer -> {
            var storage = getRootStorage();
            var syncedFields = storage.getSyncFields();
            for (int i = 0; i < syncedFields.length; i++) {
                if (changed.get(i)) {
                    var field = syncedFields[i];
                    var key = field.getKey();
                    if (storage.hasSyncListener(key)) {
                        var postStream = storage.notifyFieldUpdate(key, field.readRaw());
                        field.writeSyncFromStream(buffer);
                        postStream.forEach(consumer -> consumer.accept(field.readRaw()));
                    } else {
                        field.writeSyncFromStream(buffer);
                    }
                }
            }
        }, registryAccess);
        readCustomSyncData(registryAccess, extra);
    }

    /// Async
    default boolean isAsyncValid() {
        return true;
    }

    default boolean useAsyncThread() {
        return false;
    }

    default void attachAsyncLogic() {
        if (useAsyncThread()) {
            AsyncThreadData.getOrCreate(getServerLevel()).addAsyncLogic(this);
        }
    }

    default void detachAsyncLogic() {
        if (useAsyncThread()) {
            AsyncThreadData.getOrCreate(getServerLevel()).removeAsyncLogic(this);
        }
    }

    @Override
    default void asyncTick(long periodID) {
        if (Platform.isServerNotSafe()) return;
        if (useAsyncThread() && isAsyncValid()) {
            passivelySync();
        }
    }
}
