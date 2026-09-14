package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.world.item.ItemStack;

/**
 * Fabric-side utility class mirroring NeoForge's ItemHandlerHelper.
 */
public class ItemHandlerHelper {

    /**
     * Returns true if the two ItemStacks can be stacked together.
     */
    public static boolean canItemStacksStack(ItemStack stack1, ItemStack stack2) {
        if (stack2.isEmpty() || stack1.isEmpty()) return false;
        if (!stack1.is(stack2.getItem())) return false;
        if (!ItemStack.isSameItemSameComponents(stack1, stack2)) return false;
        return true;
    }

    /**
     * Creates a copy of the given ItemStack with the specified count.
     */
    public static ItemStack copyStackWithSize(ItemStack itemStack, int size) {
        if (itemStack.isEmpty()) return ItemStack.EMPTY;
        ItemStack stack = itemStack.copy();
        stack.setCount(size);
        return stack;
    }
}
