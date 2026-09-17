package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.misc.FluidStorage;
import dev.architectury.fluid.FluidStack;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

/**
 * Consumer-level proof that Fabric's {@link FluidSlot} container interaction behaves like NeoForge's
 * FluidUtil path: clicking a slot with a bucket on the cursor fills the bucket from the slot's tank,
 * and clicking with a filled bucket empties it into the tank.
 */
public class FabricFluidSlotGameTest {

    @GameTest
    public void click_container_exchanges_fluid_with_cursor_bucket(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        player.openMenu(new SimpleMenuProvider(
                (id, inventory, opener) -> new ChestMenu(MenuType.GENERIC_9x1, id, inventory, new SimpleContainer(9), 1),
                Component.literal("ldlib2 fluid slot container test")));
        var menu = player.containerMenu;
        // On Fabric, Architectury amounts are droplets: one bucket is FluidStack.bucketAmount() (81000),
        // which is exactly what the Fabric Transfer API's item fluid storages expect.
        long bucket = FluidStack.bucketAmount();

        // Fill direction: tank -> empty bucket on the cursor.
        var tank = new FluidStorage((int) (bucket * 2));
        tank.setFluid(FluidStack.create(Fluids.WATER, bucket));
        menu.setCarried(new ItemStack(Items.BUCKET));
        if (!FluidSlot.clickContainer(tank, 0, player, menu, false, true, true)) {
            helper.fail("clicking a filled fluid slot with an empty bucket on the cursor moved no fluid");
            return;
        }
        if (tank.getFluidAmount() != 0) {
            helper.fail("expected the slot's tank to be drained to 0, but it holds " + tank.getFluidAmount());
            return;
        }
        if (!menu.getCarried().is(Items.WATER_BUCKET)) {
            helper.fail("expected the cursor bucket to become a water bucket, got " + menu.getCarried());
            return;
        }

        // Empty direction: filled bucket on the cursor -> tank.
        var emptyTank = new FluidStorage((int) (bucket * 2));
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));
        if (!FluidSlot.clickContainer(emptyTank, 0, player, menu, false, true, true)) {
            helper.fail("clicking an empty fluid slot with a full bucket on the cursor moved no fluid");
            return;
        }
        if (emptyTank.getFluidAmount() != bucket || !emptyTank.getFluid().isFluidEqual(FluidStack.create(Fluids.WATER, bucket))) {
            helper.fail("expected the slot's tank to hold one bucket of water, got " + emptyTank.getFluid());
            return;
        }
        if (!menu.getCarried().is(Items.BUCKET)) {
            helper.fail("expected the cursor water bucket to become an empty bucket, got " + menu.getCarried());
            return;
        }

        helper.succeed();
    }
}
