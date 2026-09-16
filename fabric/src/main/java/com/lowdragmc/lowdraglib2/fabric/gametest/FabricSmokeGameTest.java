package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.test.gametest.SharedSmokeGameTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricSmokeGameTest {

    @GameTest
    public void smoke(GameTestHelper helper) {
        SharedSmokeGameTest.smoke(helper);
    }
}
