package com.zjzcn.test.graph;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zjzcn.test.graph.meta.MappingMeta;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ExecuteEngine {

    public String execute(String param, DirectedGraph<String, NodeInfo> graph) {
        Map<String, String> resultMap = new HashMap<>();
        for (String nodeId : graph) {
            Handler handler = graph.getNodeInfo(nodeId).getHandler();
            if (graph.isStartNode(nodeId)) {
                String result = handler.handle(nodeId, param);
                resultMap.put(nodeId, result);
            } else {
                JSONObject input = new JSONObject();
                for (MappingMeta mappingMeta : graph.getNodeInfo(nodeId).getMappings()) {
                    String sourceCode = mappingMeta.getSourceCode();
                    String sourceResult = resultMap.get(sourceCode);
                    JSONObject sourceJo = JSON.parseObject(sourceResult);
                    input.put(mappingMeta.getField(), sourceJo.get(mappingMeta.getSourceField()));
                }
                String result = handler.handle(nodeId, input.toJSONString());
                resultMap.put(nodeId, result);
            }
        }

        String endNode = graph.getEndNode();
        if (!resultMap.containsKey(endNode)) {
            throw new IllegalStateException(String.format("Graph(%s) not get execution result.", graph.name()));
        }

        System.out.println(resultMap);
        return resultMap.get(endNode);
    }

}
