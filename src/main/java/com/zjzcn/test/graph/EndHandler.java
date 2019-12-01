package com.zjzcn.test.graph;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EndHandler implements Handler {

    @Override
    public String handle(String nodeId, String param) {
        return  param;
    }
}
