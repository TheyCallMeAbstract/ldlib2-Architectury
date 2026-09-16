package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInputContextHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for TagValueInput's private context and input fields.
 * Kept as a pure accessor interface (only {@code @Accessor} members) so Mixin
 * classifies it as an Accessor Mixin, which is allowed to target a class.
 * Logic that used to live here has moved to {@link ValueInputHelper}.
 */
@Mixin(TagValueInput.class)
public interface TagValueInputAccessor {
    @Accessor("context")
    ValueInputContextHelper ldlib2$getContext();

    @Accessor("input")
    CompoundTag ldlib2$getInput();
}
