package com.lowdragmc.lowdraglib2.misc;

import dev.architectury.fluid.FluidStack;

/**
 * Extensions to the base {@link IFluidHandler} — mirrors NeoForge's IFluidHandlerModifiable.
 */
public interface IFluidHandlerModifiable extends IFluidHandler {

    void setFluidInTank(int tank, FluidStack stack);

    default boolean supportsFill(int tank) {
        return true;
    }

    default boolean supportsDrain(int tank) {
        return true;
    }
}
