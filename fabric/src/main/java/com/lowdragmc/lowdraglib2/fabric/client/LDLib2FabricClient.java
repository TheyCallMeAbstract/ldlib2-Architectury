package com.lowdragmc.lowdraglib2.fabric.client;

import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.client.LDLibClientConfig;
import com.lowdragmc.lowdraglib2.client.font.LDFontStatsOverlay;
import com.lowdragmc.lowdraglib2.client.shader.LDLibRenderPipelines;
import com.lowdragmc.lowdraglib2.networking.s2c.SPacketAutoSyncBlockEntity;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.networking.NetworkManager;
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
import net.minecraft.client.gui.screens.Screen;

public final class LDLib2FabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register custom render pipelines into the vanilla PIPELINES_BY_LOCATION map.
        // On NeoForge this is done via RegisterRenderPipelinesEvent; on Fabric we call
        // the vanilla RenderPipelines.register() directly (see LDLibRenderPipelines.register()).
        LDLibRenderPipelines.register();

        // Register S2C receiver for block entity sync packets
        NetworkManager.registerReceiver(NetworkManager.Side.S2C,
                SPacketAutoSyncBlockEntity.TYPE, SPacketAutoSyncBlockEntity.CODEC,
                (payload, context) -> context.queue(() -> {
                    SPacketAutoSyncBlockEntity.handleOnClient(payload);
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
                        .enumClass(LDLibClientConfig.FontRenderMode.class)
                        .build())
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
                        .step(1)
                        .build())
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
                        .step(1)
                        .build())
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
                        .step(0.05f)
                        .build())
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
                        .step(0.05f)
                        .build())
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
                        .step(1)
                        .build())
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
                        .step(1)
                        .build())
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
                .controller(base -> BooleanControllerBuilder.create(base)
                        .build())
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
                .option(rasterEvictSecondsOption)
                .option(textLayoutCacheOption)
                .build();

        builder.category(fontCategory);

        return builder.build().generateScreen(parent);
    }
}
