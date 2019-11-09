package com.zjzcn.test.graph;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class DirectedGraph<NodeId, NodeInfo, EdgeInfo> implements Iterable<NodeId> {

    private volatile Map<NodeId, NodeInfo> nodes = new HashMap<>();
    private volatile MultiKeyMap<NodeId, EdgeInfo> edges = new MultiKeyMap<>();

    private String name;

    public DirectedGraph() {
    }

    public DirectedGraph(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public DirectedGraph<NodeId, NodeInfo, EdgeInfo> addNode(NodeId nodeId, NodeInfo node) {
        Objects.requireNonNull(nodeId);
        nodes.put(nodeId, node);
        return this;
    }

    public DirectedGraph<NodeId, NodeInfo, EdgeInfo> addEdge(NodeId sourceId, NodeId targetId) {
        addEdge(sourceId, targetId, null);
        return this;
    }

    public DirectedGraph<NodeId, NodeInfo, EdgeInfo> addEdge(NodeId sourceId, NodeId targetId, EdgeInfo edge) {
        Objects.requireNonNull(sourceId);
        Objects.requireNonNull(targetId);
        if (!this.containsNode(sourceId)) {
            throw new IllegalArgumentException(String.format("No such node(%s) in graph.", sourceId));
        }
        if (!this.containsNode(targetId)) {
            throw new IllegalArgumentException(String.format("No such node(%s) in graph.", targetId));
        }
        if (hasCycleForAddEdge(sourceId, targetId)) {
            throw new IllegalArgumentException(String.format("Edge(%s -> %s) has a cycle.", sourceId, targetId));
        }
        edges.put(sourceId, targetId, edge);
        return this;
    }

    public List<NodeId> getNodes() {
        return new ArrayList<>(nodes.keySet());
    }

    public List<Pair<NodeId, NodeId>> getEdges() {
        List<Pair<NodeId, NodeId>> edgeIds = new ArrayList<>();
        for (MultiKey<? extends NodeId> key : edges.keySet()) {
            NodeId sourceId = key.getKey(0);
            NodeId targetId = key.getKey(1);
            edgeIds.add(new ImmutablePair<>(sourceId, targetId));
        }
        return edgeIds;
    }

    public boolean containsNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        return nodes.containsKey(nodeId);
    }

    public boolean containsEdge(NodeId sourceId, NodeId targetId) {
        Objects.requireNonNull(sourceId);
        Objects.requireNonNull(targetId);
        return edges.containsKey(sourceId, targetId);
    }


    public NodeInfo getNodeInfo(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        return nodes.get(nodeId);
    }


    public EdgeInfo getEdgeInfo(NodeId sourceId, NodeId targetId) {
        Objects.requireNonNull(sourceId);
        Objects.requireNonNull(targetId);
        return edges.get(sourceId, targetId);
    }


    public int getNodeCount() {
        return nodes.size();
    }


    public int getEdgeCount() {
        return edges.size();
    }

    public boolean isStartNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        return nodeId.equals(getStartNode());
    }

    public boolean isEndNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        return nodeId.equals(getEndNode());
    }

    public boolean isForkNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        return getNextNodes(nodeId).size() > 1;
    }

    public boolean isJoinNode(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        return getPrevNodes(nodeId).size() > 1;
    }

    public int toNextJoinNodeStep(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        int step = 0;
        List<NodeId> nodeIds;
        do {
            nodeIds = getNextNodes(nodeId);
            if (nodeIds.size() == 1) {
                step++;
            }
        } while (nodeIds.size() == 1);
        return step;
    }

    public NodeId getStartNode() {
        List<NodeId> nodeIds = new ArrayList<>();
        for (NodeId nodeId : nodes.keySet()) {
            if (getPrevNodes(nodeId).isEmpty()) {
                nodeIds.add(nodeId);
            }
        }
        if (nodeIds.isEmpty()) {
            return null;
        }
        if (nodeIds.size() != 1) {
            throw new IllegalStateException(String.format("StartNode size(%s) only one in graph.", nodeIds.size()));
        }
        return nodeIds.get(0);
    }


    public NodeId getEndNode() {
        List<NodeId> nodeIds = new ArrayList<>();
        for (NodeId nodeId : nodes.keySet()) {
            if (getNextNodes(nodeId).isEmpty()) {
                nodeIds.add(nodeId);
            }
        }
        if (nodeIds.isEmpty()) {
            return null;
        }
        if (nodeIds.size() > 1) {
            throw new IllegalStateException(String.format("EndNode size(%s) only one in graph.", nodeIds.size()));
        }
        return nodeIds.get(0);
    }


    public List<NodeId> getPrevNodes(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        List<NodeId> nodeIds = new ArrayList<>();
        for (MultiKey<? extends NodeId> key : edges.keySet()) {
            NodeId targetId = key.getKey(1);
            if (targetId.equals(nodeId)) {
                nodeIds.add(key.getKey(0));
            }
        }
        return nodeIds;
    }

    public List<NodeId> getNextNodes(NodeId nodeId) {
        Objects.requireNonNull(nodeId);
        List<NodeId> nodeIds = new ArrayList<>();
        for (MultiKey<? extends NodeId> key : edges.keySet()) {
            NodeId sourceId = key.getKey(0);
            if (sourceId.equals(nodeId)) {
                nodeIds.add(key.getKey(1));
            }
        }
        return nodeIds;
    }


    private boolean hasCycleForAddEdge(NodeId sourceId, NodeId targetId) {
        Queue<NodeId> queue = new LinkedList<>();
        queue.add(targetId);

        int nodeCount = this.getNodeCount();
        while (!queue.isEmpty() && (--nodeCount > 0)) {
            NodeId nodeId = queue.poll();

            for (NodeId nextId : getNextNodes(nodeId)) {
                if (nextId.equals(sourceId)) {
                    return true;
                }
                queue.add(nextId);
            }
        }

        return false;
    }

    @Override
    public Iterator<NodeId> iterator() {
        return new GraphItr();
    }

    private class GraphItr implements Iterator<NodeId> {

        private Queue<NodeId> queue = new LinkedList<>();
        private Map<NodeId, Integer> notZeroIndegreeNodeMap = new HashMap<>();

        GraphItr() {
            for (NodeId nodeId : nodes.keySet()) {
                int inDegree = getPrevNodes(nodeId).size();
                if (inDegree == 0) {
                    queue.add(nodeId);
                } else {
                    notZeroIndegreeNodeMap.put(nodeId, inDegree);
                }
            }
        }

        public boolean hasNext() {
            return !queue.isEmpty();
        }

        public NodeId next() {
            NodeId nodeId = queue.poll();
            List<NodeId> nextNodes = getNextNodes(nodeId);
            for (NodeId nextId : nextNodes) {
                Integer inDegree = notZeroIndegreeNodeMap.get(nextId);
                inDegree--;
                if (inDegree == 0) {
                    queue.offer(nextId);
                    notZeroIndegreeNodeMap.remove(nextId);
                } else {
                    notZeroIndegreeNodeMap.put(nextId, inDegree);
                }
            }
            return nodeId;
        }
    }

    public static void main(String[] args) {
        DirectedGraph<String, String, String> graph = new DirectedGraph<>();
        graph.addNode("AAAA.A", "startNode");
        graph.addNode("AAAB.A", "node1");
        graph.addNode("AAAC.A", "node1");
        graph.addNode("ZZZZ.Z", "endNode");

        graph.addEdge("AAAA.A", "ZZZZ.Z");
        graph.addEdge("AAAA.A", "AAAB.A");
        graph.addEdge("AAAA.A", "AAAC.A");
        graph.addEdge("AAAB.A", "ZZZZ.Z");
        graph.addEdge("AAAC.A", "ZZZZ.Z");

        // add cycle
        graph.addEdge("AAAB.A", "AAAA.A");

        System.out.println(graph.getStartNode());
        System.out.println(graph.getEndNode());
        System.out.println(graph.getPrevNodes("AAAA.A"));
        System.out.println(graph.getPrevNodes("ZZZZ.Z"));
        System.out.println(graph.getNextNodes("AAAA.A"));
        System.out.println(graph.getNextNodes("ZZZZ.Z"));
    }
}
