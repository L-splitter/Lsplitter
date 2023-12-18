/**
 * Validates the network by checking its equivalence with its copies and checking its properties.
 *
 * @param network The network to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
static <N, E> void validateNetwork(Network<N, E> network) {
    validateNetworkEquivalence(network);
    validateNetworkProperties(network);
    validateEdges(network);
    validateNodes(network);
}
/**
 * Validates the network's equivalence with its copies.
 *
 * @param network The network to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNetworkEquivalence(Network<N, E> network) {
    assertStronglyEquivalent(network, Graphs.copyOf(network));
    assertStronglyEquivalent(network, ImmutableNetwork.copyOf(network));
}
/**
 * Validates the network's properties.
 *
 * @param network The network to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNetworkProperties(Network<N, E> network) {
    String networkString = network.toString();
    assertThat(networkString).contains("isDirected: " + network.isDirected());
    assertThat(networkString).contains("allowsParallelEdges: " + network.allowsParallelEdges());
    assertThat(networkString).contains("allowsSelfLoops: " + network.allowsSelfLoops());
}
/**
 * Validates the edges in the network.
 *
 * @param network The network to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateEdges(Network<N, E> network) {
    String networkString = network.toString();
    int edgeStart = networkString.indexOf("edges:");
    String edgeString = networkString.substring(edgeStart);
    for (E edge : sanityCheckSet(network.edges())) {
        validateEdge(network, edgeString, edge);
    }
}
/**
 * Validates a single edge in the network.
 *
 * @param network The network to validate.
 * @param edgeString The string representation of the edge.
 * @param edge The edge to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateEdge(Network<N, E> network, String edgeString, E edge) {
    assertThat(edgeString).contains(edge.toString());
    EndpointPair<N> endpointPair = network.incidentNodes(edge);
    N nodeU = endpointPair.nodeU();
    N nodeV = endpointPair.nodeV();
    validateEdgeIncidentNodes(network, edge, nodeU, nodeV);
}
/**
 * Validates the incident nodes of an edge in the network.
 *
 * @param network The network to validate.
 * @param edge The edge to validate.
 * @param nodeU The first incident node of the edge.
 * @param nodeV The second incident node of the edge.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateEdgeIncidentNodes(Network<N, E> network, E edge, N nodeU, N nodeV) {
    Graph<N> asGraph = network.asGraph();
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
        validateIncidentNode(network, edge, incidentNode);
    }
}
/**
 * Validates an incident node of an edge in the network.
 *
 * @param network The network to validate.
 * @param edge The edge to validate.
 * @param incidentNode The incident node to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateIncidentNode(Network<N, E> network, E edge, N incidentNode) {
    assertThat(network.nodes()).contains(incidentNode);
    for (E adjacentEdge : network.incidentEdges(incidentNode)) {
        assertTrue(
                edge.equals(adjacentEdge) || network.adjacentEdges(edge).contains(adjacentEdge));
    }
}
/**
 * Validates the nodes in the network.
 *
 * @param network The network to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNodes(Network<N, E> network) {
    String networkString = network.toString();
    int nodeStart = networkString.indexOf("nodes:");
    int edgeStart = networkString.indexOf("edges:");
    String nodeString = networkString.substring(nodeStart, edgeStart);
    for (N node : sanityCheckSet(network.nodes())) {
        validateNode(network, nodeString, node);
    }
}
/**
 * Validates a single node in the network.
 *
 * @param network The network to validate.
 * @param nodeString The string representation of the node.
 * @param node The node to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNode(Network<N, E> network, String nodeString, N node) {
    assertThat(nodeString).contains(node.toString());
    Graph<N> asGraph = network.asGraph();
    assertThat(network.adjacentNodes(node)).isEqualTo(asGraph.adjacentNodes(node));
    assertThat(network.predecessors(node)).isEqualTo(asGraph.predecessors(node));
    assertThat(network.successors(node)).isEqualTo(asGraph.successors(node));
    validateNodeDegrees(network, node);
    validateNodeConnections(network, node);
    validateNodeEdges(network, node);
}
/**
 * Validates the degrees of a node in the network.
 *
 * @param network The network to validate.
 * @param node The node to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNodeDegrees(Network<N, E> network, N node) {
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
}
/**
 * Validates the connections of a node in the network.
 *
 * @param network The network to validate.
 * @param node The node to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNodeConnections(Network<N, E> network, N node) {
    for (N otherNode : network.nodes()) {
        validateNodeConnection(network, node, otherNode);
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
}
/**
 * Validates the connection between two nodes in the network.
 *
 * @param network The network to validate.
 * @param node The first node.
 * @param otherNode The second node.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNodeConnection(Network<N, E> network, N node, N otherNode) {
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
    validateNodeConnectionProperties(network, node, otherNode, edgesConnecting);
}
/**
 * Validates the properties of the connection between two nodes in the network.
 *
 * @param network The network to validate.
 * @param node The first node.
 * @param otherNode The second node.
 * @param edgesConnecting The edges connecting the two nodes.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNodeConnectionProperties(Network<N, E> network, N node, N otherNode, Set<E> edgesConnecting) {
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
/**
 * Validates the edges of a node in the network.
 *
 * @param network The network to validate.
 * @param node The node to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateNodeEdges(Network<N, E> network, N node) {
    for (E incidentEdge : sanityCheckSet(network.incidentEdges(node))) {
        validateIncidentEdge(network, node, incidentEdge);
    }
    for (E inEdge : sanityCheckSet(network.inEdges(node))) {
        validateInEdge(network, node, inEdge);
    }
    for (E outEdge : sanityCheckSet(network.outEdges(node))) {
        validateOutEdge(network, node, outEdge);
    }
}
/**
 * Validates an incident edge of a node in the network.
 *
 * @param network The network to validate.
 * @param node The node to validate.
 * @param incidentEdge The incident edge to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateIncidentEdge(Network<N, E> network, N node, E incidentEdge) {
    assertTrue(
            network.inEdges(node).contains(incidentEdge)
                    || network.outEdges(node).contains(incidentEdge));
    assertThat(network.edges()).contains(incidentEdge);
    assertThat(network.incidentNodes(incidentEdge)).contains(node);
}
/**
 * Validates an in-edge of a node in the network.
 *
 * @param network The network to validate.
 * @param node The node to validate.
 * @param inEdge The in-edge to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateInEdge(Network<N, E> network, N node, E inEdge) {
    assertThat(network.incidentEdges(node)).contains(inEdge);
    assertThat(network.outEdges(network.incidentNodes(inEdge).adjacentNode(node)))
            .contains(inEdge);
    if (network.isDirected()) {
        assertThat(network.incidentNodes(inEdge).target()).isEqualTo(node);
    }
}
/**
 * Validates an out-edge of a node in the network.
 *
 * @param network The network to validate.
 * @param node The node to validate.
 * @param outEdge The out-edge to validate.
 * @param <N> The type of nodes in the network.
 * @param <E> The type of edges in the network.
 */
private static <N, E> void validateOutEdge(Network<N, E> network, N node, E outEdge) {
    assertThat(network.incidentEdges(node)).contains(outEdge);
    assertThat(network.inEdges(network.incidentNodes(outEdge).adjacentNode(node)))
            .contains(outEdge);
    if (network.isDirected()) {
        assertThat(network.incidentNodes(outEdge).source()).isEqualTo(node);
    }
}
