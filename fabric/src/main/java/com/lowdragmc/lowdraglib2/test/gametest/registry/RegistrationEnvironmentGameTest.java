package com.lowdragmc.lowdraglib2.test.gametest.registry;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.registry.AutoRegistry;
import com.lowdragmc.lowdraglib2.test.registry.ITestRegistryEntry;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.function.Supplier;

public final class RegistrationEnvironmentGameTest {
    static final String ALWAYS_PATH = "reg_env_always";
    static final String DEV_ONLY_PATH = "reg_env_dev_only";
    static final String PROD_ONLY_PATH = "reg_env_prod_only";
    static final String MANUAL_PATH = "reg_env_manual";

    public static final AutoRegistry.LDLibRegister<ITestRegistryEntry, Supplier<ITestRegistryEntry>> TEST_ENV_REGISTRY =
            AutoRegistry.LDLibRegister.create(LDLib2.id("test_env_registry"), ITestRegistryEntry.class, AutoRegistry::noArgsCreator);

    private RegistrationEnvironmentGameTest() {
    }

    public static void alwaysEntryIsRegistered(GameTestHelper helper) {
        var holder = TEST_ENV_REGISTRY.get("test_always");
        if (holder == null) {
            helper.fail("ALWAYS entry should be registered but was not found");
            return;
        }
        helper.succeed();
    }

    public static void devOnlyEntryIsRegisteredInDev(GameTestHelper helper) {
        var holder = TEST_ENV_REGISTRY.get("test_dev_only");
        if (holder == null) {
            helper.fail("DEV_ONLY entry should be registered in dev environment but was not found");
            return;
        }
        helper.succeed();
    }

    public static void productionOnlyEntryIsNotRegisteredInDev(GameTestHelper helper) {
        var holder = TEST_ENV_REGISTRY.get("test_production_only");
        if (holder != null) {
            helper.fail("PRODUCTION_ONLY entry should NOT be registered in dev environment");
            return;
        }
        helper.succeed();
    }

    public static void manualEntryIsNotAutoRegistered(GameTestHelper helper) {
        var holder = TEST_ENV_REGISTRY.get("test_manual");
        if (holder != null) {
            helper.fail("MANUAL entry should NOT be auto-registered");
            return;
        }
        helper.succeed();
    }
}
