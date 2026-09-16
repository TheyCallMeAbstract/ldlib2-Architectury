package com.lowdragmc.lowdraglib2.client;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.Platform;
import com.lowdragmc.lowdraglib2.client.font.LDFontStatsOverlay;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.uitest.UITestRunner;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent;
import dev.architectury.event.events.client.ClientCommandRegistrationEvent.ClientCommandSourceStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Fabric port of NeoForge's {@code ClientCommands}.
 * <p>
 * Architectury's client command source is {@link ClientCommandSourceStack} rather than vanilla's
 * {@code CommandSourceStack}, so literals are built through {@link ClientCommandRegistrationEvent}.
 */
public class ClientCommands {

    public static LiteralArgumentBuilder<ClientCommandSourceStack> createLiteral(String command) {
        return ClientCommandRegistrationEvent.literal(command);
    }

    public static List<LiteralArgumentBuilder<ClientCommandSourceStack>> createClientCommands() {
        var commands = new ArrayList<LiteralArgumentBuilder<ClientCommandSourceStack>>();
        LDLib2.LOGGER.info("[uitest] createClientCommands: devEnv={} screen_test={} ui_scenario={}",
                Platform.isDevEnv(),
                LDLib2ClientRegistries.SCREEN_TESTS == null ? "null" : LDLib2ClientRegistries.SCREEN_TESTS.values().size(),
                LDLib2ClientRegistries.UI_SCENARIOS == null ? "null" : LDLib2ClientRegistries.UI_SCENARIOS.values().size());
        if (Platform.isDevEnv()) {
            commands.add(createFontCommands());
        }
        if (LDLib2ClientRegistries.SCREEN_TESTS != null && !LDLib2ClientRegistries.SCREEN_TESTS.values().isEmpty()) {
            commands.add(createScreenTestCommands());
        } else {
            LDLib2.LOGGER.warn("[uitest] ldlib2_screen_test will have no subcommands: screen_test registry is {} (requires a dev environment)",
                    LDLib2ClientRegistries.SCREEN_TESTS == null ? "null" : "empty");
        }
        if (LDLib2ClientRegistries.UI_SCENARIOS != null && !LDLib2ClientRegistries.UI_SCENARIOS.values().isEmpty()) {
            commands.add(createAutoTestCommands());
        } else {
            LDLib2.LOGGER.warn("[uitest] ldlib2_autotest will have no subcommands: ui_scenario registry is {} (requires a dev environment)",
                    LDLib2ClientRegistries.UI_SCENARIOS == null ? "null" : "empty");
        }
        return commands;
    }

    /**
     * Runs a UI test scenario inside the running game.
     *
     * <p>A command-line run pays for Gradle, mod loading and world creation before the first step,
     * and none of that changes between attempts. While iterating on a scenario, launch once with
     * {@code -PldTestKeepOpen} and re-run from here instead.
     */
    private static LiteralArgumentBuilder<ClientCommandSourceStack> createAutoTestCommands() {
        LDLib2.LOGGER.info("[uitest] ldlib2_autotest subcommands ({}) sorted: {}",
                LDLib2ClientRegistries.UI_SCENARIOS.values().size(),
                LDLib2ClientRegistries.UI_SCENARIOS.keys().stream().sorted().toList());
        return createLiteral("ldlib2_autotest")
                .then(createLiteral("list")
                        .executes(context -> {
                            var names = UITestRunner.registeredScenarioNames();
                            context.getSource().arch$sendSuccess(() -> Component.literal(
                                    names.size() + " scenario(s): " + String.join(", ", names)), false);
                            return names.size();
                        }))
                .then(createLiteral("run")
                        .then(ClientCommandRegistrationEvent.argument("selection", StringArgumentType.greedyString())
                                .suggests((context, builder) -> {
                                    builder.suggest("all");
                                    UITestRunner.registeredScenarioNames().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    var selection = StringArgumentType.getString(context, "selection");
                                    var error = UITestRunner.runInteractive(selection);
                                    if (error != null) {
                                        context.getSource().arch$sendFailure(Component.literal(error));
                                        return 0;
                                    }
                                    context.getSource().arch$sendSuccess(() -> Component.literal(
                                            "Running UI scenarios: " + selection
                                                    + " (results go to the log and report.json)"), false);
                                    return 1;
                                })));
    }

    /**
     * Development only helpers for eyeballing the text renderer. Not registered outside a dev environment:
     * the settings they poke live in the client config, which is where users are meant to change them.
     */
    private static LiteralArgumentBuilder<ClientCommandSourceStack> createFontCommands() {
        return createLiteral("ldlib2_font")
                .then(createLiteral("mode")
                        .executes(context -> {
                            var modes = LDLibClientConfig.FontRenderMode.values();
                            var next = modes[(LDLibClientConfig.fontRenderMode().ordinal() + 1) % modes.length];
                            LDLibClientConfig.setFontRenderMode(next);
                            // the renderers measure text slightly differently, so lay the screen out again
                            reinitCurrentScreen();
                            context.getSource().arch$sendSuccess(
                                    () -> Component.literal("LDLib text: " + next), false);
                            return 1;
                        }))
                .then(createLiteral("stats")
                        .executes(context -> {
                            LDFontStatsOverlay.toggle();
                            context.getSource().arch$sendSuccess(
                                    () -> Component.literal(LDFontStatsOverlay.describe()), false);
                            return 1;
                        }));
    }

