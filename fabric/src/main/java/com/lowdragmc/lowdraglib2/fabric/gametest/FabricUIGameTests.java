package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.test.gametest.ui.SliderGameTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricUIGameTests {

    @GameTest
    public void slider_registered_under_both_names(GameTestHelper helper) {
        SliderGameTest.registeredUnderBothNames(helper);
    }

    @GameTest
    public void slider_value_is_clamped_to_range(GameTestHelper helper) {
        SliderGameTest.valueIsClampedToRange(helper);
    }

    @GameTest
    public void slider_normalized_value_round_trips(GameTestHelper helper) {
        SliderGameTest.normalizedValueRoundTrips(helper);
    }

    @GameTest
    public void slider_listeners_only_fire_on_real_changes(GameTestHelper helper) {
        SliderGameTest.listenersOnlyFireOnRealChanges(helper);
    }

    @GameTest
    public void slider_step_moves_by_a_fraction_of_the_range(GameTestHelper helper) {
        SliderGameTest.stepMovesByAFractionOfTheRange(helper);
    }
}
