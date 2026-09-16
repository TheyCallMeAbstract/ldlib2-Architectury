package com.lowdragmc.lowdraglib2.utils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Helper to replace NeoForge's {@code ValueOutputExtension.store(CompoundTag)} on Fabric.
 *
 * <p>Mirrors NeoForge's implementation exactly: each entry is stored via
 * {@link ExtraCodecs#NBT}, which round-trips any {@code Tag} (including lists, byte
 * arrays and long arrays) without loss.</p>
 *
 * <p>Deliberately outside {@code com.lowdragmc.lowdraglib2.core.mixins.*}: Mixin forbids loading
 * any class from a declared mixin package directly.</p>
 */
public final class ValueOutputHelper {
    private ValueOutputHelper() {}

    /**
     * Writes all entries from {@code tag} into {@code output}.
     */
    public static void storeCompoundTag(ValueOutput output, CompoundTag tag) {
        for (var entry : tag.entrySet()) {
            output.store(entry.getKey(), ExtraCodecs.NBT, entry.getValue());
        }
    }
}
