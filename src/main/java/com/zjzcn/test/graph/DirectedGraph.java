package com.zjzcn.test.graph;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class DirectedGraph<Node, NodeInfo> implements Iterable<Node> {

    private volatile Map<Node, NodeInfo> nodeMap = new HashMap<>();
    private volatile MultiKeyMap<Node, Void> edgeMap = new MultiKeyMap<>();

    private String name;

    public DirectedGraph() {
    }

    public DirectedGraph(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void addNode(Node node, NodeInfo nodeInfo) {
        Objects.requireNonNull(node);
        nodeMap.put(node, nodeInfo);
    }

    public void addEdge(Node sourceNode, Node targetNode) {
        Objects.requireNonNull(sourceNode);
        Objects.requireNonNull(targetNode);
        if (hasCycleForAddEdge(sourceNode, targetNode)) {
            throw new IllegalArgumentException(String.format("Edge(%s -> %s) has a cycle.", sourceNode, targetNode));
        }
        edgeMap.put(sourceNode, targetNode, null);
    }

    public List<Node> getNodes() {
        return new ArrayList<>(nodeMap.keySet());
    }

    public List<Pair<Node, Node>> getEdges() {
        List<Pair<Node, Node>> edges = new ArrayList<>();
        for (MultiKey<? extends Node> key : edgeMap.keySet()) {
            Node sourceNode = key.getKey(0);
            Node targetNode = key.getKey(1);
            edges.add(new ImmutablePair<>(sourceNode, targetNode));
        }
        return edges;
    }

    public boolean containsNode(Node node) {
        Objects.requireNonNull(node);
        return nodeMap.containsKey(node);
    }

    public boolean containsEdge(Node sourceNode, Node targetNode) {
        Objects.requireNonNull(sourceNode);
        Objects.requireNonNull(targetNode);
        return edgeMap.containsKey(sourceNode, targetNode);
    }


    public NodeInfo getNodeInfo(Node node) {
        Objects.requireNonNull(node);
        return nodeMap.get(node);
    }


    public int getNodeCount() {
        return nodeMap.size();
    }


    public int getEdgeCount() {
        return edgeMap.size();
    }

    public boolean isStartNode(Node node) {
        Objects.requireNonNull(node);
        return node.equals(getStartNode());
    }

    public boolean isEndNode(Node node) {
        Objects.requireNonNull(node);
        return node.equals(getEndNode());
    }

    public boolean isForkNode(Node node) {
        Objects.requireNonNull(node);
        return getNextNodes(node).size() > 1;
    }

    public boolean isJoinNode(Node node) {
        Objects.requireNonNull(node);
        return getPrevNodes(node).size() > 1;
    }

    public int toNextJoinNodeStep(Node node) {
        Objects.requireNonNull(node);
        int step = 0;
        List<Node> nodes;
        do {
            nodes = getNextNodes(node);
            if (nodes.size() == 1) {
                step++;
            }
        } while (nodes.size() == 1);
        return step;
    }

    public Node getStartNode() {
        List<Node> nodes = new ArrayList<>();
        for (Node node : nodeMap.keySet()) {
            if (getPrevNodes(node).isEmpty()) {
                nodes.add(node);
            }
        }
        if (nodes.isEmpty()) {
            return null;
        }
        if (nodes.size() != 1) {
            throw new IllegalStateException(String.format("Can not have many start node(size=%s) in graph.", nodes.size()));
        }
        return nodes.get(0);
    }


    public Node getEndNode() {
        List<Node> nodes = new ArrayList<>();
        for (Node node : nodeMap.keySet()) {
            if (getNextNodes(node).isEmpty()) {
                nodes.add(node);
            }
        }
        if (nodes.isEmpty()) {
            return null;
        }
        if (nodes.size() > 1) {
            throw new IllegalStateException(String.format("Can not have many end node(size=%s) in graph.", nodes.size()));
        }
        return nodes.get(0);
    }


    public List<Node> getPrevNodes(Node node) {
        Objects.requireNonNull(node);
        List<Node> nodes = new ArrayList<>();
        for (MultiKey<? extends Node> key : edgeMap.keySet()) {
            Node targetNode = key.getKey(1);
            if (targetNode.equals(node)) {
                nodes.add(key.getKey(0));
            }
        }
        return nodes;
    }

    public List<Node> getNextNodes(Node node) {
        Objects.requireNonNull(node);
        List<Node> nodes = new ArrayList<>();
        for (MultiKey<? extends Node> key : edgeMap.keySet()) {
            Node sourceNode = key.getKey(0);
            if (sourceNode.equals(node)) {
                nodes.add(key.getKey(1));
            }
        }
        return nodes;
    }


    private boolean hasCycleForAddEdge(Node sourceNode, Node targetNode) {
        Queue<Node> queue = new LinkedList<>();
        queue.add(targetNode);

        int nodeCount = this.getNodeCount();
        while (!queue.isEmpty() && (--nodeCount > 0)) {
            Node node = queue.poll();

            for (Node nextNode : getNextNodes(node)) {
                if (nextNode.equals(sourceNode)) {
                    return true;
                }
                queue.add(nextNode);
            }
        }

        return false;
    }

    @Override
    public Iterator<Node> iterator() {
        return new GraphItr();
    }

    private class GraphItr implements Iterator<Node> {

        private Queue<Node> queue = new LinkedList<>();
        private Map<Node, Integer> notZeroIndegreeNodeMap = new HashMap<>();

        GraphItr() {
            for (Node node : nodeMap.keySet()) {
                int inDegree = getPrevNodes(node).size();
                if (inDegree == 0) {
                    queue.add(node);
                } else {
                    notZeroIndegreeNodeMap.put(node, inDegree);
                }
            }
        }

        public boolean hasNext() {
            return !queue.isEmpty();
        }

        public Node next() {
            Node node = queue.poll();
            List<Node> nextNodes = getNextNodes(node);
            for (Node nextNode : nextNodes) {
                Integer inDegree = notZeroIndegreeNodeMap.get(nextNode);
                inDegree--;
                if (inDegree == 0) {
                    queue.offer(nextNode);
                    notZeroIndegreeNodeMap.remove(nextNode);
                } else {
                    notZeroIndegreeNodeMap.put(nextNode, inDegree);
                }
            }
            return node;
        }
    }

    public static void main(String[] args) {
        DirectedGraph<String, String> graph = new DirectedGraph<>();
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
