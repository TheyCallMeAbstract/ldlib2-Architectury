package com.lowdragmc.lowdraglib2.fabric.client;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.LDLib2Registries;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.client.LDLib2ClientRegistries;
import com.lowdragmc.lowdraglib2.client.LDLibClientConfig;
import com.lowdragmc.lowdraglib2.client.font.LDFontStatsOverlay;
import com.lowdragmc.lowdraglib2.client.shader.LDLibRenderPipelines;
import com.lowdragmc.lowdraglib2.client.window.OsWindowManager;
import com.lowdragmc.lowdraglib2.gui.factory.LDMenuTypes;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.networking.both.PacketModularUISync;
import com.lowdragmc.lowdraglib2.networking.both.PacketRPCBlockEntity;
import com.lowdragmc.lowdraglib2.networking.both.PacketRPCPacket;
import com.lowdragmc.lowdraglib2.networking.both.PacketUIRPCEvent;
import com.lowdragmc.lowdraglib2.networking.both.PacketUIRPCEventReturn;
import com.lowdragmc.lowdraglib2.networking.s2c.SPacketAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib2.uitest.UITestBootstrap;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.networking.NetworkManager;
import dev.architectury.registry.client.gui.MenuScreenRegistry;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import net.minecraft.network.chat.Component;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class LDLib2FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        LDLib2.LOGGER.info("[uitest] onInitializeClient: isDevEnv={} screen_tests={} ui_scenarios={} menu_tests={}",
                Platform.isDevEnv(),
                LDLib2ClientRegistries.SCREEN_TESTS == null ? "null" : "present",
                LDLib2ClientRegistries.UI_SCENARIOS == null ? "null" : "present",
                LDLib2Registries.MENU_TESTS == null ? "null" : "present");

        // Register custom render pipelines into the vanilla PIPELINES_BY_LOCATION map.
        // On NeoForge this is done via RegisterRenderPipelinesEvent; on Fabric we call
        // the vanilla RenderPipelines.register() directly (see LDLibRenderPipelines.register()).
        LDLibRenderPipelines.register();

        // Client-only registries the annotation scan skips (RegistrationEnvironment.MANUAL):
        // ldlib2:gui_texture "empty"/"missing" and ldlib2:renderer "empty".
        // NeoForge parity: ClientProxy.init() calls this before any client wiring.
        LDLib2ClientRegistries.init();

        // Register screen factories for our menu types
        MenuScreenRegistry.registerScreenFactory(LDMenuTypes.PLAYER_UI.get(), ModularUIContainerScreen::new);
        MenuScreenRegistry.registerScreenFactory(LDMenuTypes.HELD_ITEM_UI.get(), ModularUIContainerScreen::new);
        MenuScreenRegistry.registerScreenFactory(LDMenuTypes.BLOCK_UI.get(), ModularUIContainerScreen::new);

        // Register OS window lifecycle and tick hooks
        OsWindowManager.init();

        // Client-parity wiring (reload listeners, font tick/render hooks, client commands) that NeoForge
        // sets up through its event bus.
        FabricClientEventListener.init();

        // In-client UI test harness: pump scenarios from the per-frame hook. Dev-env only; a production
        // client never subscribes and so never class-loads the runner.
        if (Platform.isDevEnv()) {
            UITestBootstrap.register();
        }

        // Register S2C receivers.
        //
        // Note: Architectury's registerReceiver(Side.S2C, ...) also registers the packet's payload
        // type, so these must NOT also be registered via registerS2CPayloadType on a client —
        // that would be a duplicate registration. LDLib2Fabric registers them (explicitly) only on
        // a dedicated server, which has no client entrypoint.
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                SPacketAutoSyncBlockEntity.TYPE, SPacketAutoSyncBlockEntity.CODEC,
                (payload, context) -> context.queue(() -> {
                    SPacketAutoSyncBlockEntity.handleOnClient(payload);
                }));

        // Register S2C receivers for bidirectional UI sync packets
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                PacketModularUISync.TYPE, PacketModularUISync.CODEC,
                (payload, context) -> context.queue(() -> {
                    PacketModularUISync.handleOnClient(payload, Minecraft.getInstance().player);
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                PacketUIRPCEvent.TYPE, PacketUIRPCEvent.CODEC,
                (payload, context) -> context.queue(() -> {
                    PacketUIRPCEvent.handleClient(payload, Minecraft.getInstance().player);
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                PacketUIRPCEventReturn.TYPE, PacketUIRPCEventReturn.CODEC,
                (payload, context) -> context.queue(() -> {
                    PacketUIRPCEventReturn.handle(payload, Minecraft.getInstance().player);
                }));

        // Register S2C receivers for RPC packets
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                PacketRPCBlockEntity.TYPE, PacketRPCBlockEntity.CODEC,
                (payload, context) -> context.queue(() -> {
                    PacketRPCBlockEntity.handleOnClient(payload, Minecraft.getInstance().player);
                }));
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                PacketRPCPacket.TYPE, PacketRPCPacket.CODEC,
                (payload, context) -> context.queue(() -> {
                    PacketRPCPacket.handleOnClient(payload, Minecraft.getInstance().player);
                }));

        // Register the font stats overlay as a HUD layer.
        // On NeoForge this is done via RegisterGuiLayersEvent.registerAboveAll() in
        // ClientModBusEventListener.registerFontStatsOverlay(), gated on Platform.isDevEnv().
        // On Fabric, Architectury's ClientGuiEvent.RENDER_HUD provides the equivalent hook
        // with the same renderHud(GuiGraphicsExtractor, DeltaTracker) signature.
        if (Platform.isDevEnv()) {
            ClientGuiEvent.RENDER_HUD.register(LDFontStatsOverlay.INSTANCE::render);
        }

        // Load saved config values on client start
        LDLibClientConfig.load();
    }

    /**
     * Builds a YACL config screen for LDLib2's client settings.
     *
     * <p>Mirrors NeoForge's {@code modContainer.registerExtensionPoint(IConfigScreenFactory.class,
     * ConfigurationScreen::new)} which makes the Config button appear in the mod list.
     * On Fabric without Mod Menu, this method is available for programmatic access
     * (e.g., via a keybind or command).
     */
    public static Screen buildConfigScreen(Screen parent) {
        var builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("ldlib2.configuration.title"))
                .save(LDLibClientConfig::save);

        // Font category — mirrors the NeoForge ModConfigSpec "font" push() group
        var fontGroup = OptionGroup.createBuilder()
                .name(Component.translatable("ldlib2.configuration.font"))
                .build();

        // fontRenderMode
        var renderModeOption = Option.<LDLibClientConfig.FontRenderMode>createBuilder()
                .name(Component.translatable("ldlib2.configuration.font.fontRenderMode"))
                .description(OptionDescription.of(
                        Component.translatable("ldlib2.configuration.font.fontRenderMode.tooltip")))
                .controller(base -> EnumControllerBuilder.create(base)
                        .enumClass(LDLibClientConfig.FontRenderMode.class))
                .binding(
                        LDLibClientConfig.FontRenderMode.AUTO,
                        LDLibClientConfig::fontRenderMode,
                        LDLibClientConfig::setFontRenderMode)
                .build();

        // fontAtlasSize
        var atlasSizeOption = Option.<Integer>createBuilder()
                .name(Component.translatable("ldlib2.configuration.font.fontAtlasSize"))
                .description(OptionDescription.of(
                        Component.translatable("ldlib2.configuration.font.fontAtlasSize.tooltip")))
                .controller(base -> IntegerSliderControllerBuilder.create(base)
                        .range(256, 4096)
                        .step(1))
                .binding(
                        1024,
                        LDLibClientConfig::atlasSize,
                        LDLibClientConfig::setAtlasSize)
                .build();

        // sdfEmSize
        var emSizeOption = Option.<Integer>createBuilder()
                .name(Component.translatable("ldlib2.configuration.font.sdfEmSize"))
                .description(OptionDescription.of(
                        Component.translatable("ldlib2.configuration.font.sdfEmSize.tooltip")))
                .controller(base -> IntegerSliderControllerBuilder.create(base)
                        .range(16, 128)
                        .step(1))
                .binding(
                        48,
                        LDLibClientConfig::emSize,
                        LDLibClientConfig::setEmSize)
                .build();

        // sdfSharpness
        var sharpnessOption = Option.<Float>createBuilder()
                .name(Component.translatable("ldlib2.configuration.font.sdfSharpness"))
                .description(OptionDescription.of(
                        Component.translatable("ldlib2.configuration.font.sdfSharpness.tooltip")))
                .controller(base -> FloatSliderControllerBuilder.create(base)
                        .range(0.1f, 4.0f)
                        .step(0.05f))
                .binding(
                        1.0f,
                        LDLibClientConfig::sharpness,
                        v -> LDLibClientConfig.setSharpness(v))
                .build();

        // sdfWeight
        var weightOption = Option.<Float>createBuilder()
                .name(Component.translatable("ldlib2.configuration.font.sdfWeight"))
                .description(OptionDescription.of(
                        Component.translatable("ldlib2.configuration.font.sdfWeight.tooltip")))
                .controller(base -> FloatSliderControllerBuilder.create(base)
                        .range(-0.5f, 0.5f)
                        .step(0.05f))
                .binding(
                        0.0f,
                        LDLibClientConfig::weight,
                        v -> LDLibClientConfig.setWeight(v))
                .build();

        // fontRasterMaxSize
        var rasterMaxSizeOption = Option.<Integer>createBuilder()
                .name(Component.translatable("ldlib2.configuration.font.fontRasterMaxSize"))
                .description(OptionDescription.of(
                        Component.translatable("ldlib2.configuration.font.fontRasterMaxSize.tooltip")))
                .controller(base -> IntegerSliderControllerBuilder.create(base)
                        .range(16, 512)
                        .step(1))
                .binding(
                        256,
                        LDLibClientConfig::rasterMaxSize,
                        LDLibClientConfig::setRasterMaxSize)
                .build();

        // fontRasterEvictSeconds
        var rasterEvictOption = Option.<Integer>createBuilder()
                .name(Component.translatable("ldlib2.configuration.font.fontRasterEvictSeconds"))
                .description(OptionDescription.of(
                        Component.translatable("ldlib2.configuration.font.fontRasterEvictSeconds.tooltip")))
                .controller(base -> IntegerSliderControllerBuilder.create(base)
                        .range(1, 600)
                        .step(1))
                .binding(
                        30,
                        LDLibClientConfig::rasterEvictSeconds,
                        LDLibClientConfig::setRasterEvictSeconds)
                .build();

        // textLayoutCache
        var textLayoutCacheOption = Option.<Boolean>createBuilder()
                .name(Component.translatable("ldlib2.configuration.font.textLayoutCache"))
                .description(OptionDescription.of(
                        Component.translatable("ldlib2.configuration.font.textLayoutCache.tooltip")))
                .controller(base -> BooleanControllerBuilder.create(base))
                .binding(
                        true,
                        LDLibClientConfig::isTextLayoutCache,
                        LDLibClientConfig::setTextLayoutCache)
                .build();

        var fontCategory = ConfigCategory.createBuilder()
                .name(Component.translatable("ldlib2.configuration.font"))
                .tooltip(Component.translatable("ldlib2.configuration.font.tooltip"))
                .group(fontGroup)
                .option(renderModeOption)
                .option(atlasSizeOption)
                .option(emSizeOption)
                .option(sharpnessOption)
                .option(weightOption)
                .option(rasterMaxSizeOption)
                .option(rasterEvictOption)
                .option(textLayoutCacheOption)
                .build();

        builder.category(fontCategory);

        return builder.build().generateScreen(parent);
    }
}
