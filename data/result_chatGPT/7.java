/**
 * Starts the nodes for the test.
 */
private void startNodes() {
    logger.info("--> starting 2 nodes");
    internalCluster().startNodes(2);
}
/**
 * Prepares the index for the test.
 */
private void prepareTestIndex() {
    prepareIndex(1, 0);
}
/**
 * Sets up allocation filtering for the test.
 */
private void setupAllocationFiltering() {
    logger.info("--> setting up allocation filtering to prevent allocation to both nodes");
    updateIndexSettings(Settings.builder().put("index.routing.allocation.include._name", "non_existent_node"), "idx");
}
/**
 * Runs the explanation for the test.
 */
private ClusterAllocationExplanation runTestExplanation() {
    boolean includeYesDecisions = randomBoolean();
    boolean includeDiskInfo = randomBoolean();
    return runExplain(true, includeYesDecisions, includeDiskInfo);
}
/**
 * Verifies the shard info for the test.
 */
private void verifyShardInfo(ShardId shardId, boolean isPrimary) {
    assertEquals("idx", shardId.getIndexName());
    assertEquals(0, shardId.getId());
    assertTrue(isPrimary);
}
/**
 * Verifies the current node info for the test.
 */
private void verifyCurrentNodeInfo(ShardRoutingState shardRoutingState, DiscoveryNode currentNode) {
    assertEquals(ShardRoutingState.STARTED, shardRoutingState);
    assertNotNull(currentNode);
}
/**
 * Verifies the unassigned info for the test.
 */
private void verifyUnassignedInfo(UnassignedInfo unassignedInfo) {
    assertNull(unassignedInfo);
}
/**
 * Verifies the cluster info for the test.
 */
private void verifyClusterInfo(ClusterInfo clusterInfo, boolean includeDiskInfo) {
    verifyClusterInfo(clusterInfo, includeDiskInfo, 2);
}
/**
 * Verifies the decision object for the test.
 */
private void verifyDecisionObject(AllocateUnassignedDecision allocateDecision, MoveDecision moveDecision) {
    assertFalse(allocateDecision.isDecisionTaken());
    assertTrue(moveDecision.isDecisionTaken());
    assertEquals(AllocationDecision.NO, moveDecision.getAllocationDecision());
    assertEquals(Explanations.Move.NO, moveDecision.getExplanation());
    assertFalse(moveDecision.canRemain());
    assertFalse(moveDecision.forceMove());
    assertFalse(moveDecision.canRebalanceCluster());
    assertNull(moveDecision.getClusterRebalanceDecision());
    assertNull(moveDecision.getTargetNode());
    assertEquals(0, moveDecision.getCurrentNodeRanking());
}
/**
 * Verifies the can remain decision object for the test.
 */
private void verifyCanRemainDecisionObject(MoveDecision moveDecision) {
    assertNotNull(moveDecision.getCanRemainDecision());
    assertEquals(Decision.Type.NO, moveDecision.getCanRemainDecision().type());
    for (Decision d : moveDecision.getCanRemainDecision().getDecisions()) {
        if (d.label().equals("filter")) {
            assertEquals(Decision.Type.NO, d.type());
            assertEquals(
                "node does not match index setting [index.routing.allocation.include] filters [_name:\"non_existent_node\"]",
                d.getExplanation()
            );
        } else {
            assertEquals(Decision.Type.YES, d.type());
            assertNotNull(d.getExplanation());
        }
    }
}
/**
 * Verifies the node decisions for the test.
 */
private void verifyNodeDecisions(MoveDecision moveDecision, boolean includeYesDecisions) {
    assertEquals(1, moveDecision.getNodeDecisions().size());
    NodeAllocationResult result = moveDecision.getNodeDecisions().get(0);
    assertNotNull(result.getNode());
    assertEquals(1, result.getWeightRanking());
    assertEquals(AllocationDecision.NO, result.getNodeDecision());
    if (includeYesDecisions) {
        assertThat(result.getCanAllocateDecision().getDecisions().size(), greaterThan(1));
    } else {
        assertEquals(1, result.getCanAllocateDecision().getDecisions().size());
    }
    for (Decision d : result.getCanAllocateDecision().getDecisions()) {
        if (d.label().equals("filter")) {
            assertEquals(Decision.Type.NO, d.type());
            assertEquals(
                "node does not match index setting [index.routing.allocation.include] filters [_name:\"non_existent_node\"]",
                d.getExplanation()
            );
        } else {
            assertEquals(Decision.Type.YES, d.type());
            assertNotNull(d.getExplanation());
        }
    }
}
/**
 * Verifies the JSON output for the test.
 */
private void verifyJsonOutput(ClusterAllocationExplanation explanation) throws IOException {
    try (XContentParser parser = getParser(explanation)) {
        verifyShardInfo(parser, true, includeDiskInfo, ShardRoutingState.STARTED);
        parser.nextToken();
        assertEquals("can_remain_on_current_node", parser.currentName());
        parser.nextToken();
        assertEquals(AllocationDecision.NO.toString(), parser.text());
        parser.nextToken();
        assertEquals("can_remain_decisions", parser.currentName());
        verifyDeciders(parser, AllocationDecision.NO);
        parser.nextToken();
        assertEquals("can_move_to_other_node", parser.currentName());
        parser.nextToken();
        assertEquals(AllocationDecision.NO.toString(), parser.text());
        parser.nextToken();
        assertEquals("move_explanation", parser.currentName());
        parser.nextToken();
        assertEquals(Explanations.Move.NO, parser.text());
        verifyNodeDecisions(parser, allNodeDecisions(AllocationDecision.NO, true), includeYesDecisions, false);
        assertEquals(Token.END_OBJECT, parser.nextToken());
    }
}
public void testAllocationFilteringPreventsShardMove() throws Exception {
    startNodes();
    prepareTestIndex();
    setupAllocationFiltering();
    ClusterAllocationExplanation explanation = runTestExplanation();
    ShardId shardId = explanation.getShard();
    boolean isPrimary = explanation.isPrimary();
    ShardRoutingState shardRoutingState = explanation.getShardState();
    DiscoveryNode currentNode = explanation.getCurrentNode();
    UnassignedInfo unassignedInfo = explanation.getUnassignedInfo();
    ClusterInfo clusterInfo = explanation.getClusterInfo();
    AllocateUnassignedDecision allocateDecision = explanation.getShardAllocationDecision().getAllocateDecision();
    MoveDecision moveDecision = explanation.getShardAllocationDecision().getMoveDecision();
    verifyShardInfo(shardId, isPrimary);
    verifyCurrentNodeInfo(shardRoutingState, currentNode);
    verifyUnassignedInfo(unassignedInfo);
    verifyClusterInfo(clusterInfo, includeDiskInfo);
    verifyDecisionObject(allocateDecision, moveDecision);
    verifyCanRemainDecisionObject(moveDecision);
    verifyNodeDecisions(moveDecision, includeYesDecisions);
    verifyJsonOutput(explanation);
}
