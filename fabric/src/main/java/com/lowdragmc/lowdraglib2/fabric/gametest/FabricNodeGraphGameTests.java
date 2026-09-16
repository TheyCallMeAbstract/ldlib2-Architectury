package com.lowdragmc.lowdraglib2.fabric.gametest;

import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphSerializationGameTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphHierarchySerializationGameTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphAnnotationRegistrationGameTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphCopyPasteGameTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphFuzzGameTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphRenameColorTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphSubgraphTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.ContextBlockTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphCommandPolicyTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphCrossTypeSubgraphTest;
import com.lowdragmc.lowdraglib2.test.gametest.nodegraph.GraphWireReroutePointTest;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class FabricNodeGraphGameTests {

    @GameTest
    public void graphSerializationRoundTrip(GameTestHelper helper) {
        GraphSerializationGameTest.graphSerializationRoundTrip(helper);
    }

    @GameTest
    public void emptyGraphSerialization(GameTestHelper helper) {
        GraphSerializationGameTest.emptyGraphSerialization(helper);
    }

    @GameTest
    public void portConstantsSerialization(GameTestHelper helper) {
        GraphSerializationGameTest.portConstantsSerialization(helper);
    }

    @GameTest
    public void typeHandleResolveFallsBackToClassForName(GameTestHelper helper) {
        GraphSerializationGameTest.typeHandleResolveFallsBackToClassForName(helper);
    }

    @GameTest
    public void variableInitializationModelRoundTrip(GameTestHelper helper) {
        GraphSerializationGameTest.variableInitializationModelRoundTrip(helper);
    }

    @GameTest
    public void constantNodeOwnerAndValuePreserved(GameTestHelper helper) {
        GraphSerializationGameTest.constantNodeOwnerAndValuePreserved(helper);
    }

    @GameTest
    public void optionDrivenPortCountSurvivesRoundTrip(GameTestHelper helper) {
        GraphSerializationGameTest.optionDrivenPortCountSurvivesRoundTrip(helper);
    }

    @GameTest
    public void portCustomCodecRoundTrip(GameTestHelper helper) {
        GraphSerializationGameTest.portCustomCodecRoundTrip(helper);
    }

    @GameTest
    public void portWithoutSerializationResetsToDefault(GameTestHelper helper) {
        GraphSerializationGameTest.portWithoutSerializationResetsToDefault(helper);
    }

    @GameTest
    public void portMissingAccessorSerializesGracefully(GameTestHelper helper) {
        GraphSerializationGameTest.portMissingAccessorSerializesGracefully(helper);
    }

    @GameTest
    public void portWithoutConfiguratorFlagsModel(GameTestHelper helper) {
        GraphSerializationGameTest.portWithoutConfiguratorFlagsModel(helper);
    }

    @GameTest
    public void evolutionCodecToNoCodec(GameTestHelper helper) {
        GraphSerializationGameTest.evolutionCodecToNoCodec(helper);
    }

    @GameTest
    public void evolutionCodecToWithoutSerialization(GameTestHelper helper) {
        GraphSerializationGameTest.evolutionCodecToWithoutSerialization(helper);
    }

    @GameTest
    public void evolutionWithoutSerializationToCodec(GameTestHelper helper) {
        GraphSerializationGameTest.evolutionWithoutSerializationToCodec(helper);
    }

    @GameTest
    public void evolutionAccessorToCodec(GameTestHelper helper) {
        GraphSerializationGameTest.evolutionAccessorToCodec(helper);
    }

    @GameTest
    public void evolutionCodecToDifferentCodec(GameTestHelper helper) {
        GraphSerializationGameTest.evolutionCodecToDifferentCodec(helper);
    }

    @GameTest
    public void evolutionCorruptValueTag(GameTestHelper helper) {
        GraphSerializationGameTest.evolutionCorruptValueTag(helper);
    }

    @GameTest
    public void codecPortSurvivesMultipleDefineNode(GameTestHelper helper) {
        GraphSerializationGameTest.codecPortSurvivesMultipleDefineNode(helper);
    }

    @GameTest
    public void portMissingAccessorWarnsOnce(GameTestHelper helper) {
        GraphSerializationGameTest.portMissingAccessorWarnsOnce(helper);
    }

    @GameTest
    public void backwardCompatLegacyNbt(GameTestHelper helper) {
        GraphSerializationGameTest.backwardCompatLegacyNbt(helper);
    }

    @GameTest
    public void wireRecoveryByPortId(GameTestHelper helper) {
        GraphSerializationGameTest.wireRecoveryByPortId(helper);
    }

    @GameTest
    public void orphanMissingPortRemovedOnLoad(GameTestHelper helper) {
        GraphSerializationGameTest.orphanMissingPortRemovedOnLoad(helper);
    }

    @GameTest
    public void deletingWireRemovesMissingPort(GameTestHelper helper) {
        GraphSerializationGameTest.deletingWireRemovesMissingPort(helper);
    }

    @GameTest
    public void variableIoReversalDropsWire(GameTestHelper helper) {
        GraphSerializationGameTest.variableIoReversalDropsWire(helper);
    }

    @GameTest
    public void missingPortReportedToGraphLogger(GameTestHelper helper) {
        GraphSerializationGameTest.missingPortReportedToGraphLogger(helper);
    }

    @GameTest
    public void removingMissingPortInvalidatesVisibleCache(GameTestHelper helper) {
        GraphSerializationGameTest.removingMissingPortInvalidatesVisibleCache(helper);
    }

    @GameTest
    public void incompatibleRetypeParksWireOnMissingPort(GameTestHelper helper) {
        GraphSerializationGameTest.incompatibleRetypeParksWireOnMissingPort(helper);
    }

    @GameTest
    public void variableSectionHierarchy(GameTestHelper helper) {
        GraphHierarchySerializationGameTest.variableSectionHierarchy(helper);
    }

    @GameTest
    public void nestedGroupHierarchy(GameTestHelper helper) {
        GraphHierarchySerializationGameTest.nestedGroupHierarchy(helper);
    }

    @GameTest
    public void doubleRoundTripHierarchy(GameTestHelper helper) {
        GraphHierarchySerializationGameTest.doubleRoundTripHierarchy(helper);
    }

    @GameTest
    public void nodeTypesAreRegistered(GameTestHelper helper) {
        GraphAnnotationRegistrationGameTest.nodeTypesAreRegistered(helper);
    }

    @GameTest
    public void portOrientationFollowsBuilder(GameTestHelper helper) {
        GraphAnnotationRegistrationGameTest.portOrientationFollowsBuilder(helper);
    }

    @GameTest
    public void unboundNodeIsInOtherGraphRegistry(GameTestHelper helper) {
        GraphAnnotationRegistrationGameTest.unboundNodeIsInOtherGraphRegistry(helper);
    }

    @GameTest
    public void copyPasteBasicNodes(GameTestHelper helper) {
        GraphCopyPasteGameTest.copyPasteBasicNodes(helper);
    }

    @GameTest
    public void copyPastePartialSelection(GameTestHelper helper) {
        GraphCopyPasteGameTest.copyPastePartialSelection(helper);
    }

    @GameTest
    public void copyPasteWithVariable(GameTestHelper helper) {
        GraphCopyPasteGameTest.copyPasteWithVariable(helper);
    }

    @GameTest
    public void copyPastePositionOffset(GameTestHelper helper) {
        GraphCopyPasteGameTest.copyPastePositionOffset(helper);
    }

    @GameTest
    public void duplicatePreservesConnections(GameTestHelper helper) {
        GraphCopyPasteGameTest.duplicatePreservesConnections(helper);
    }

    @GameTest
    public void graphFuzzCreateDeleteSerialize(GameTestHelper helper) {
        GraphFuzzGameTest.graphFuzzCreateDeleteSerialize(helper);
    }

    @GameTest
    public void graphFuzzUndoRedoIntegrity(GameTestHelper helper) {
        GraphFuzzGameTest.graphFuzzUndoRedoIntegrity(helper);
    }

    @GameTest
    public void abstractNodeModelColorStoragePersisted(GameTestHelper helper) {
        GraphRenameColorTest.abstractNodeModelColorStoragePersisted(helper);
    }

    @GameTest
    public void renamableJudgmentRespectsCapabilityAndInterface(GameTestHelper helper) {
        GraphRenameColorTest.renamableJudgmentRespectsCapabilityAndInterface(helper);
    }

    @GameTest
    public void subgraphNodeTitleFollowsName(GameTestHelper helper) {
        GraphRenameColorTest.subgraphNodeTitleFollowsName(helper);
    }

    @GameTest
    public void colorableSettersRoundTrip(GameTestHelper helper) {
        GraphRenameColorTest.colorableSettersRoundTrip(helper);
    }

    @GameTest
    public void wireIsNeitherRenamableNorColorable(GameTestHelper helper) {
        GraphRenameColorTest.wireIsNeitherRenamableNorColorable(helper);
    }

    @GameTest
    public void resetColorRestoresDefault(GameTestHelper helper) {
        GraphRenameColorTest.resetColorRestoresDefault(helper);
    }

    @GameTest
    public void localSubgraphSerializationRoundTrip(GameTestHelper helper) {
        GraphSubgraphTest.localSubgraphSerializationRoundTrip(helper);
    }

    @GameTest
    public void externalSubgraphPortCacheSurvives(GameTestHelper helper) {
        GraphSubgraphTest.externalSubgraphPortCacheSurvives(helper);
    }

    @GameTest
    public void portsFollowVariableModifiers(GameTestHelper helper) {
        GraphSubgraphTest.portsFollowVariableModifiers(helper);
    }

    @GameTest
    public void portsTrackVariableTypeChanges(GameTestHelper helper) {
        GraphSubgraphTest.portsTrackVariableTypeChanges(helper);
    }

    @GameTest
    public void deletingExposedVariableRemovesOuterPort(GameTestHelper helper) {
        GraphSubgraphTest.deletingExposedVariableRemovesOuterPort(helper);
    }

    @GameTest
    public void nestedLocalSubgraphSerialization(GameTestHelper helper) {
        GraphSubgraphTest.nestedLocalSubgraphSerialization(helper);
    }

    @GameTest
    public void externalSaveBroadcastReDefinesPorts(GameTestHelper helper) {
        GraphSubgraphTest.externalSaveBroadcastReDefinesPorts(helper);
    }

    @GameTest
    public void extractSelectionToLocalSubgraph(GameTestHelper helper) {
        GraphSubgraphTest.extractSelectionToLocalSubgraph(helper);
    }

    @GameTest
    public void extractAcceptsPlacematAndStickyNote(GameTestHelper helper) {
        GraphSubgraphTest.extractAcceptsPlacematAndStickyNote(helper);
    }

    @GameTest
    public void extractRejectsPlacematWithExternalNode(GameTestHelper helper) {
        GraphSubgraphTest.extractRejectsPlacematWithExternalNode(helper);
    }

    @GameTest
    public void extractTransplantsLocalSubgraphReference(GameTestHelper helper) {
        GraphSubgraphTest.extractTransplantsLocalSubgraphReference(helper);
    }

    @GameTest
    public void subgraphRegistryListenerReceivesBroadcast(GameTestHelper helper) {
        GraphSubgraphTest.subgraphRegistryListenerReceivesBroadcast(helper);
    }

    @GameTest
    public void resolverSaveDefaultIsNoOp(GameTestHelper helper) {
        GraphSubgraphTest.resolverSaveDefaultIsNoOp(helper);
    }

    @GameTest
    public void copyPasteLocalSubgraphInSameGraph(GameTestHelper helper) {
        GraphSubgraphTest.copyPasteLocalSubgraphInSameGraph(helper);
    }

    @GameTest
    public void copyPasteLocalSubgraphCrossGraph(GameTestHelper helper) {
        GraphSubgraphTest.copyPasteLocalSubgraphCrossGraph(helper);
    }

    @GameTest
    public void preSubgraphNbtIsForwardCompatible(GameTestHelper helper) {
        GraphSubgraphTest.preSubgraphNbtIsForwardCompatible(helper);
    }

    @GameTest
    public void graphCanDisableSubgraphVariablePorts(GameTestHelper helper) {
        GraphSubgraphTest.graphCanDisableSubgraphVariablePorts(helper);
    }

    @GameTest
    public void graphCanRestrictSubgraphVariablePortDirection(GameTestHelper helper) {
        GraphSubgraphTest.graphCanRestrictSubgraphVariablePortDirection(helper);
    }

    @GameTest
    public void contextBlockBasicOperations(GameTestHelper helper) {
        ContextBlockTest.contextBlockBasicOperations(helper);
    }

    @GameTest
    public void incompatibleBlockRejected(GameTestHelper helper) {
        ContextBlockTest.incompatibleBlockRejected(helper);
    }

    @GameTest
    public void contextBlockSerializationRoundTrip(GameTestHelper helper) {
        ContextBlockTest.contextBlockSerializationRoundTrip(helper);
    }

    @GameTest
    public void contextDeletionCascadesBlocks(GameTestHelper helper) {
        ContextBlockTest.contextDeletionCascadesBlocks(helper);
    }

    @GameTest
    public void contextWithBlocksCopyPaste(GameTestHelper helper) {
        ContextBlockTest.contextWithBlocksCopyPaste(helper);
    }

    @GameTest
    public void canExecuteCommandDelegatesToGraph(GameTestHelper helper) {
        GraphCommandPolicyTest.canExecuteCommandDelegatesToGraph(helper);
    }

    @GameTest
    public void onCommandExecutedDelegatesToGraph(GameTestHelper helper) {
        GraphCommandPolicyTest.onCommandExecutedDelegatesToGraph(helper);
    }

    @GameTest
    public void defaultsArePermissive(GameTestHelper helper) {
        GraphCommandPolicyTest.defaultsArePermissive(helper);
    }

    @GameTest
    public void foreignLocalSubgraphRoundTrip(GameTestHelper helper) {
        GraphCrossTypeSubgraphTest.foreignLocalSubgraphRoundTrip(helper);
    }

    @GameTest
    public void compatibilityGating(GameTestHelper helper) {
        GraphCrossTypeSubgraphTest.compatibilityGating(helper);
    }

    @GameTest
    public void legacyLocalSubgraphWithoutGraphClass(GameTestHelper helper) {
        GraphCrossTypeSubgraphTest.legacyLocalSubgraphWithoutGraphClass(helper);
    }

    @GameTest
    public void reroutePointsDoNotChangeConnectionApi(GameTestHelper helper) {
        GraphWireReroutePointTest.reroutePointsDoNotChangeConnectionApi(helper);
    }

    @GameTest
    public void fanOutFromReroutePointSharesTheTrunk(GameTestHelper helper) {
        GraphWireReroutePointTest.fanOutFromReroutePointSharesTheTrunk(helper);
    }

    @GameTest
    public void bendingASharedTrunkBendsEveryBranch(GameTestHelper helper) {
        GraphWireReroutePointTest.bendingASharedTrunkBendsEveryBranch(helper);
    }

    @GameTest
    public void deletingASharedReroutePointKeepsEveryBranch(GameTestHelper helper) {
        GraphWireReroutePointTest.deletingASharedReroutePointKeepsEveryBranch(helper);
    }

    @GameTest
    public void reroutePointOutlivesOneBranchButNotAll(GameTestHelper helper) {
        GraphWireReroutePointTest.reroutePointOutlivesOneBranchButNotAll(helper);
    }

    @GameTest
    public void fanOutSurvivesSerialization(GameTestHelper helper) {
        GraphWireReroutePointTest.fanOutSurvivesSerialization(helper);
    }

    @GameTest
    public void fanOutSurvivesCopyPaste(GameTestHelper helper) {
        GraphWireReroutePointTest.fanOutSurvivesCopyPaste(helper);
    }

    @GameTest
    public void reSourcingAReroutePointMovesEveryBranch(GameTestHelper helper) {
        GraphWireReroutePointTest.reSourcingAReroutePointMovesEveryBranch(helper);
    }

    @GameTest
    public void reSourcingDetachesTheOldUpstream(GameTestHelper helper) {
        GraphWireReroutePointTest.reSourcingDetachesTheOldUpstream(helper);
    }

    @GameTest
    public void reroutePointCentreRoundTrips(GameTestHelper helper) {
        GraphWireReroutePointTest.reroutePointCentreRoundTrips(helper);
    }
}
