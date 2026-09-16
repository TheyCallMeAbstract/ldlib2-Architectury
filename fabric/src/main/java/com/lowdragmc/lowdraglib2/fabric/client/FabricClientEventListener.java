package com.lowdragmc.lowdraglib2.fabric.client;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.client.ClientCommands;
import com.lowdragmc.lowdraglib2.client.font.LDFontManager;
import com.lowdragmc.lowdraglib2.client.font.LDFontStatsOverlay;
import com.lowdragmc.lowdraglib2.editor.resource.EditorResourceEvent;
import com.lowdragmc.lowdraglib2.editor.resource.PackResourceManager;
import com.lowdragmc.lowdraglib2.editor.resource.ResourceInstance;
import com.lowdragmc.lowdraglib2.editor.resource.TexturesResource;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.MCSprites;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.OreSprites;
import com.lowdragmc.lowdraglib2.gui.ui.styletemplate.Sprites;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.gui.ui.utils.CursorOverlay;
import com.lowdragmc.lowdraglib2.gui.ui.utils.ModularUIClientElementComponent;
import com.lowdragmc.lowdraglib2.gui.ui.utils.ModularUITooltipComponent;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.ReloadListenerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

/**
 * Fabric counterpart of NeoForge's {@code ClientEventListener} + {@code ClientModBusEventListener}.
 * <p>
 * The container-screen attach itself is deliberately <em>not</em> done here: Fabric has no deterministic
 * "screen init pre" hook equivalent to NeoForge's {@code ScreenEvent.Init.Pre}, so
 * {@link com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen} lays out and attaches its own widget
 * in {@code init()}. This class carries the remaining client wiring that has an Architectury/Fabric equivalent.
 */
public final class FabricClientEventListener {
    private static boolean texturesInitialised;

    private FabricClientEventListener() {
    }

    public static void init() {
        // Client resource reload listeners. NeoForge registers these from
        // ClientModBusEventListener#onAddClientReloadListenerEvent; Fabric registers none by default, which
        // leaves the font manager (and friends) permanently empty.
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES,
                PackResourceManager.INSTANCE, PackResourceManager.RESOURCE_ID);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES,
                StylesheetManager.INSTANCE, StylesheetManager.RESOURCE_ID);
        ReloadListenerRegistry.register(PackType.CLIENT_RESOURCES,
                LDFontManager.INSTANCE, LDFontManager.RESOURCE_ID);

        // Per-tick font housekeeping, mirroring ClientEventListener#onClientTick.
        ClientTickEvent.CLIENT_POST.register(minecraft -> {
            LDFontManager.INSTANCE.refreshVanillaFontOptions();
            LDFontManager.INSTANCE.evictStaleRasterSizes();
        });

        // Drawn above the open screen, mirroring ClientEventListener#onScreenRendered.
        ClientGuiEvent.RENDER_POST.register((screen, graphics, mouseX, mouseY, partialTick) -> {
            if (Platform.isDevEnv()) {
                LDFontStatsOverlay.INSTANCE.render(graphics, Minecraft.getInstance().getDeltaTracker());
            }
            // Only draws while something is driving the cursor from inside the process; see the class doc.
            CursorOverlay.render(graphics, partialTick);
        });

        // Client commands, mirroring ClientEventListener#onRegisterClientCommands.
        ClientCommandRegistrationEvent.EVENT.register((dispatcher, context) ->
                ClientCommands.createClientCommands().forEach(dispatcher::register));

        // The frame-hook applier for /ldlib2_screen_test. Must be subscribed before the UI test harness
        // (UITestBootstrap.register, called later in LDLib2FabricClient#onInitializeClient) so a screen
        // armed by a synthetic chat command lands on the following frame, never the same one. See
        // ClientCommands#installScreenApplier.
        ClientCommands.installScreenApplier();

        // NeoForge parity: ClientEventListener#onLoadBuiltinEditorResource. Without this the
        // ui-gdp / ui-mc / ui-ore built-in providers are never created and every
        // built-in(ui-*:NAME) stylesheet value resolves to null (transparent panels/borders).
        EditorResourceEvent.registerListener(FabricClientEventListener::onLoadBuiltinEditorResource);

        // NeoForge parity: ClientModBusEventListener#onRegisterClientTooltipComponentFactoriesEvent
        // registers ModularUITooltipComponent -> ModularUIClientElementComponent. Fabric's equivalent
        // is a factory callback; returning null falls through to vanilla (the fabric-rendering-v1
        // mixin only overrides the return value when the callback result is non-null).
        ClientTooltipComponentCallback.EVENT.register(component ->
                component instanceof ModularUITooltipComponent modular
                        ? new ModularUIClientElementComponent(modular)
                        : null);
    }

    /**
     * NeoForge parity for {@code ClientEventListener#onLoadBuiltinEditorResource}: attaches the
     * {@code ui-gdp} / {@code ui-mc} / {@code ui-ore} built-in providers to the {@link TexturesResource}
     * instance when it is built. Client-only because {@link Sprites} and friends pull in client classes.
     */
    @SuppressWarnings("unchecked")
    public static void onLoadBuiltinEditorResource(EditorResourceEvent event) {
        if (event.resourceInstance.resource != TexturesResource.INSTANCE) return;
        if (texturesInitialised) return; // a resource instance is built once; never re-wrap
        texturesInitialised = true;
        var instance = (ResourceInstance<IGuiTexture>) event.resourceInstance;
        Sprites.init(instance);
        MCSprites.init(instance);
        OreSprites.init(instance);
    }
}
