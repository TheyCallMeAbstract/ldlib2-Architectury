package com.lowdragmc.lowdraglib2.core.mixins.client;

import com.lowdragmc.lowdraglib2.client.ClientGuiLayers;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Reproduces NeoForge's {@code GameRenderer} patch that renders the GUI layers beneath the current
 * screen.
 *
 * <p>Vanilla renders only {@link net.minecraft.client.Minecraft#screen}. NeoForge replaces that call
 * with {@code ClientHooks.drawScreen}, which walks the layer stack first — drawing each with the
 * pointer parked at {@link Integer#MAX_VALUE} so no background control believes it is hovered — then
 * draws the current screen. Without this the inspected UI vanishes the moment the debugger opens.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererGuiLayerMixin {

    @Redirect(
            method = "extractGui(Lnet/minecraft/client/DeltaTracker;ZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"
            )
    )
    private void ldlib2$drawScreensWithLayers(Screen screen, GuiGraphicsExtractor graphics,
                                              int mouseX, int mouseY, float partialTick) {
        ClientGuiLayers.drawScreen(screen, graphics, mouseX, mouseY, partialTick);
    }
}
