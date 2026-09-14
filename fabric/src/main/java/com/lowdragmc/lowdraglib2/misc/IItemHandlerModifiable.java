package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.world.item.ItemStack;

/**
 * Fabric-side mirror of NeoForge's IItemHandlerModifiable.
 * Extends IItemHandler with the ability to directly set a slot's contents.
 */
public interface IItemHandlerModifiable extends IItemHandler {

    /**
     * Sets the ItemStack for the given slot, bypassing insert/extract logic.
     */
    void setStackInSlot(int index, ItemStack stack);
}
