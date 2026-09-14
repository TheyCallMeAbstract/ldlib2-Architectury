package com.lowdragmc.lowdraglib2.misc;

import dev.architectury.fluid.FluidStack;
import org.jetbrains.annotations.NotNull;

/**
 * Fabric-compatible mirror of NeoForge's IFluidHandler.
 * On NeoForge this maps to net.neoforged.neoforge.fluids.capability.IFluidHandler.
 */
public interface IFluidHandler {

    int getTanks();

    @NotNull
    FluidStack getFluidInTank(int tank);

    int getTankCapacity(int tank);

    boolean isFluidValid(int tank, @NotNull FluidStack stack);

    int fill(FluidStack resource, FluidAction action);

    @NotNull
    FluidStack drain(FluidStack resource, FluidAction action);

    @NotNull
    FluidStack drain(int maxDrain, FluidAction action);

    enum FluidAction {
        EXECUTE,
        SIMULATE;

        public boolean isSimulate() {
            return this == SIMULATE;
        }

        public boolean isExecute() {
            return this == EXECUTE;
        }
    }
}
