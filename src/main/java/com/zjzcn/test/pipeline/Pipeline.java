package com.zjzcn.test.pipeline;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class Pipeline {

    private Map<String, Handler<?, ?>> handlers = new LinkedHashMap<>();

    public void addLast(String name, Handler<?, ?> handler) {
        handlers.put(name, handler);
    }

//    public <T, R> R handle(T t) {
//        for (Handler<?, ?> handler : handlers.values()) {
//            handler.handle(t);
//        }
//    }

}
