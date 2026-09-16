package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.TagValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for TagValueOutput's private ops field.
 *
 * <p>The declared return type must match the field type exactly ({@code DynamicOps<Tag>}, not
 * {@code Object}) or Mixin finds no candidate and fails accessor application.
 */
@Mixin(TagValueOutput.class)
public interface TagValueOutputAccessor {
    @Accessor("ops")
    DynamicOps<Tag> ldlib2$getOps();
}
