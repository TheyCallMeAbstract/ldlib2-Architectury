package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.test.gametest.resource.DirectFileResolutionGameTest;
import com.lowdragmc.lowdraglib2.test.gametest.resource.ResourcePathMigrationGameTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricResourceGameTests {

    @GameTest
    public void resolvesFileOutsideAnyProvider(GameTestHelper helper) {
        DirectFileResolutionGameTest.resolvesFileOutsideAnyProvider(helper);
    }

    @GameTest
    public void rejectsWrongTypeEnvelope(GameTestHelper helper) {
        DirectFileResolutionGameTest.rejectsWrongTypeEnvelope(helper);
    }

    @GameTest
    public void rejectsOutsideGameDir(GameTestHelper helper) {
        DirectFileResolutionGameTest.rejectsOutsideGameDir(helper);
    }

    @GameTest
    public void picksUpExternalEdits(GameTestHelper helper) {
        DirectFileResolutionGameTest.picksUpExternalEdits(helper);
    }

    @GameTest
    public void negativeCacheDoesNotBlockCreation(GameTestHelper helper) {
        DirectFileResolutionGameTest.negativeCacheDoesNotBlockCreation(helper);
    }

    @GameTest
    public void registeredProviderStillWins(GameTestHelper helper) {
        DirectFileResolutionGameTest.registeredProviderStillWins(helper);
    }

    @GameTest
    public void listAllResourcesUnaffected(GameTestHelper helper) {
        DirectFileResolutionGameTest.listAllResourcesUnaffected(helper);
    }

    @GameTest
    public void absoluteUnderGameDirCollapses(GameTestHelper helper) {
        ResourcePathMigrationGameTest.absoluteUnderGameDirCollapses(helper);
    }

    @GameTest
    public void foreignAbsoluteCollapses(GameTestHelper helper) {
        ResourcePathMigrationGameTest.foreignAbsoluteCollapses(helper);
    }

    @GameTest
    public void identityIsStable(GameTestHelper helper) {
        ResourcePathMigrationGameTest.identityIsStable(helper);
    }

    @GameTest
    public void outsideGameDirUnchanged(GameTestHelper helper) {
        ResourcePathMigrationGameTest.outsideGameDirUnchanged(helper);
    }

    @GameTest
    public void idempotent(GameTestHelper helper) {
        ResourcePathMigrationGameTest.idempotent(helper);
    }

    @GameTest
    public void resolveFileRoundTrip(GameTestHelper helper) {
        ResourcePathMigrationGameTest.resolveFileRoundTrip(helper);
    }

    @GameTest
    public void providerNbtRoundTrip(GameTestHelper helper) {
        ResourcePathMigrationGameTest.providerNbtRoundTrip(helper);
    }

    @GameTest
    public void toResourceLocationStillDerives(GameTestHelper helper) {
        ResourcePathMigrationGameTest.toResourceLocationStillDerives(helper);
    }

    @GameTest
    public void legacyCodecsStillDecode(GameTestHelper helper) {
        ResourcePathMigrationGameTest.legacyCodecsStillDecode(helper);
    }
}
