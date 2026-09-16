package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.test.gametest.registry.RegistrationEnvironmentGameTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricRegistryGameTests {

    @GameTest
    public void reg_env_always(GameTestHelper helper) {
        RegistrationEnvironmentGameTest.alwaysEntryIsRegistered(helper);
    }

    @GameTest
    public void reg_env_dev_only(GameTestHelper helper) {
        RegistrationEnvironmentGameTest.devOnlyEntryIsRegisteredInDev(helper);
    }

    @GameTest
    public void reg_env_prod_only(GameTestHelper helper) {
        RegistrationEnvironmentGameTest.productionOnlyEntryIsNotRegisteredInDev(helper);
    }

    @GameTest
    public void reg_env_manual(GameTestHelper helper) {
        RegistrationEnvironmentGameTest.manualEntryIsNotAutoRegistered(helper);
    }
}
