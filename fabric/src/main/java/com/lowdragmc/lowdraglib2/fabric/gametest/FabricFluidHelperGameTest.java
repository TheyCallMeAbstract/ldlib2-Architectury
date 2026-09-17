package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.utils.FluidHelper;
import dev.architectury.fluid.FluidStack;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.material.Fluids;

/**
 * Consumer-level proof that Fabric's {@link FluidHelper} reports real fluid properties instead of
 * the previous hardcoded stub values (which reported 300 K for every fluid).
 */
public class FabricFluidHelperGameTest {

    @GameTest
    public void fluid_properties_are_read_from_the_fluid(GameTestHelper helper) {
        int lavaTemperature = FluidHelper.getTemperature(FluidStack.create(Fluids.LAVA, 1000));
        if (lavaTemperature != 1300) {
            helper.fail("Expected lava temperature 1300, got " + lavaTemperature
                    + " (300 would mean the old hardcoded stub is still in place)");
            return;
        }
        int waterTemperature = FluidHelper.getTemperature(FluidStack.create(Fluids.WATER, 1000));
        if (waterTemperature != 300) {
            helper.fail("Expected water temperature 300, got " + waterTemperature);
            return;
        }
        if (FluidHelper.isLighterThanAir(FluidStack.create(Fluids.WATER, 1000))) {
            helper.fail("Water must not be lighter than air");
            return;
        }
        if (FluidHelper.getEmptySound(FluidStack.create(Fluids.WATER, 1000)) == null) {
            helper.fail("Water must have a bucket-empty sound");
            return;
        }
        helper.succeed();
    }
}
