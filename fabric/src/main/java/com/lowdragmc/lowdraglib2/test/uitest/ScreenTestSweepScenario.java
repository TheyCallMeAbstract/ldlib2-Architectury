package com.lowdragmc.lowdraglib2.test.uitest;

import com.lowdragmc.lowdraglib2.client.LDLib2ClientRegistries;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIScreen;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;

/**
 * Opens every entry of the {@code ldlib2:screen_test} registry through the real screen path —
 * {@code IScreenTest#createUI(Player)} → {@code ModularUIScreen} — and asserts that each one renders
 * a non-trivial UI before screenshotting it and closing it again.
 *
 * <p>This closes the gap left by the {@code /ldlib2_autotest} harness: the {@code ldlib2:ui_scenario}
 * registry only ever covered the scenarios written against it, so two of the sixteen screen tests
 * registered under {@code ldlib2:screen_test} were exercised by hand-written scenarios while the
 * rest could rot. The sweep is deliberately registry-driven, so a newly registered screen test is
 * covered automatically without touching this class.
 *
 * <p>Unlike the menu sweep there is no server round trip and no container: a screen test is built
 * client-side by {@link ScenarioBuilder#openScreenTest(String)}, which takes the exact same path as
 * {@code /ldlib2_screen_test <name>}. A world is still required because {@code createUI} takes a
 * {@code Player}.
 *
 * <p>Fabric-only: the NeoForge tree is not part of this milestone, and the scenario is registered
 * {@link RegistrationEnvironment#DEV_ONLY} like the rest of the harness.
 */
@LDLRegisterClient(name = "screen_test_sweep", group = "ldlib2", registry = UIScenario.REGISTRY,
        environment = RegistrationEnvironment.DEV_ONLY)
public class ScreenTestSweepScenario implements UIScenario {

    @Override
    public void configure(ScenarioOptions options) {
        options.defaultSettleMs(30).tags("screen_tests").requiresWorld(true).guiScale(3);
    }

    @Override
    public void define(ScenarioBuilder s) {
        var registry = LDLib2ClientRegistries.SCREEN_TESTS;
        if (registry == null) {
            throw new IllegalStateException("The ldlib2:screen_test registry is not available "
                    + "(it only exists in a development environment)");
        }

        var names = registry.values().stream()
                .map(holder -> holder.annotation().name())
                .sorted()
                .toList();
        if (names.isEmpty()) {
            throw new IllegalStateException("No screen tests are registered under ldlib2:screen_test, "
                    + "so a sweep would silently pass while exercising nothing");
        }

        for (int i = 0; i < names.size(); i++) {
            var name = names.get(i);
            var index = "%02d".formatted(i + 1);
            s.openScreenTest(name)
                    .awaitScreen(ModularUIScreen.class)
                    .awaitModularUI()
                    .checkScreen(ModularUIScreen.class)
                    // Generic render assertion: #root only exists on some screen tests, so instead of a
                    // selector we assert the registry-backed ModularUI has more than the root element.
                    // getAllElements() always contains the root plus every registered descendant, and
                    // every screen test builds at least one child, so >1 proves layout produced a real
                    // tree. requireUI() resolves the ModularUI through ModularUIWidget, which is a
                    // child of every ModularUIScreen, so it works identically for all of them.
                    .check("the " + name + " screen rendered a non-trivial element tree",
                            ctx -> ctx.requireUI().getAllElements().size() > 1)
                    .screenshot(index + "_" + name)
                    // Closed at the end of every iteration rather than once at the end: a screen left
                    // open by the previous iteration would make the next openScreenTest a no-op and the
                    // sweep would pass while skipping tests. closeScreen() is client-only here - the
                    // player.closeContainer() it also performs is a harmless no-op for a non-menu screen.
                    .closeScreen();
        }

        s.teardown("close the screen", ctx -> {
            var player = ctx.player();
            if (player != null) player.closeContainer();
            ctx.mc().setScreen(null);
        });
    }
}
