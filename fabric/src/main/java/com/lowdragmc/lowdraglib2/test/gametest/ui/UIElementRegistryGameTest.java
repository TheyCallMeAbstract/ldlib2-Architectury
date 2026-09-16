package com.lowdragmc.lowdraglib2.test.gametest.ui;

import com.lowdragmc.lowdraglib2.LDLib2;
import com.lowdragmc.lowdraglib2.LDLib2Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;

public final class UIElementRegistryGameTest {
    static final String TEST_PATH = "ui_registry_test";

    private UIElementRegistryGameTest() {
    }

    public static void run(GameTestHelper helper) {
        LDLib2.LOGGER.info("Start UI Registry Test");
        for (var holder : LDLib2Registries.UI_ELEMENTS.values()) {
            var element = holder.value().get();
            element.deserialize(TagValueInput.create(
                    ProblemReporter.Collector.DISCARDING,
                    helper.getLevel().registryAccess(),
                    new CompoundTag()
            ));
        }
        LDLib2.LOGGER.info("End UI Registry Test");
        helper.succeed();
    }
}
