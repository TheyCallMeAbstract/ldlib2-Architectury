package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Accessor for Slot's private x/y fields, bypassing JPMS module boundary restrictions.
 * Used by ItemSlot.updateSlotPosition() to set slot position without direct final field access.
 */
@Mixin(Slot.class)
public interface SlotAccessor {
    @Accessor("x")
    int ldlib2$getX();

    @Accessor("x")
    @Mutable
    void ldlib2$setX(int x);

    @Accessor("y")
    int ldlib2$getY();

    @Accessor("y")
    @Mutable
    void ldlib2$setY(int y);
}