    /**
     * Rebuilds the open screen so text is measured again with the renderer that is now active.
     */
    private static void reinitCurrentScreen() {
        var minecraft = Minecraft.getInstance();
        var screen = minecraft.screen;
        if (screen != null) {
            screen.resize(screen.width, screen.height);
        }
    }

    /**
     * A screen the command handler wants open, but must not open itself.
     *
     * <p>On Fabric client commands run <em>synchronously</em> inside {@code ChatScreen#keyPressed}
     * (the Fabric API {@code ClientPacketListener#sendCommand} mixin), and vanilla then closes the chat
     * with {@code Minecraft#setScreen(null)} — which would clobber a screen set from the callback.
     * Vanilla has no NeoForge-style guard on that teardown, so the change is parked here and applied
     * from the frame hook instead.
     *
     * <p>{@code armedFrame} is the value of {@link #frameCounter} when the command ran. The applier
     * only fires once the counter has advanced past it, so a command dispatched from <em>inside</em>
     * the frame hook (as the UI test harness does) still lands on a later frame rather than in the
     * same callback. {@code Minecraft#execute} cannot be used for this: during GLFW input polling the
     * render thread is not inside {@code BlockableEventLoop#doRunTask}, so {@code reentrantCount} is 0
     * and {@code execute} runs the task inline instead of queueing it.
     */
    private record PendingScreen(ModularUIScreen screen, String name, long armedFrame) {
    }

    /** Single-slot handoff from the command callback to the frame hook. Written/read on the render thread. */
    private static volatile @Nullable PendingScreen pendingScreen;
    private static long frameCounter;
    private static boolean applierInstalled;

    /**
     * Subscribes {@link #applyPendingScreen} to the per-frame hook fired after {@code GameRenderer.render}.
     *
     * <p>Call this before the UI test harness subscribes its frame pump (see
     * {@code LDLib2FabricClient#onInitializeClient} - {@code FabricClientEventListener#init} runs first).
     * The applier then executes before the scenario runner each frame, so a screen armed by a synthetic
     * chat command is guaranteed to be applied on the following frame, never in the same one.
     */
    public static void installScreenApplier() {
        if (applierInstalled) return;
        applierInstalled = true;
        FrameEvents.subscribe(ClientCommands::applyPendingScreen);
    }

    private static void applyPendingScreen() {
        frameCounter++;
        var pending = pendingScreen;
        if (pending == null || frameCounter <= pending.armedFrame()) {
            return;
        }
        pendingScreen = null;
        var minecraft = Minecraft.getInstance();
        LDLib2.LOGGER.info("[uitest] ldlib2_screen_test {}: applying pending screen on frame {} (gui tick {})",
                pending.name(), frameCounter, minecraft.gui.getGuiTicks());
        minecraft.setScreen(pending.screen());
    }

    private static LiteralArgumentBuilder<ClientCommandSourceStack> createScreenTestCommands() {
        var builder = createLiteral("ldlib2_screen_test");
        if (LDLib2ClientRegistries.SCREEN_TESTS == null) {
            return builder;
        }
        LDLib2.LOGGER.info("[uitest] ldlib2_screen_test subcommands ({}) sorted: {}",
                LDLib2ClientRegistries.SCREEN_TESTS.values().size(),
                LDLib2ClientRegistries.SCREEN_TESTS.keys().stream().sorted().toList());
        for (var uiTest : LDLib2ClientRegistries.SCREEN_TESTS) {
            builder = builder.then(createLiteral(uiTest.annotation().name())
                    .executes(context -> {
                        var test = uiTest.value().get();
                        var minecraft = Minecraft.getInstance();
                        var entityPlayer = minecraft.player;
                        if (entityPlayer == null) return 0;
                        var name = uiTest.annotation().name();
                        var ui = test.createUI(entityPlayer);
                        // Arm, do not open: see PendingScreen. The frame hook applies it once the counter
                        // has moved on, which is always after ChatScreen's own setScreen(null).
                        pendingScreen = new PendingScreen(
                                new ModularUIScreen(ui, Component.empty()), name, frameCounter);
                        LDLib2.LOGGER.info("[uitest] ldlib2_screen_test {}: armed pending screen on frame {} (gui tick {})",
                                name, frameCounter, minecraft.gui.getGuiTicks());
                        return 1;
                    }));
        }
        return builder;
    }
}
