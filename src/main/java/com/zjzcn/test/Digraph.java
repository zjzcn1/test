package com.zjzcn.test;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;


public class Digraph<NodeId, NodeInfo> implements Iterable<NodeId> {

    private volatile Map<NodeId, NodeInfo> nodeMap = new HashMap<>();
    private volatile MultiKeyMap<NodeId, Void> edgeMap = new MultiKeyMap<>();

    public Digraph() {
    }

    public void addNode(NodeId node, NodeInfo nodeInfo) {
        Objects.requireNonNull(node);
        nodeMap.put(node, nodeInfo);
    }

    public void addEdge(NodeId fromNode, NodeId toNode) {
        Objects.requireNonNull(fromNode);
        Objects.requireNonNull(toNode);
        edgeMap.put(fromNode, toNode, null);
    }

    public List<NodeId> getNodes() {
        return new ArrayList<>(nodeMap.keySet());
    }

    public List<Pair<NodeId, NodeId>> getEdges() {
        List<Pair<NodeId, NodeId>> edges = new ArrayList<>();
        for (MultiKey<? extends NodeId> key : edgeMap.keySet()) {
            NodeId fromNode = key.getKey(0);
            NodeId toNode = key.getKey(1);
            edges.add(new ImmutablePair<>(fromNode, toNode));
        }
        return edges;
    }

    public boolean containsNode(NodeId node) {
        Objects.requireNonNull(node);
        return nodeMap.containsKey(node);
    }

    public boolean containsEdge(NodeId fromNode, NodeId toNode) {
        Objects.requireNonNull(fromNode);
        Objects.requireNonNull(toNode);
        return edgeMap.containsKey(fromNode, toNode);
    }


    public NodeInfo getNodeInfo(NodeId node) {
        Objects.requireNonNull(node);
        return nodeMap.get(node);
    }


    public int getNodeCount() {
        return nodeMap.size();
    }


    public int getEdgeCount() {
        return edgeMap.size();
    }

    public boolean isStartNode(NodeId node) {
        Objects.requireNonNull(node);
        return node.equals(getStartNode());
    }

    public boolean isEndNode(NodeId node) {
        Objects.requireNonNull(node);
        return node.equals(getEndNode());
    }

    public boolean isForkNode(NodeId node) {
        Objects.requireNonNull(node);
        return getNextNodes(node).size() > 1;
    }

    public boolean isJoinNode(NodeId node) {
        Objects.requireNonNull(node);
        return getPrevNodes(node).size() > 1;
    }

    public List<NodeId> getStartNode() {
        List<NodeId> nodes = new ArrayList<>();
        for (NodeId node : nodeMap.keySet()) {
            if (getPrevNodes(node).isEmpty()) {
                nodes.add(node);
            }
        }
        return nodes;
    }


    public List<NodeId> getEndNode() {
        List<NodeId> nodes = new ArrayList<>();
        for (NodeId node : nodeMap.keySet()) {
            if (getNextNodes(node).isEmpty()) {
                nodes.add(node);
            }
        }
        return nodes;
    }


    public List<NodeId> getPrevNodes(NodeId node) {
        Objects.requireNonNull(node);
        List<NodeId> nodes = new ArrayList<>();
        for (MultiKey<? extends NodeId> key : edgeMap.keySet()) {
            NodeId toNode = key.getKey(1);
            if (toNode.equals(node)) {
                nodes.add(key.getKey(0));
            }
        }
        return nodes;
    }

    public List<NodeId> getNextNodes(NodeId node) {
        Objects.requireNonNull(node);
        List<NodeId> nodes = new ArrayList<>();
        for (MultiKey<? extends NodeId> key : edgeMap.keySet()) {
            NodeId fromNode = key.getKey(0);
            if (fromNode.equals(node)) {
                nodes.add(key.getKey(1));
            }
        }
        return nodes;
    }

    public boolean hasCycle() {
        Queue<NodeId> queue = new LinkedList<>();
        Map<NodeId, Integer> notZeroInDegreeNodeMap = new HashMap<>();

        for (NodeId node : nodeMap.keySet()) {
            int inDegree = getPrevNodes(node).size();
            if (inDegree == 0) {
                queue.add(node);
            } else {
                notZeroInDegreeNodeMap.put(node, inDegree);
            }
        }

        while(!queue.isEmpty()) {
            NodeId node = queue.poll();
            List<NodeId> nextNodes = getNextNodes(node);
            for (NodeId nextNode : nextNodes) {
                Integer inDegree = notZeroInDegreeNodeMap.get(nextNode);
                inDegree--;
                if (inDegree == 0) {
                    queue.offer(nextNode);
                    notZeroInDegreeNodeMap.remove(nextNode);
                } else {
                    notZeroInDegreeNodeMap.put(nextNode, inDegree);
                }
            }
        }

        return !notZeroInDegreeNodeMap.isEmpty();
    }

    @Override
    public Iterator<NodeId> iterator() {
        return new GraphItr();
    }

    private class GraphItr implements Iterator<NodeId> {

        private Queue<NodeId> queue = new LinkedList<>();
        private Map<NodeId, Integer> notZeroInDegreeNodeMap = new HashMap<>();

        GraphItr() {
            for (NodeId node : nodeMap.keySet()) {
                int inDegree = getPrevNodes(node).size();
                if (inDegree == 0) {
                    queue.add(node);
                } else {
                    notZeroInDegreeNodeMap.put(node, inDegree);
                }
            }
        }

        public boolean hasNext() {
            return !queue.isEmpty();
        }

        public NodeId next() {
            NodeId node = queue.poll();
            List<NodeId> nextNodes = getNextNodes(node);
            for (NodeId nextNode : nextNodes) {
                Integer inDegree = notZeroInDegreeNodeMap.get(nextNode);
                inDegree--;
                if (inDegree == 0) {
                    queue.offer(nextNode);
                    notZeroInDegreeNodeMap.remove(nextNode);
                } else {
                    notZeroInDegreeNodeMap.put(nextNode, inDegree);
                }
            }
            return node;
        }
    }

    public static void main(String[] args) {
        Digraph<String, String> graph = new Digraph<>();
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
//        graph.addEdge("AAAB.A", "AAAA.A");

        System.out.println(graph.hasCycle());
        System.out.println(graph.getStartNode());
        System.out.println(graph.getEndNode());
        System.out.println(graph.getPrevNodes("AAAA.A"));
        System.out.println(graph.getPrevNodes("ZZZZ.Z"));
        System.out.println(graph.getNextNodes("AAAA.A"));
        System.out.println(graph.getNextNodes("ZZZZ.Z"));
    }
}
