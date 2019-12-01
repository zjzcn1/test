package com.zjzcn.test.graph.meta;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class NodeMeta {
    private NodeType nodeType;
    private String code;
    private List<MappingMeta> mappings = new ArrayList<>();

    public enum NodeType {
        START_NODE, END_NODE, EXECUTE_NODE
    }
}

