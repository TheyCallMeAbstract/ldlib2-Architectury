package com.lowdragmc.lowdraglib2.test.uitest;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.LDLib2Registries;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerScreen;
import com.lowdragmc.lowdraglib2.registry.RegistrationEnvironment;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegisterClient;
import com.lowdragmc.lowdraglib2.uitest.ScenarioBuilder;
import com.lowdragmc.lowdraglib2.uitest.ScenarioOptions;
import com.lowdragmc.lowdraglib2.uitest.UIScenario;

/**
 * Opens every entry of the {@code ldlib2:menu_test} registry through the real menu path — server
 * {@code openUI} → open-screen packet → {@code ModularUIContainerScreen} — and asserts that each one
 * renders a non-trivial UI before screenshotting it.
 *
 * <p>This closes the gap left by the {@code /ldlib2_autotest} harness: the {@code ldlib2:ui_scenario}
 * registry only ever covered the scenarios written against it, so the menu tests registered under
 * {@code ldlib2:menu_test} could rot while every run stayed green. The sweep is deliberately
 * registry-driven, so a newly registered menu test is covered automatically without touching this
 * class.
 *
 * <p>Fabric-only: the NeoForge tree is not part of this milestone, and the scenario is registered
 * {@link RegistrationEnvironment#DEV_ONLY} like the rest of the harness.
 */
@LDLRegisterClient(name = "menu_test_sweep", group = "ldlib2", registry = UIScenario.REGISTRY,
        environment = RegistrationEnvironment.DEV_ONLY)
public class MenuTestSweepScenario implements UIScenario {

    @Override
    public void configure(ScenarioOptions options) {
        options.defaultSettleMs(30).tags("menu_tests").requiresWorld(true).guiScale(3);
    }

    @Override
    public void define(ScenarioBuilder s) {
        var registry = LDLib2Registries.MENU_TESTS;
        if (registry == null) {
            throw new IllegalStateException("The ldlib2:menu_test registry is not available "
                    + "(it only exists in a development environment)");
        }

        var names = registry.values().stream()
                .map(holder -> holder.annotation().name())
                .sorted()
                .toList();
        if (names.isEmpty()) {
            throw new IllegalStateException("No menu tests are registered under ldlib2:menu_test, "
                    + "so a sweep would silently pass while exercising nothing");
        }

        for (int i = 0; i < names.size(); i++) {
            var name = names.get(i);
            var index = "%02d".formatted(i + 1);
            s.server("open " + name, sc -> {
                        if (!PlayerUIMenuType.openUI(sc.player(), LDLib2.id(name))) {
                            throw new IllegalStateException("No menu test registered as '" + name + "'");
                        }
                    })
                    .awaitScreen(ModularUIContainerScreen.class)
                    .awaitModularUI()
                    .checkScreen(ModularUIContainerScreen.class)
                    // Generic render assertion: #root only exists on some menu tests, so instead of a
                    // selector we assert the registry-backed ModularUI has more than the root element.
                    // getAllElements() always contains the root plus every registered descendant, and
                    // every menu test builds at least one child, so >1 proves layout produced a real tree.
                    .check("the " + name + " menu rendered a non-trivial element tree",
                            ctx -> ctx.requireUI().getAllElements().size() > 1)
                    .screenshot(index + "_" + name)
                    .closeScreen()
                    .server("close the " + name + " container", sc -> sc.player().closeContainer());
        }

        s.teardown("close the container", ctx -> ctx.requirePlayer().closeContainer());
    }
}
