package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.misc.FluidStorage;
import com.lowdragmc.lowdraglib2.misc.FluidTransferList;
import com.lowdragmc.lowdraglib2.misc.ItemStackHandler;
import com.lowdragmc.lowdraglib2.misc.ItemTransferList;
import com.lowdragmc.lowdraglib2.syncdata.IPersistedSerializable;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import dev.architectury.fluid.FluidStack;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/**
 * Consumer-level proof that {@link ItemTransferList} (and the analogous {@link FluidTransferList})
 * actually round-trip through the persistence path on Fabric.
 * <p>
 * Before this capability was restored, Fabric's transfer lists did not implement the platform's
 * serialize/deserialize contract, so a {@code @Persisted(subPersisted = true)} field was written as an
 * empty map and the inner slots/tanks came back empty. If persistence regresses, the restored
 * assertions fail at runtime rather than at compile time.
 */
public class FabricItemTransferListGameTest {

    public static class Holder implements IPersistedSerializable {
        @Persisted(subPersisted = true)
        public ItemTransferList itemTransferList;
        @Persisted(subPersisted = true)
        public FluidTransferList fluidTransferList;

        public Holder(ItemTransferList itemTransferList, FluidTransferList fluidTransferList) {
            this.itemTransferList = itemTransferList;
            this.fluidTransferList = fluidTransferList;
        }

        public CompoundTag serializeNBT(HolderLookup.Provider provider) {
            var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
            this.serialize(output);
            return output.buildResult();
        }

        public void deserializeNBT(RegistryAccess provider, CompoundTag nbt) {
            var input = TagValueInput.create(ProblemReporter.DISCARDING, provider, nbt);
            this.deserialize(input);
        }
    }

    @GameTest
    public void transfer_lists_round_trip_through_persistence(GameTestHelper helper) {
        var provider = helper.getLevel().registryAccess();

        var srcItemsA = new ItemStackHandler(2);
        srcItemsA.setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));
        srcItemsA.setStackInSlot(1, new ItemStack(Items.APPLE, 3));
        var srcItemsB = new ItemStackHandler(1);
        srcItemsB.setStackInSlot(0, new ItemStack(Items.EMERALD, 64));
        var srcItemList = new ItemTransferList(srcItemsA, srcItemsB);

        var srcFluidTank = new FluidStorage(8000);
        srcFluidTank.setFluid(FluidStack.create(Fluids.WATER, FluidStack.bucketAmount()));
        var srcFluidList = new FluidTransferList(srcFluidTank);

        var nbt = new Holder(srcItemList, srcFluidList).serializeNBT(provider);

        // Destination wraps fresh, empty containers with the same layout.
        var dstItemsA = new ItemStackHandler(2);
        var dstItemsB = new ItemStackHandler(1);
        var dstFluidTank = new FluidStorage(8000);
        var dst = new Holder(new ItemTransferList(dstItemsA, dstItemsB), new FluidTransferList(dstFluidTank));
        dst.deserializeNBT(provider, nbt);

        if (!ItemStack.matches(new ItemStack(Items.DIAMOND, 5), dst.itemTransferList.getStackInSlot(0))) {
            helper.fail("slot 0 did not persist: " + dst.itemTransferList.getStackInSlot(0));
            return;
        }
        if (!ItemStack.matches(new ItemStack(Items.APPLE, 3), dst.itemTransferList.getStackInSlot(1))) {
            helper.fail("slot 1 did not persist: " + dst.itemTransferList.getStackInSlot(1));
            return;
        }
        if (!ItemStack.matches(new ItemStack(Items.EMERALD, 64), dst.itemTransferList.getStackInSlot(2))) {
            helper.fail("slot 2 (second handler) did not persist: " + dst.itemTransferList.getStackInSlot(2));
            return;
        }

        var fluid = dst.fluidTransferList.getFluidInTank(0);
        if (fluid.isEmpty() || !fluid.isFluidEqual(FluidStack.create(Fluids.WATER, 1))) {
            helper.fail("fluid tank did not persist: " + fluid);
            return;
        }
        if (fluid.getAmount() != FluidStack.bucketAmount()) {
            helper.fail("fluid amount did not persist: expected " + FluidStack.bucketAmount() + ", got " + fluid.getAmount());
            return;
        }
        helper.succeed();
    }
}
