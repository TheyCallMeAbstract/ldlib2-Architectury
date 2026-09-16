package com.lowdragmc.lowdraglib2.core.mixins.ui;

import com.lowdragmc.lowdraglib2.event.LDLib2Events;
import com.lowdragmc.lowdraglib2.gui.event.ContainerMenuEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Posts {@link ContainerMenuEvent.Create} when a {@link MenuType} constructs a menu.
 *
 * <p>Fabric's {@code MenuType} only exposes {@code create(int, Inventory)} — the client-side
 * {@code MenuScreens} path funnels through it and Architectury's extended menu providers build
 * a plain {@code MenuType} as well — so the single inject below covers every construction path
 * on this loader. The NeoForge-only {@code RegistryFriendlyByteBuf} overload does not exist here.
 */
@Mixin(MenuType.class)
public abstract class MenuTypeMixin<T extends AbstractContainerMenu> {

    @Inject(method = "create(ILnet/minecraft/world/entity/player/Inventory;)Lnet/minecraft/world/inventory/AbstractContainerMenu;",
            at = @At(value = "RETURN"))
    private void ldlib2$create1(int containerId, Inventory playerInventory, CallbackInfoReturnable<T> cir) {
        var menu = cir.getReturnValue();
        if (menu != null) {
            LDLib2Events.game().post(new ContainerMenuEvent.Create(playerInventory.player, menu));
        }
    }
}
