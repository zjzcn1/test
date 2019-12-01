package com.zjzcn.test.graph;

import com.zjzcn.test.graph.meta.MappingMeta;
import lombok.Data;

import java.util.List;

@Data
public class NodeInfo {

    private String nodeCode;
    private Handler handler;
    private List<MappingMeta> mappings;
}
