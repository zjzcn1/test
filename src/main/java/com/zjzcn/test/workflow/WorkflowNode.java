package com.zjzcn.test.workflow;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

@Data
public class WorkflowNode {

    private String id;
    private String code;
//    private Handler handler;
    private List<ParamMapping> mappings = new LinkedList<>();

    @Data
    public static class ParamMapping {
        private String field;
        private String fromId;
        private String fromField;
    }
}
