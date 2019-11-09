package com.zjzcn.test.graph;

public class HttpHandler implements Handler {

    private HttpClient client = new HttpClient("http://www.baidu.com");
    @Override
    public String handle(String param) {
        System.out.println("http--->" + param);
//        System.out.println(client.get("/"));
        return  param + "=ok --> ";
    }

    public static void main(String[] args) throws Exception {
        new HttpHandler().handle("xxxx");
    }
}
