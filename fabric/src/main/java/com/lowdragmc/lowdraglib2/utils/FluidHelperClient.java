package com.lowdragmc.lowdraglib2.utils;

import dev.architectury.fluid.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.client.resources.model.sprite.Material;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FluidHelperClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(FluidHelperClient.class);

    private FluidHelperClient() {
    }

    public static FluidModel getFluidModel(FluidStack fluidStack) {
        return Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidStack.getFluid().defaultFluidState());
    }

    public static int getColor(FluidStack fluidStack) {
        var model = getFluidModel(fluidStack);
        var tintSource = model.tintSource();
        if (tintSource != null) {
            var fluid = fluidStack.getFluid();
            var defaultState = fluid.defaultFluidState();
            // Use reflection to access the protected createLegacyBlock method
            try {
                var createLegacyBlock = fluid.getClass().getMethod("createLegacyBlock", FluidState.class);
                var legacyBlock = (net.minecraft.world.level.block.state.BlockState) createLegacyBlock.invoke(fluid, defaultState);
                return tintSource.color(legacyBlock);
            } catch (Exception e) {
                LOGGER.warn("Failed to get fluid color via createLegacyBlock, falling back to default", e);
                return -1;
            }
        }
        return -1;
    }

    public static Material.Baked getStillMaterial(FluidStack fluidStack) {
        var model = getFluidModel(fluidStack);
        return model.stillMaterial();
    }
}
