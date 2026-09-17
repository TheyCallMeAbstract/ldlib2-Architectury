package com.lowdragmc.lowdraglib2.misc;

import com.lowdragmc.lowdraglib2.common.io.SerializableIO;
import dev.architectury.fluid.FluidStack;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Compile-and-behaviour assertions for the loader-neutral shapes LDLib2 documents
 * for NeoForge consumers. If any of these types/methods disappear from the Fabric tree,
 * documented consumer code ({@code new FluidTank(2000)}, {@code implements INBTSerializable}
 * replaced by {@link SerializableIO}) stops compiling, and this test fails loudly.
 */
class FluidTankConsumerCompatTest {

    @BeforeAll
    static void bootstrapRegistries() {
        // Architectury's FluidStack.CODEC touches BuiltInRegistries at class-init; vanilla content
        // must be bootstrapped before the first FluidTank is constructed.
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void documented_constructor_exposes_capacity_and_empty_state() {
        FluidTank tank = new FluidTank(2000);
        assertEquals(2000, tank.getCapacity());
        assertEquals(2000, tank.getTankCapacity(0));
        assertEquals(1, tank.getTanks());
        assertEquals(0, tank.getFluidAmount());
        assertEquals(2000, tank.getSpace());
        assertTrue(tank.isEmpty());
        assertTrue(tank.getFluid().isEmpty());
        assertTrue(tank.getFluidInTank(0).isEmpty());
    }

    @Test
    void default_validator_accepts_any_stack_and_fluent_setters_chain() {
        FluidTank tank = new FluidTank(2000);
        assertTrue(tank.isFluidValid(FluidStack.empty()));
        assertTrue(tank.isFluidValid(0, FluidStack.empty()));

        assertSame(tank, tank.setCapacity(4000));
        assertSame(tank, tank.setValidator(stack -> false));
        assertEquals(4000, tank.getCapacity());
        assertEquals(4000, tank.getSpace());
        assertFalse(tank.isFluidValid(FluidStack.empty()));
        assertFalse(tank.isFluidValid(0, FluidStack.empty()));

        // NeoForge's setValidator(null) keeps the existing validator instead of clearing it.
        tank.setValidator(null);
        assertFalse(tank.isFluidValid(FluidStack.empty()));
    }

    @Test
    void empty_transfers_are_no_ops() {
        FluidTank tank = new FluidTank(2000);
        assertEquals(0, tank.fill(FluidStack.empty(), IFluidHandler.FluidAction.EXECUTE));
        assertEquals(0, tank.fill(FluidStack.empty(), IFluidHandler.FluidAction.SIMULATE));
        assertTrue(tank.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty());
        assertTrue(tank.drain(FluidStack.empty(), IFluidHandler.FluidAction.EXECUTE).isEmpty());
        assertTrue(tank.isEmpty());
        assertEquals(0, tank.getFluidAmount());
    }

    @Test
    void tank_advertises_cross_platform_handler_and_persistence_supertypes() {
        FluidTank tank = new FluidTank(2000);
        assertInstanceOf(IFluidHandlerModifiable.class, tank);
        assertInstanceOf(IFluidHandler.class, tank);
        assertInstanceOf(SerializableIO.class, tank);
    }

    @Test
    void empty_tank_round_trips_through_serializable_io() {
        FluidTank source = new FluidTank(2000);
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, RegistryAccess.EMPTY);
        source.serialize(output);
        CompoundTag tag = output.buildResult();

        FluidTank restored = new FluidTank(1234);
        restored.deserialize(TagValueInput.create(
                ProblemReporter.DISCARDING, RegistryAccess.EMPTY, tag));
        assertTrue(restored.isEmpty());
        assertEquals(0, restored.getFluidAmount());
        // Capacity is a runtime property; only the contained fluid is on the wire (NeoForge parity).
        assertEquals(1234, restored.getCapacity());
    }

    @Test
    void serializable_io_contract_round_trips_consumer_data() {
        TagValueOutput output = TagValueOutput.createWithContext(
                ProblemReporter.DISCARDING, RegistryAccess.EMPTY);
        new CounterHolder(7).serialize(output);
        CounterHolder restored = new CounterHolder(0);
        restored.deserialize(TagValueInput.create(
                ProblemReporter.DISCARDING, RegistryAccess.EMPTY, output.buildResult()));
        assertEquals(7, restored.count);
    }

    /** Mirrors the consumer pattern docs show as {@code implements INBTSerializable<CompoundTag>}. */
    static final class CounterHolder implements SerializableIO {
        int count;

        CounterHolder(int count) {
            this.count = count;
        }

        @Override
        public void serialize(ValueOutput output) {
            output.putInt("Count", count);
        }

        @Override
        public void deserialize(ValueInput input) {
            count = input.getIntOr("Count", -1);
        }
    }
}
