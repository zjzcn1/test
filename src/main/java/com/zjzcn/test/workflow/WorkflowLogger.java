package com.zjzcn.test.workflow;

import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.Factory;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Slf4j
public class WorkflowLogger {

    private static final String GRAPH_PNG_LOG_FILE = "logs/graph/%s.png";

    private static final String GRAPH_RESULT_LOG_FILE = "logs/result/%s.log";

    public static void logGraph(String name, WorkflowGraph<String, WorkflowNode> graph) {
        MutableGraph g = Factory.mutGraph().setDirected(true);
        for (String nodeId : graph.getNodes()) {
            WorkflowNode workflowNode = graph.getNodeInfo(nodeId);
            MutableNode node = Factory.mutNode(workflowNode.getCode() + ":" + nodeId);
            if (graph.isStartNode(nodeId)) {
                node.add(Color.BLUE);
            }
            if (graph.isEndNode(nodeId)) {
                node.add(Color.RED);
            }
            g.add(node);
            for (String targetId : graph.getNextNodes(nodeId)) {
                node.addLink(graph.getNodeInfo(targetId).getCode() + ":" + targetId);
            }
        }
        try {
            String fileName = String.format(GRAPH_PNG_LOG_FILE, name);
            Graphviz.fromGraph(g).render(Format.PNG).toFile(new File(fileName));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void logResult(String name, Map<String, Pair<String, String>> results) {
        String fileName = String.format(GRAPH_RESULT_LOG_FILE, name + "_" + DateFormatUtils.format(new Date(), "yyyyMMddHHmmss.SSS"));
        try {
            StringBuilder sb = new StringBuilder();
            for (String code : results.keySet()) {
                String input = results.get(code).getKey();
                String output = results.get(code).getValue();
                sb.append("[I] ").append(code).append(": ").append(input).append("\n");
                sb.append("[O] ").append(code).append(": ").append(output).append("\n");
            }
            FileUtils.writeStringToFile(new File(fileName), sb.toString(), "UTF-8");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
