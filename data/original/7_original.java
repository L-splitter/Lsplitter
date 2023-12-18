public void testAllocationFilteringPreventsShardMove() throws Exception {
    logger.info("--> starting 2 nodes");
    internalCluster().startNodes(2);

    prepareIndex(1, 0);

    logger.info("--> setting up allocation filtering to prevent allocation to both nodes");
    updateIndexSettings(Settings.builder().put("index.routing.allocation.include._name", "non_existent_node"), "idx");

    boolean includeYesDecisions = randomBoolean();
    boolean includeDiskInfo = randomBoolean();
    ClusterAllocationExplanation explanation = runExplain(true, includeYesDecisions, includeDiskInfo);

    ShardId shardId = explanation.getShard();
    boolean isPrimary = explanation.isPrimary();
    ShardRoutingState shardRoutingState = explanation.getShardState();
    DiscoveryNode currentNode = explanation.getCurrentNode();
    UnassignedInfo unassignedInfo = explanation.getUnassignedInfo();
    ClusterInfo clusterInfo = explanation.getClusterInfo();
    AllocateUnassignedDecision allocateDecision = explanation.getShardAllocationDecision().getAllocateDecision();
    MoveDecision moveDecision = explanation.getShardAllocationDecision().getMoveDecision();

    // verify shard info
    assertEquals("idx", shardId.getIndexName());
    assertEquals(0, shardId.getId());
    assertTrue(isPrimary);

    // verify current node info
    assertEquals(ShardRoutingState.STARTED, shardRoutingState);
    assertNotNull(currentNode);

    // verify unassigned info
    assertNull(unassignedInfo);

    // verify cluster info
    verifyClusterInfo(clusterInfo, includeDiskInfo, 2);

    // verify decision object
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
    // verifying can remain decision object
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
    // verify node decisions
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

    // verify JSON output
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