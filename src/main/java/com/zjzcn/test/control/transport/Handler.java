package com.zjzcn.test.control.transport;

public interface Handler<C, M> {

    void handle(C ctx, M msg);
}
