package com.zjzcn.test.transport;

public interface MessageHandler {

    Response handleRequest(Request request);

    void handleResponse(Response response);
}
