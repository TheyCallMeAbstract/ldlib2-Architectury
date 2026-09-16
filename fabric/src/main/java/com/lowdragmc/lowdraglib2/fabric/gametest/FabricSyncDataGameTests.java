package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.test.gametest.syncdata.MapSerializationGameTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricSyncDataGameTests {

    @GameTest
    public void directKDirectV_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.directKDirectV_nbtRoundTrip(helper);
    }

    @GameTest
    public void directKCustomV_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.directKCustomV_nbtRoundTrip(helper);
    }

    @GameTest
    public void enumKVectorV_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.enumKVectorV_nbtRoundTrip(helper);
    }

    @GameTest
    public void directKReadOnlyV_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.directKReadOnlyV_nbtRoundTrip(helper);
    }

    @GameTest
    public void readOnlyManagedMap_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.readOnlyManagedMap_nbtRoundTrip(helper);
    }

    @GameTest
    public void readOnlyManagedListMap_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.readOnlyManagedListMap_nbtRoundTrip(helper);
    }

    @GameTest
    public void readOnlyManagedListMap_bufRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.readOnlyManagedListMap_bufRoundTrip(helper);
    }

    @GameTest
    public void bufferRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.bufferRoundTrip(helper);
    }

    @GameTest
    public void emptyMap_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.emptyMap_nbtRoundTrip(helper);
    }

    @GameTest
    public void wireFormatShape_nbt(GameTestHelper helper) {
        MapSerializationGameTest.wireFormatShape_nbt(helper);
    }

    @GameTest
    public void readOnlyKDirectV_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.readOnlyKDirectV_nbtRoundTrip(helper);
    }

    @GameTest
    public void readOnlyKReadOnlyV_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.readOnlyKReadOnlyV_nbtRoundTrip(helper);
    }

    @GameTest
    public void readOnlyKDirectV_bufRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.readOnlyKDirectV_bufRoundTrip(helper);
    }

    @GameTest
    public void readOnlyKReadOnlyV_bufRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.readOnlyKReadOnlyV_bufRoundTrip(helper);
    }

    @GameTest
    public void directKReadOnlyV_bufRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.directKReadOnlyV_bufRoundTrip(helper);
    }

    @GameTest
    public void autoFabricate_directKReadOnlyV_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.autoFabricate_directKReadOnlyV_nbtRoundTrip(helper);
    }

    @GameTest
    public void autoFabricate_directKReadOnlyV_bufRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.autoFabricate_directKReadOnlyV_bufRoundTrip(helper);
    }

    @GameTest
    public void autoFabricate_directKListV_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.autoFabricate_directKListV_nbtRoundTrip(helper);
    }

    @GameTest
    public void autoFabricate_directKListV_bufRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.autoFabricate_directKListV_bufRoundTrip(helper);
    }

    @GameTest
    public void autoFabricate_collection_readOnlyChild_nbtRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.autoFabricate_collection_readOnlyChild_nbtRoundTrip(helper);
    }

    @GameTest
    public void autoFabricate_collection_readOnlyChild_bufRoundTrip(GameTestHelper helper) {
        MapSerializationGameTest.autoFabricate_collection_readOnlyChild_bufRoundTrip(helper);
    }

    @GameTest
    public void autoFabricate_fallsBackToError_whenNoCtor(GameTestHelper helper) {
        MapSerializationGameTest.autoFabricate_fallsBackToError_whenNoCtor(helper);
    }
}
