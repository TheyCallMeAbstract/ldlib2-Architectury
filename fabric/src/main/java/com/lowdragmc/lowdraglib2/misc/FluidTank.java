package com.lowdragmc.lowdraglib2.misc;

import dev.architectury.fluid.FluidStack;

import java.util.function.Predicate;

/**
 * Fabric emulation of the (deprecated) NeoForge type
 * {@code net.neoforged.neoforge.fluids.capability.templates.FluidTank}.
 * <p>
 * NeoForge's {@code FluidTank} is a concrete {@code IFluidHandler}/{@code IFluidTank}
 * implementation with no Architectury equivalent, yet LDLib2's documentation uses it
 * directly ({@code new FluidTank(2000)}). This class re-exposes the same documented
 * public surface (identical method names and semantics) on Fabric by extending the
 * already-present Fabric {@link FluidStorage} port, so that loader-neutral consumer code
 * compiles and behaves the same without loader conditionals.
 * <p>
 * Because {@link FluidStorage} already implements {@link IFluidHandlerModifiable},
 * {@link com.lowdragmc.lowdraglib2.syncdata.IContentChangeAware} and
 * {@link com.lowdragmc.lowdraglib2.common.io.SerializableIO} (the cross-platform analogue
 * of NeoForge's {@code ValueIOSerializable}), persistence via
 * {@code serialize(ValueOutput)}/{@code deserialize(ValueInput)} rounds-trips exactly as
 * the NeoForge original does.
 * <p>
 * Note: Fabric amounts are Architectury droplets ({@code long}); the amount accessors on
 * this class return {@code int} to match NeoForge's {@code getFluidAmount()}.
 */
public class FluidTank extends FluidStorage {

    public FluidTank(int capacity) {
        super(capacity);
    }

    public FluidTank(int capacity, Predicate<FluidStack> validator) {
        super(capacity, validator);
    }

    /**
     * Mirrors {@code FluidTank#setCapacity(int)} on NeoForge (fluent).
     */
    public FluidTank setCapacity(int capacity) {
        this.capacity = capacity;
        return this;
    }

    /**
     * Mirrors {@code FluidTank#setValidator(Predicate)} on NeoForge (fluent, ignores null).
     */
    public FluidTank setValidator(Predicate<FluidStack> validator) {
        if (validator != null) {
            this.validator = validator;
        }
        return this;
    }

    /**
     * Mirrors {@code FluidTank#getCapacity()} on NeoForge.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Mirrors {@code FluidTank#isEmpty()} on NeoForge.
     */
    public boolean isEmpty() {
        return fluid.isEmpty();
    }

    /**
     * Mirrors {@code FluidTank#getSpace()} on NeoForge.
     */
    public int getSpace() {
        return Math.max(0, capacity - getFluidAmount());
    }
}
