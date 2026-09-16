package com.lowdragmc.lowdraglib2.core.mixins.client;

import com.lowdragmc.lowdraglib2.client.ClientGuiLayers;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reproduces the two {@code ClientHooks} calls NeoForge patches into {@link Minecraft}: clearing the
 * GUI layers whenever a fresh screen is opened, and resizing them with the window.
 *
 * <p>{@link ClientGuiLayers#push} / {@link ClientGuiLayers#pop} assign {@link Minecraft#screen}
 * directly, so they never route through {@code setScreen} and cannot recurse into these hooks.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftGuiLayerMixin {

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void ldlib2$clearGuiLayersOnSetScreen(CallbackInfo ci) {
        ClientGuiLayers.clear((Minecraft) (Object) this);
    }

    @Inject(method = "resizeGui", at = @At("TAIL"))
    private void ldlib2$resizeGuiLayers(CallbackInfo ci) {
        var minecraft = (Minecraft) (Object) this;
        if (minecraft.screen != null) {
            ClientGuiLayers.resize(minecraft.getWindow().getGuiScaledWidth(),
                    minecraft.getWindow().getGuiScaledHeight());
        }
    }
}
