package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.test.gametest.ContainerMenuEventGameTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricContainerMenuGameTest {

    @GameTest
    public void create_fires_on_menu_open(GameTestHelper helper) {
        ContainerMenuEventGameTest.createFiresOnMenuOpen(helper);
    }
}
