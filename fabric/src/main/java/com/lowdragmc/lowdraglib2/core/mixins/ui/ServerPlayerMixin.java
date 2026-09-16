package com.lowdragmc.lowdraglib2.core.mixins.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.lowdragmc.lowdraglib2.event.LDLib2Events;
import com.lowdragmc.lowdraglib2.gui.event.ContainerMenuEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Posts {@link ContainerMenuEvent.Create} when the server builds a menu for a player through
 * {@link ServerPlayer#openMenu(net.minecraft.world.MenuProvider)}.
 *
 * <p>Fabric's {@code ServerPlayer} only exposes the one-argument overload
 * {@code openMenu(MenuProvider)}; it internally invokes
 * {@code MenuProvider.createMenu(int, Inventory, Player)}, so retargeting that call with
 * MixinExtras' {@code @ModifyExpressionValue} covers the documented menu-open path.
 *
 * <p>NeoForge additionally hooks {@code lambda$openMenu$0} to wire
 * {@code IModularUIHolder#writeInitialData}; that synthetic lambda belongs to the two-argument
 * {@code openMenu} overload, which does not exist on Fabric, so it is intentionally absent here.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    @ModifyExpressionValue(
            method = "openMenu(Lnet/minecraft/world/MenuProvider;)Ljava/util/OptionalInt;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"
            )
    )
    private AbstractContainerMenu ldlib2$openMenu(AbstractContainerMenu original) {
        if (original != null) {
            LDLib2Events.game().post(new ContainerMenuEvent.Create((ServerPlayer)(Object)this, original));
        }
        return original;
    }
}
