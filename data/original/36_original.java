static <N, E> void validateNetwork(Network<N, E> network) {
  assertStronglyEquivalent(network, Graphs.copyOf(network));
  assertStronglyEquivalent(network, ImmutableNetwork.copyOf(network));

  String networkString = network.toString();
  assertThat(networkString).contains("isDirected: " + network.isDirected());
  assertThat(networkString).contains("allowsParallelEdges: " + network.allowsParallelEdges());
  assertThat(networkString).contains("allowsSelfLoops: " + network.allowsSelfLoops());

  int nodeStart = networkString.indexOf("nodes:");
  int edgeStart = networkString.indexOf("edges:");
  String nodeString = networkString.substring(nodeStart, edgeStart);
  String edgeString = networkString.substring(edgeStart);

  Graph<N> asGraph = network.asGraph();
  AbstractGraphTest.validateGraph(asGraph);
  assertThat(network.nodes()).isEqualTo(asGraph.nodes());
  assertThat(network.edges().size()).isAtLeast(asGraph.edges().size());
  assertThat(network.nodeOrder()).isEqualTo(asGraph.nodeOrder());
  assertThat(network.isDirected()).isEqualTo(asGraph.isDirected());
  assertThat(network.allowsSelfLoops()).isEqualTo(asGraph.allowsSelfLoops());

  for (E edge : sanityCheckSet(network.edges())) {
    // TODO(b/27817069): Consider verifying the edge's incident nodes in the string.
    assertThat(edgeString).contains(edge.toString());

    EndpointPair<N> endpointPair = network.incidentNodes(edge);
    N nodeU = endpointPair.nodeU();
    N nodeV = endpointPair.nodeV();
    assertThat(asGraph.edges()).contains(EndpointPair.of(network, nodeU, nodeV));
    assertThat(network.edgesConnecting(nodeU, nodeV)).contains(edge);
    assertThat(network.successors(nodeU)).contains(nodeV);
    assertThat(network.adjacentNodes(nodeU)).contains(nodeV);
    assertThat(network.outEdges(nodeU)).contains(edge);
    assertThat(network.incidentEdges(nodeU)).contains(edge);
    assertThat(network.predecessors(nodeV)).contains(nodeU);
    assertThat(network.adjacentNodes(nodeV)).contains(nodeU);
    assertThat(network.inEdges(nodeV)).contains(edge);
    assertThat(network.incidentEdges(nodeV)).contains(edge);

    for (N incidentNode : network.incidentNodes(edge)) {
      assertThat(network.nodes()).contains(incidentNode);
      for (E adjacentEdge : network.incidentEdges(incidentNode)) {
        assertTrue(
            edge.equals(adjacentEdge) || network.adjacentEdges(edge).contains(adjacentEdge));
      }
    }
  }

  for (N node : sanityCheckSet(network.nodes())) {
    assertThat(nodeString).contains(node.toString());

    assertThat(network.adjacentNodes(node)).isEqualTo(asGraph.adjacentNodes(node));
    assertThat(network.predecessors(node)).isEqualTo(asGraph.predecessors(node));
    assertThat(network.successors(node)).isEqualTo(asGraph.successors(node));

    int selfLoopCount = network.edgesConnecting(node, node).size();
    assertThat(network.incidentEdges(node).size() + selfLoopCount)
        .isEqualTo(network.degree(node));

    if (network.isDirected()) {
      assertThat(network.incidentEdges(node).size() + selfLoopCount)
          .isEqualTo(network.inDegree(node) + network.outDegree(node));
      assertThat(network.inEdges(node)).hasSize(network.inDegree(node));
      assertThat(network.outEdges(node)).hasSize(network.outDegree(node));
    } else {
      assertThat(network.predecessors(node)).isEqualTo(network.adjacentNodes(node));
      assertThat(network.successors(node)).isEqualTo(network.adjacentNodes(node));
      assertThat(network.inEdges(node)).isEqualTo(network.incidentEdges(node));
      assertThat(network.outEdges(node)).isEqualTo(network.incidentEdges(node));
      assertThat(network.inDegree(node)).isEqualTo(network.degree(node));
      assertThat(network.outDegree(node)).isEqualTo(network.degree(node));
    }

    for (N otherNode : network.nodes()) {
      Set<E> edgesConnecting = sanityCheckSet(network.edgesConnecting(node, otherNode));
      switch (edgesConnecting.size()) {
        case 0:
          assertThat(network.edgeConnectingOrNull(node, otherNode)).isNull();
          assertThat(network.edgeConnecting(node, otherNode).isPresent()).isFalse();
          assertThat(network.hasEdgeConnecting(node, otherNode)).isFalse();
          break;
        case 1:
          E edge = edgesConnecting.iterator().next();
          assertThat(network.edgeConnectingOrNull(node, otherNode)).isEqualTo(edge);
          assertThat(network.edgeConnecting(node, otherNode).get()).isEqualTo(edge);
          assertThat(network.hasEdgeConnecting(node, otherNode)).isTrue();
          break;
        default:
          assertThat(network.hasEdgeConnecting(node, otherNode)).isTrue();
          try {
            network.edgeConnectingOrNull(node, otherNode);
            fail();
          } catch (IllegalArgumentException expected) {
          }
          try {
            network.edgeConnecting(node, otherNode);
            fail();
          } catch (IllegalArgumentException expected) {
          }
      }

      boolean isSelfLoop = node.equals(otherNode);
      boolean connected = !edgesConnecting.isEmpty();
      if (network.isDirected() || !isSelfLoop) {
        assertThat(edgesConnecting)
            .isEqualTo(Sets.intersection(network.outEdges(node), network.inEdges(otherNode)));
      }
      if (!network.allowsParallelEdges()) {
        assertThat(edgesConnecting.size()).isAtMost(1);
      }
      if (!network.allowsSelfLoops() && isSelfLoop) {
        assertThat(connected).isFalse();
      }

      assertThat(network.successors(node).contains(otherNode)).isEqualTo(connected);
      assertThat(network.predecessors(otherNode).contains(node)).isEqualTo(connected);
      for (E edge : edgesConnecting) {
        assertThat(network.incidentNodes(edge))
            .isEqualTo(EndpointPair.of(network, node, otherNode));
        assertThat(network.outEdges(node)).contains(edge);
        assertThat(network.inEdges(otherNode)).contains(edge);
      }
    }

    for (N adjacentNode : sanityCheckSet(network.adjacentNodes(node))) {
      assertTrue(
          network.predecessors(node).contains(adjacentNode)
              || network.successors(node).contains(adjacentNode));
      assertTrue(
          !network.edgesConnecting(node, adjacentNode).isEmpty()
              || !network.edgesConnecting(adjacentNode, node).isEmpty());
    }

    for (N predecessor : sanityCheckSet(network.predecessors(node))) {
      assertThat(network.successors(predecessor)).contains(node);
      assertThat(network.edgesConnecting(predecessor, node)).isNotEmpty();
    }

    for (N successor : sanityCheckSet(network.successors(node))) {
      assertThat(network.predecessors(successor)).contains(node);
      assertThat(network.edgesConnecting(node, successor)).isNotEmpty();
    }

    for (E incidentEdge : sanityCheckSet(network.incidentEdges(node))) {
      assertTrue(
          network.inEdges(node).contains(incidentEdge)
              || network.outEdges(node).contains(incidentEdge));
      assertThat(network.edges()).contains(incidentEdge);
      assertThat(network.incidentNodes(incidentEdge)).contains(node);
    }

    for (E inEdge : sanityCheckSet(network.inEdges(node))) {
      assertThat(network.incidentEdges(node)).contains(inEdge);
      assertThat(network.outEdges(network.incidentNodes(inEdge).adjacentNode(node)))
          .contains(inEdge);
      if (network.isDirected()) {
        assertThat(network.incidentNodes(inEdge).target()).isEqualTo(node);
      }
    }

    for (E outEdge : sanityCheckSet(network.outEdges(node))) {
      assertThat(network.incidentEdges(node)).contains(outEdge);
      assertThat(network.inEdges(network.incidentNodes(outEdge).adjacentNode(node)))
          .contains(outEdge);
      if (network.isDirected()) {
        assertThat(network.incidentNodes(outEdge).source()).isEqualTo(node);
      }
    }
  }
}