package com.zjzcn.test.graph;

import com.alibaba.fastjson.JSONObject;
import com.zjzcn.test.graph.meta.GraphMeta;
import com.zjzcn.test.graph.meta.GraphMetaParser;
import com.zjzcn.test.graph.meta.MappingMeta;
import com.zjzcn.test.graph.meta.NodeMeta;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class Main {

    public static void main(String[] args) {

        String path = Main.class.getResource("/").getPath() + "graph.json";
        System.out.println(path);

        String json = null;
        try {
            json = new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(json);

        GraphMeta meta = GraphMetaParser.parse(json);

        DirectedGraph<String, NodeInfo> graph = new DirectedGraph<>(meta.getName());

        String url = "http://localhost:8081/";
        Handler handler = new HttpHandler(url);
        for (NodeMeta node : meta.getNodeMetas()) {
            NodeInfo info = new NodeInfo();
            info.setNodeCode(node.getCode());
            if (node.getNodeType() == NodeMeta.NodeType.START_NODE) {
                info.setHandler(new StartHandler());
            } else if (node.getNodeType() == NodeMeta.NodeType.END_NODE) {
                info.setHandler(new EndHandler());
            } else {
                info.setHandler(handler);
            }
            graph.addNode(node.getCode(), info);

            if (node.getMappings() != null) {
                info.setMappings(node.getMappings());
                for (MappingMeta mapping : node.getMappings()) {
                    graph.addEdge(mapping.getSourceCode(), node.getCode());
                }
            }
        }

//        graph.addNode("AAAA.A", new StartHandler());
//        graph.addNode("BBBB.B", handler);
//        graph.addNode("BBBB.C", handler);
//        graph.addNode("BBBB.D", handler);
//        graph.addNode("BBBB.E", handler);
//        graph.addNode("BBBB.F", handler);
//        graph.addNode("ZZZZ.Z", new EndHandler());
//
//        graph.addEdge("AAAA.A", "BBBB.B");
//        graph.addEdge("AAAA.A", "BBBB.C");
//        graph.addEdge("AAAA.A", "BBBB.D");
//
//        graph.addEdge("BBBB.B", "BBBB.D");
//        graph.addEdge("BBBB.C", "BBBB.D");
//
//        graph.addEdge("BBBB.D", "BBBB.E");
//        graph.addEdge("BBBB.D", "BBBB.F");
//
//        graph.addEdge("BBBB.E", "ZZZZ.Z");
//        graph.addEdge("BBBB.F", "ZZZZ.Z");

        GraphLogger.logGraph(graph);

        JSONObject jo = new JSONObject();
        jo.put("b", 1);
        jo.put("c", 1);
        jo.put("d", 1);

        ExecuteEngine engine = new ExecuteEngine();
        String result = engine.execute(jo.toJSONString(), graph);
        System.out.println(result);
    }
}
