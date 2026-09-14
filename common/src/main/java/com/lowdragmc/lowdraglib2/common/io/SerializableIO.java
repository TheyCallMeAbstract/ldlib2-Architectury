package com.lowdragmc.lowdraglib2.common.io;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

/**
 * Cross-platform replacement for NeoForge's {@code ValueIOSerializable}.
 * <p>
 * Classes implementing this interface can serialize/deserialize their state
 * using vanilla's {@link ValueInput}/{@link ValueOutput} system.
 * <p>
 * On NeoForge this was {@code net.neoforged.neoforge.common.util.ValueIOSerializable}.
 * The method signatures are identical — only the package changed.
 */
public interface SerializableIO {

    /**
     * Serialize this object's state to the given output.
     */
    void serialize(@NotNull ValueOutput output);

    /**
     * Deserialize this object's state from the given input.
     */
    void deserialize(@NotNull ValueInput input);
}
