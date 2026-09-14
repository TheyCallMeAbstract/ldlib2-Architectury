package com.lowdragmc.lowdraglib2.misc;

import com.google.common.util.concurrent.Runnables;
import com.lowdragmc.lowdraglib2.syncdata.IContentChangeAware;
import dev.architectury.fluid.FluidStack;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Predicate;

/**
 * FluidStorage — Fabric port.
 * NeoForge's FluidTank is a concrete class with no Architectury equivalent.
 * This implements IFluidHandlerModifiable directly with the same semantics.
 */
public class FluidStorage implements IFluidHandlerModifiable, IContentChangeAware {
    @Getter
    @Setter
    private Runnable onContentsChanged = Runnables.doNothing();

    protected int capacity;
    protected Predicate<FluidStack> validator;
    protected FluidStack fluid = FluidStack.empty();

    public FluidStorage(int capacity) {
        this.capacity = capacity;
        this.validator = fs -> true;
    }

    public FluidStorage(int capacity, Predicate<FluidStack> validator) {
        this.capacity = capacity;
        this.validator = validator;
    }

    public FluidStack getFluid() {
        return fluid;
    }

    public void setFluid(FluidStack fluid) {
        this.fluid = fluid;
    }

    @Override
    public void setFluidInTank(int tank, FluidStack fluid) {
        this.fluid = fluid;
        onContentsChanged();
    }

    public void setFluidInTank(FluidStack fluid, boolean notify) {
        this.fluid = fluid;
        if (notify) {
            onContentsChanged();
        }
    }

    public void onContentsChanged() {
        onContentsChanged.run();
    }

    public int getFluidAmount() {
        return (int) fluid.getAmount();
    }

    public boolean isFluidValid(FluidStack stack) {
        return validator.test(stack);
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return fluid;
    }

    @Override
    public int getTankCapacity(int tank) {
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !isFluidValid(resource)) return 0;
        if (fluid.isEmpty()) {
            long filled = Math.min(resource.getAmount(), capacity);
            if (action.isExecute()) {
                fluid = resource.copyWithAmount(filled);
                onContentsChanged();
            }
            return (int) filled;
        }
        if (!fluid.isFluidEqual(resource)) return 0;
        long filled = Math.min(resource.getAmount(), capacity - fluid.getAmount());
        if (filled > 0 && action.isExecute()) {
            fluid.grow(filled);
            onContentsChanged();
        }
        return (int) filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !resource.isFluidEqual(fluid)) return FluidStack.empty();
        return drain((int) resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        if (fluid.isEmpty() || maxDrain <= 0) return FluidStack.empty();
        long drained = Math.min(maxDrain, fluid.getAmount());
        FluidStack result = fluid.copyWithAmount(drained);
        if (action.isExecute() && drained > 0) {
            fluid.shrink(drained);
            onContentsChanged();
        }
        return result;
    }

    @Override
    public boolean supportsFill(int tank) {
        return true;
    }

    @Override
    public boolean supportsDrain(int tank) {
        return true;
    }

    public FluidStorage copy() {
        var storage = new FluidStorage(capacity, validator);
        storage.setFluid(fluid.copy());
        return storage;
    }
}
