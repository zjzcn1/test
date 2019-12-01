package com.zjzcn.test.graph;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class GraphLogger {

    private static final String GRAPH_PNG_LOG_FILE = "log/graph.png";

    public static void logGraph(DirectedGraph<String, NodeInfo> graph) {
        MutableGraph g = Factory.mutGraph().setDirected(true);
        for (String nodeId : graph.getNodes()) {
            MutableNode node = Factory.mutNode(nodeId);
            if (graph.isStartNode(nodeId)) {
                node.add(Color.BLUE);
            }
            if (graph.isEndNode(nodeId)) {
                node.add(Color.RED);
            }
            g.add(node);
            for (String targetId : graph.getNextNodes(nodeId)) {
                node.addLink(targetId);
            }
        }
        try {
            Graphviz.fromGraph(g).render(Format.PNG).toFile(new File(GRAPH_PNG_LOG_FILE));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
