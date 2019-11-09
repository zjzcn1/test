package com.zjzcn.test.graph;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExecuteEngine {

    public String execute(String param, DirectedGraph<String, Handler, Void> graph) {
        Map<String, String> resultMap = new HashMap<>();
        for (String nodeId : graph) {
            System.out.println(nodeId + ":");
            StringBuilder results = new StringBuilder();
            List<String> prevNodes = graph.getPrevNodes(nodeId);
            if (prevNodes.isEmpty()) {
                results = new StringBuilder(param);
            }

            for (String prevId : prevNodes) {
                if (!resultMap.containsKey(prevId)) {
                    throw new IllegalStateException(String.format("Graph(%s) for node(%s) not get previous node(%s) result.",
                            graph.name(), prevId, nodeId));
                }
                results.append(resultMap.get(prevId)).append(":");
            }

            Handler handler = graph.getNodeInfo(nodeId);
            String result = handler.handle(results.toString());
            resultMap.put(nodeId, nodeId);
        }

        String endNode = graph.getEndNode();
        if (!resultMap.containsKey(endNode)) {
            throw new IllegalStateException(String.format("Graph(%s) not get execution result.", graph.name()));
        }

        return resultMap.get(endNode);
    }

    public static void main(String[] args) {
        DirectedGraph<String, Handler, Void> graph = new DirectedGraph<>();

        Handler handler = new HttpHandler();
        graph.addNode("AAAA.A", new StartHandler());
        graph.addNode("BBBB.B", handler);
        graph.addNode("BBBB.C", handler);
        graph.addNode("BBBB.D", handler);
        graph.addNode("BBBB.E", handler);
        graph.addNode("BBBB.F", handler);
        graph.addNode("ZZZZ.Z", new EndHandler());

        graph.addEdge("AAAA.A", "BBBB.B");
        graph.addEdge("AAAA.A", "BBBB.C");
        graph.addEdge("AAAA.A", "BBBB.D");

        graph.addEdge("BBBB.B", "BBBB.D");
        graph.addEdge("BBBB.C", "BBBB.D");

        graph.addEdge("BBBB.D", "BBBB.E");
        graph.addEdge("BBBB.D", "BBBB.F");

        graph.addEdge("BBBB.E", "ZZZZ.Z");
        graph.addEdge("BBBB.F", "ZZZZ.Z");

        GraphLogger.logGraph(graph);

//        graph.addEdge("AAAA.A", "AAAB.A");
//        graph.addEdge("AAAB.A", "AAAC.A");
//        graph.addEdge("AAAC.A", "ZZZZ.Z");

//        System.out.println(graph.getStartNodes());
//        System.out.println(graph.getEndNodes());
//        System.out.println(graph.getPrevNodes("AAAA.A"));
//        System.out.println(graph.getPrevNodes("ZZZZ.Z"));
//        System.out.println(graph.getNextNodes("AAAA.A"));
//        System.out.println(graph.getNextNodes("ZZZZ.Z"));
//        System.out.println(graph.hasCycle("AAAA.A", "ZZZZ.Z"));


        ExecuteEngine engine = new ExecuteEngine();
        engine.execute("start", graph);
    }
}
