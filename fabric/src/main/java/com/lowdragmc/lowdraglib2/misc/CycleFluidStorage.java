package com.lowdragmc.lowdraglib2.misc;

import dev.architectury.fluid.FluidStack;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * CycleFluidStorage — Fabric port.
 * NeoForge's IFluidTank interface has no Architectury equivalent; we implement IFluidHandlerModifiable only.
 */
public class CycleFluidStorage implements IFluidHandlerModifiable {
    private List<FluidStack> storages;
    @Getter @Setter
    private int capacity;

    public CycleFluidStorage(int capacity, List<FluidStack> storages) {
        setCapacity(capacity);
        updateStacks(storages);
    }

    public void updateStacks(List<FluidStack> storages) {
        this.storages = storages;
    }

    @NotNull
    public FluidStack getFluid() {
        return storages == null || storages.isEmpty() ? FluidStack.empty() : storages.get(Math.abs((int)(System.currentTimeMillis() / 1000) % storages.size()));
    }

    public int getFluidAmount() {
        return (int) getFluid().getAmount();
    }

    public void setFluid(FluidStack fluid) {
        updateStacks(List.of(fluid));
    }

    @Override
    public void setFluidInTank(int tank, FluidStack stack) {
        setFluid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return FluidStack.empty();
    }

    @Override
    public boolean supportsFill(int tank) {
        return false;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return FluidStack.empty();
    }

    @Override
    public boolean supportsDrain(int tank) {
        return false;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return true;
    }
}
