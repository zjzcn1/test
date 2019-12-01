package com.zjzcn.test.workflow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class WorkflowBuilder {


    public static Workflow build(String brickUrl, String json) {
        JSONObject graphJo = JSON.parseObject(json);
        String wfCode = graphJo.getString("brick_code");
        String wfDesc = graphJo.getString("brick_desc");

        WorkflowGraph<String, WorkflowNode> graph = new WorkflowGraph<>();
        Workflow workflow = new Workflow(wfCode, wfDesc, graph);

        JSONArray nodesJa = graphJo.getJSONArray("nodes");
        for (Object obj : nodesJa) {
            JSONObject nodeJo = (JSONObject) obj;
            String id = nodeJo.getString("unique_name");
            String code = nodeJo.getString("brick_code");

            WorkflowNode node = new WorkflowNode();
            node.setId(id);
            node.setCode(code);

            JSONObject mappingsJo = nodeJo.getJSONObject("param_mappings");
            if (mappingsJo != null) {
                for (String field : mappingsJo.keySet()) {
                    WorkflowNode.ParamMapping mapping = new WorkflowNode.ParamMapping();
                    node.getMappings().add(mapping);
                    mapping.setField(field);
                    String fromId = StringUtils.substringBetween(mappingsJo.getString(field), "@{", "}");
                    mapping.setFromId(fromId);
                    String fromField = StringUtils.substringBetween(mappingsJo.getString(field), "${", "}");
                    mapping.setFromField(fromField);

                    graph.addEdge(fromId, id);
                }
            }
            workflow.getGraph().addNode(id, node);
        }

//        Handler httpHandler = new HttpHandler(brickUrl);
//        for (String nodeId : workflow.getGraph().getNodes()) {
//            WorkflowNode node = workflow.getGraph().getNodeInfo(nodeId);
//            if (workflow.getGraph().isStartNode(nodeId)) {
//                node.setHandler(new StartHandler());
//            } else if (workflow.getGraph().isEndNode(nodeId)) {
//                node.setHandler(new EndHandler());
//            } else {
//                node.setHandler(httpHandler);
//            }
//        }

        return workflow;
    }


}
