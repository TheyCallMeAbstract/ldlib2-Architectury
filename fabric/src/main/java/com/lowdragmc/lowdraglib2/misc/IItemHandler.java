package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

/**
 * Fabric-side mirror of NeoForge's IItemHandler.
 * Provides a slot-based item inventory abstraction.
 */
public interface IItemHandler {

    /**
     * Returns the number of inventory slots available.
     */
    int getSlots();

    /**
     * Returns the ItemStack in the given slot.
     */
    @Nonnull
    ItemStack getStackInSlot(int slot);

    /**
     * Inserts an ItemStack into the given slot and returns the remainder.
     * @param slot Slot index
     * @param stack ItemStack to insert
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted
     */
    @NotNull
    ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate);

    /**
     * Extracts an ItemStack from the given slot.
     * @param slot Slot index
     * @param amount Maximum number of items to extract
     * @param simulate If true, the extraction is only simulated
     * @return The extracted ItemStack
     */
    @NotNull
    ItemStack extractItem(int slot, int amount, boolean simulate);

    /**
     * Returns the maximum stack size for the given slot.
     */
    int getSlotLimit(int slot);

    /**
     * Returns true if the given ItemStack can be placed in the given slot.
     */
    boolean isItemValid(int slot, @Nonnull ItemStack stack);
}
