package com.lowdragmc.lowdraglib2.utils;

import dev.architectury.fluid.FluidStack;
import dev.architectury.hooks.fluid.fabric.FluidStackHooksFabric;
import lombok.experimental.UtilityClass;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.attribute.EnvironmentAttributes;
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

    // NeoForge reads these from FluidType. Fabric has no FluidType; the Fabric Transfer API's
    // FluidVariantAttributes is the equivalent registered per-fluid attribute source and is the
    // exact API Architectury's own FluidStackHooks delegates to on Fabric. Divergence: attribute
    // values come from FluidVariantAttributeHandler instead of FluidType, but the values and
    // defaults (water 300 K, lighter-than-air false) are identical.

    public static int getTemperature(FluidStack fluidStack) {
        return FluidVariantAttributes.getTemperature(toVariant(fluidStack));
    }

    public static boolean isLighterThanAir(FluidStack fluidStack) {
        return FluidVariantAttributes.isLighterThanAir(toVariant(fluidStack));
    }

    public static boolean canBePlacedInWorld(FluidStack fluidStack, BlockAndLightGetter level, BlockPos pos) {
        // NeoForge FluidType#canBePlacedInLevel is final and, with the default getBlockForFluidState,
        // reduces to !state.createLegacyBlock().isAir() for the fluid's default state. That is pure
        // vanilla logic, so it is reproduced exactly here rather than stubbed.
        return !fluidStack.getFluid().defaultFluidState().createLegacyBlock().isAir();
    }

    public static boolean doesVaporize(FluidStack fluidStack, Level level, BlockPos pos) {
        // NeoForge gates on the vanilla WATER_EVAPORATES environment attribute and the water tag.
        // The per-position overload is a NeoForge interface injection, so Fabric uses the vanilla
        // dimension-scoped value (the attribute's defaults are dimension-level) - same result.
        return level.environmentAttributes().getDimensionValue(EnvironmentAttributes.WATER_EVAPORATES)
                && fluidStack.getFluid().defaultFluidState().is(FluidTags.WATER);
    }

    public static SoundEvent getEmptySound(FluidStack fluidStack) {
        return FluidVariantAttributes.getEmptySound(toVariant(fluidStack));
    }

    public static SoundEvent getFillSound(FluidStack fluidStack) {
        return FluidVariantAttributes.getFillSound(toVariant(fluidStack));
    }

    public static Object toRealFluidStack(FluidStack fluidStack) {
        return fluidStack;
    }

    public static String getUnit() {
        return "mB";
    }

    private static FluidVariant toVariant(FluidStack fluidStack) {
        return FluidStackHooksFabric.toFabric(fluidStack);
    }
}
