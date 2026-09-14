package com.lowdragmc.lowdraglib2.utils;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.FluidStackHooks;
import lombok.experimental.UtilityClass;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockAndLightGetter;
import net.minecraft.world.level.Level;

/**
 * @author KilaBash
 * @date 2023/2/10
 * @implNote FluidHelper — Fabric port
 */
@UtilityClass
public final class FluidHelper {

    public static int getBucket() {
        return (int) FluidStack.bucketAmount();
    }

    public static Component getDisplayName(FluidStack fluidStack) {
        return fluidStack.getName();
    }

    // NeoForge-specific methods (FluidType.getTemperature, isLighterThanAir, etc.)
    // have no direct Architectury equivalent — stubbed with sensible defaults.

    public static int getTemperature(FluidStack fluidStack) {
        return 300; // default room temperature
    }

    public static boolean isLighterThanAir(FluidStack fluidStack) {
        return false;
    }

    public static boolean canBePlacedInWorld(FluidStack fluidStack, BlockAndLightGetter level, BlockPos pos) {
        return true;
    }

    public static boolean doesVaporize(FluidStack fluidStack, Level level, BlockPos pos) {
        return false;
    }

    public static Object toRealFluidStack(FluidStack fluidStack) {
        return fluidStack;
    }

    public static String getUnit() {
        return "mB";
    }
}
