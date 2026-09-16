package com.lowdragmc.lowdraglib2.core.mixins.ui;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.event.LDLib2Events;
import com.lowdragmc.lowdraglib2.gui.event.ContainerMenuEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Posts {@link ContainerMenuEvent.Create} when the player's own {@link InventoryMenu} is
 * constructed. The construction happens on both the client and the server, so the post is
 * routed through {@link Platform#executeOnClient(Runnable)} /
 * {@link Platform#executeOnServer(Runnable)} to run on the correct thread for the side.
 */
@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void ldlib2$onInit(Inventory playerInventory, boolean active, Player owner, CallbackInfo ci) {
        if (owner == null) return;
        var menu = (InventoryMenu)(Object)this;
        if (owner.level().isClientSide()) {
            Platform.executeOnClient(() ->
                    LDLib2Events.game().post(new ContainerMenuEvent.Create(owner, menu)));
        } else {
            Platform.executeOnServer(() ->
                    LDLib2Events.game().post(new ContainerMenuEvent.Create(owner, menu)));
        }
    }
}
