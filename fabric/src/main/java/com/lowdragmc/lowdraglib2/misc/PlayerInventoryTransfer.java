package com.lowdragmc.lowdraglib2.misc;

import net.minecraft.world.entity.player.Inventory;

public class PlayerInventoryTransfer extends InvWrapper {
    public PlayerInventoryTransfer(Inventory inv) {
        super(inv);
    }

    @Override
    public int getSlots() {
        return 36;
    }
}
