package com.lowdragmc.lowdraglib2.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import java.util.Objects;
import java.util.Stack;

/**
 * Fabric's replacement for NeoForge's {@code ClientHooks} GUI layer stack.
 *
 * <p>NeoForge keeps the screens <em>beneath</em> the current one in a static stack so that opening a
 * screen with {@code pushGuiLayer} does not destroy what is under it. The debugger relies on exactly
 * that: it is a layer drawn over the UI it inspects, and that UI has to stay alive, tick and render
 * underneath it. Fabric has no such concept, so this mirrors it.
 *
 * <p>Semantics are copied from {@code net.neoforged.neoforge.client.ClientHooks}: the exposed
 * {@link #push}, {@link #pop}, {@link #clear} and {@link #resize} behave identically, and
 * {@link #drawScreen} reproduces the layer-then-current draw order. The only difference is that the
 * NeoForge hooks also post {@code ScreenEvent}s, which Fabric does not have.
 */
public final class ClientGuiLayers {

    /**
     * The <em>extra</em> GUI layers. The current top layer stays in {@link Minecraft#screen}, and the
     * rest serve as a background for it.
     */
    private static final Stack<Screen> GUI_LAYERS = new Stack<>();

    private ClientGuiLayers() {
    }

    public static void resize(int width, int height) {
        GUI_LAYERS.forEach(screen -> screen.resize(width, height));
    }

    public static void clear(Minecraft minecraft) {
        while (!GUI_LAYERS.isEmpty()) {
            popInternal(minecraft);
        }
    }

    private static void popInternal(Minecraft minecraft) {
        if (minecraft.screen != null) {
            minecraft.screen.removed();
        }
        minecraft.screen = GUI_LAYERS.pop();
    }

    public static void push(Minecraft minecraft, Screen screen) {
        if (minecraft.screen != null) {
            GUI_LAYERS.push(minecraft.screen);
        }
        minecraft.screen = Objects.requireNonNull(screen);
        screen.init(minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
        minecraft.getNarrator().saySystemNow(screen.getNarrationMessage());
    }

    public static void pop(Minecraft minecraft) {
        if (GUI_LAYERS.isEmpty()) {
            minecraft.setScreen(null);
            return;
        }

        popInternal(minecraft);
        if (minecraft.screen != null) {
            minecraft.getNarrator().saySystemNow(minecraft.screen.getNarrationMessage());
        }
    }

    public static void drawScreen(Screen screen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GUI_LAYERS.forEach(layer -> {
            // Prevent the background layers from thinking the mouse is over their controls and showing them as highlighted.
            layer.extractRenderStateWithTooltipAndSubtitles(graphics, Integer.MAX_VALUE, Integer.MAX_VALUE, partialTick);
            graphics.nextStratum();
        });
        screen.extractRenderStateWithTooltipAndSubtitles(graphics, mouseX, mouseY, partialTick);
    }
}
