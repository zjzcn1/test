package com.zjzcn.test.graph.meta;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GraphMeta {

    private String name;
    private List<NodeMeta> nodeMetas = new ArrayList<>();

}
