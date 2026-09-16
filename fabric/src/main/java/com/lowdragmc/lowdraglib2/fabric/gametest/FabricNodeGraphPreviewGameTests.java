package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphNodePreviewTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricNodeGraphPreviewGameTests {

    @GameTest
    public void previewAutoCreatedAndDependent(GameTestHelper helper) {
        GraphNodePreviewTest.previewAutoCreatedAndDependent(helper);
    }

    @GameTest
    public void orphanSpawnSkipsPreview(GameTestHelper helper) {
        GraphNodePreviewTest.orphanSpawnSkipsPreview(helper);
    }

    @GameTest
    public void duplicateCopiesExpandedState(GameTestHelper helper) {
        GraphNodePreviewTest.duplicateCopiesExpandedState(helper);
    }

    @GameTest
    public void expandedStatePersistsRoundTrip(GameTestHelper helper) {
        GraphNodePreviewTest.expandedStatePersistsRoundTrip(helper);
    }

    @GameTest
    public void previewDefaultExpandedApi(GameTestHelper helper) {
        GraphNodePreviewTest.previewDefaultExpandedApi(helper);
    }

    @GameTest
    public void previewExpandedStateOverridesDefaultOnLoad(GameTestHelper helper) {
        GraphNodePreviewTest.previewExpandedStateOverridesDefaultOnLoad(helper);
    }

    @GameTest
    public void duplicatePreviewStateOverridesDefault(GameTestHelper helper) {
        GraphNodePreviewTest.duplicatePreviewStateOverridesDefault(helper);
    }

    @GameTest
    public void pasteRecreatesPreview(GameTestHelper helper) {
        GraphNodePreviewTest.pasteRecreatesPreview(helper);
    }
}
