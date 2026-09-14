package com.lowdragmc.lowdraglib2.gui.hud;

import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUIClientAccess;
import com.lowdragmc.lowdraglib2.math.Size;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.jetbrains.annotations.Nullable;

/**
 * Fabric-compatible interface for HUD overlay layers that render through ModularUI.
 */
public interface ModularHudLayer {

    @Nullable ModularUI getModularUI();

    /**
     * Retrieves the current screen size of the game window.
     */
    default Size getScreenSize() {
        return Size.of(Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                Minecraft.getInstance().getWindow().getGuiScaledHeight());
    }

    /**
     * Validates the specified {@code ModularUI} by ensuring its dimensions match the current
     * screen size. If the dimensions do not match, it reinitializes the {@code ModularUI} to
     * adapt to the screen size.
     *
     * <p><b>Why this exists on Fabric:</b> The NeoForge version has this in its {@code GuiLayer}
     * override. On Fabric there is no {@code GuiLayer} interface, so we replicate the same
     * resize-check logic here. Without this, HUD overlays would render at stale dimensions
     * after a window resize.
     *
     * @param mui the {@link ModularUI} instance to be validated and possibly reinitialized.
     * @return {@code true} always (kept for API compatibility with NeoForge callers).
     */
    default boolean validModularUI(ModularUI mui) {
        // Always update tick while rendering
        mui.setTickWhileRending(true);
        // Check screen size — reinit if the window was resized since last init
        var size = getScreenSize();
        if (mui.getScreenWidth() != size.getWidth() || mui.getScreenHeight() != size.getHeight()) {
            mui.init(size.width, size.height);
        }
        return true;
    }

    /**
     * Renders the HUD layer. Validates screen dimensions first, then delegates to the
     * ModularUI widget's rendering pipeline.
     *
     * <p><b>Why this matches NeoForge:</b> The NeoForge version calls
     * {@code ModularUIClientAccess.getWidget(mui).extractRenderState(graphics, Integer.MAX_VALUE,
     * Integer.MAX_VALUE, partialTicks)}. We replicate the exact same call here for 1:1 parity.
     * Both platforms use the same {@link ModularUIClientAccess} and {@link
     * com.lowdragmc.lowdraglib2.gui.ui.ModularUIWidget#extractRenderState} — the only difference
     * is the registration mechanism (NeoForge {@code GuiLayer} vs Architectury
     * {@code ClientGuiEvent.RenderHud}).
     */
    default void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        var mui = getModularUI();
        if (mui == null) return;
        if (validModularUI(mui)) {
            var partialTicks = deltaTracker.getGameTimeDeltaPartialTick(false);
            ModularUIClientAccess.getWidget(mui).extractRenderState(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTicks);
        }
    }
}
