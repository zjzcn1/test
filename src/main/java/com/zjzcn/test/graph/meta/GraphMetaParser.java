package com.zjzcn.test.graph.meta;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

public class GraphMetaParser {


    private static final String START_NODE_CODE = "AAAA.A";
    private static final String END_NODE_CODE = "ZZZZ.Z";

    public static GraphMeta parse(String graphJson) {
        JSONObject graphJo = JSON.parseObject(graphJson);

        GraphMeta meta = new GraphMeta();
        meta.setName(graphJo.getString("name"));

        JSONArray nodesJa = graphJo.getJSONArray("nodes");
        for (Object obj : nodesJa) {
            JSONObject nodeJo = (JSONObject) obj;
            String code = nodeJo.getString("code");

            NodeMeta nodeMeta = new NodeMeta();
            meta.getNodeMetas().add(nodeMeta);

            nodeMeta.setCode(code);

            if (START_NODE_CODE.equals(code)) {
                nodeMeta.setNodeType(NodeMeta.NodeType.START_NODE);
            } else if (END_NODE_CODE.equals(code)) {
                nodeMeta.setNodeType(NodeMeta.NodeType.END_NODE);
            } else {
                nodeMeta.setNodeType(NodeMeta.NodeType.EXECUTE_NODE);
            }

            JSONObject mappingsJo = nodeJo.getJSONObject("mappings");
            if (mappingsJo == null) {
                continue;
            }
            for (String field : mappingsJo.keySet()) {
                MappingMeta mappingMeta = new MappingMeta();
                nodeMeta.getMappings().add(mappingMeta);
                mappingMeta.setField(field);
                String sourceCode = StringUtils.substringBetween(mappingsJo.getString(field), "@{", "}");
                mappingMeta.setSourceCode(sourceCode);
                String sourceField = StringUtils.substringBetween(mappingsJo.getString(field), "${", "}");
                mappingMeta.setSourceField(sourceField);
            }
        }

        return meta;
    }


}
