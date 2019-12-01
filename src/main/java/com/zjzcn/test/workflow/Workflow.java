package com.zjzcn.test.workflow;

import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Workflow {

    private String code;
    private String desc;
    private WorkflowGraph<String, WorkflowNode> graph;

    public Workflow(String code, String desc, WorkflowGraph<String, WorkflowNode> graph) {
        this.code = code;
        this.desc = desc;
        this.graph = graph;
    }

    public Map<String, Object> execute(Map<String, Object> params) {
        Map<String, Pair<String, String>> inputOutputs = new LinkedHashMap<>();
        Map<String, Map<String, Object>> resultMap = new LinkedHashMap<>();
        for (String nodeId : graph) {
            Map<String, Object> inputMap = new HashMap<>();
//            Handler handler = graph.getNodeInfo(nodeId).getHandler();
//            if (graph.isStartNode(nodeId)) {
//                inputMap.putAll(params);
//            } else {
//                for (WorkflowNode.ParamMapping mapping : graph.getNodeInfo(nodeId).getMappings()) {
//                    String fromId = mapping.getFromId();
//                    Map<String, Object> fromResult = resultMap.get(fromId);
//                    inputMap.put(mapping.getField(), MvelUtils.eval(mapping.getFromField(), fromResult.get("data")));
//                }
//            }
//            String input = JsonUtils.toJSONString(inputMap);
//            String output = handler.handle(graph.getNodeInfo(nodeId).getCode(), input);
//            resultMap.put(nodeId, JsonUtils.toMap(output));
//            inputOutputs.put(graph.getNodeInfo(nodeId).getCode() + ":" + nodeId, new ImmutablePair<>(input, output));
        }

        WorkflowLogger.logResult(code, inputOutputs);

        String endNode = graph.getEndNode();
        if (!resultMap.containsKey(endNode)) {
            throw new IllegalStateException(String.format("Workflow(%s) not get execution result.", code));
        }

        return resultMap.get(endNode);
    }

}
