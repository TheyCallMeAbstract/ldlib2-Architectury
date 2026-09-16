package com.lowdragmc.lowdraglib2.core.mixins.accessor;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

/**
 * Accessor for AbstractContainerScreen's private/protected fields used by ItemSlot
 * to inspect quick-crafting state and the dragged/splitting stack.
 */
@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccessor {
    @Accessor("clickedSlot")
    Slot ldlib2$getClickedSlot();

    @Accessor("draggingItem")
    ItemStack ldlib2$getDraggingItem();

    @Accessor("isSplittingStack")
    boolean ldlib2$isSplittingStack();

    @Accessor("isQuickCrafting")
    boolean ldlib2$isQuickCrafting();

    @Accessor("quickCraftSlots")
    Set<Slot> ldlib2$getQuickCraftSlots();

    @Accessor("quickCraftingType")
    int ldlib2$getQuickCraftingType();

    @Invoker("recalculateQuickCraftRemaining")
    void ldlib2$recalculateQuickCraftRemaining();

    @Accessor("imageWidth")
    int ldlib2$getImageWidth();

    @Accessor("imageWidth")
    @Mutable
    void ldlib2$setImageWidth(int imageWidth);

    @Accessor("imageHeight")
    int ldlib2$getImageHeight();

    @Accessor("imageHeight")
    @Mutable
    void ldlib2$setImageHeight(int imageHeight);
}
