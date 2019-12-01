package com.zjzcn.test.graph;


import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpHandler implements Handler {

    private HttpClient client;

    public HttpHandler(String url) {
         client = new HttpClient(url);
    }

    @Override
    public String handle(String nodeId, String param) {
        log.info("[http] ---> node={}, body={}", nodeId, param);
        String resp = client.post(String.format("brick?code=%s", nodeId), param);
        log.info("[http] <--- resp={}", resp);
        return  resp;
    }
}
