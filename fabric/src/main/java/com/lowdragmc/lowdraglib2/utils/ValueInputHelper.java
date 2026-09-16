package com.lowdragmc.lowdraglib2.utils;

import com.lowdragmc.lowdraglib2.core.mixins.accessor.TagValueInputAccessor;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.TagValueInput;

/**
 * Helper to expose the ops/lookup/input held by a {@link TagValueInput} on Fabric.
 * Mirrors {@link ValueOutputHelper}: it keeps the accessor interface pure by
 * centralising the cast and the reads here.
 *
 * <p>Deliberately outside {@code com.lowdragmc.lowdraglib2.core.mixins.*}: Mixin forbids loading
 * any class from a declared mixin package directly, and callers like {@link PersistedParser} are
 * not mixins.</p>
 */
public final class ValueInputHelper {
    private ValueInputHelper() {}

    public static DynamicOps<Tag> getOps(TagValueInput input) {
        return ((TagValueInputAccessor) input).ldlib2$getContext().ops();
    }

    public static HolderLookup.Provider getLookup(TagValueInput input) {
        return ((TagValueInputAccessor) input).ldlib2$getContext().lookup();
    }

    public static CompoundTag getInput(TagValueInput input) {
        return ((TagValueInputAccessor) input).ldlib2$getInput();
    }
}
