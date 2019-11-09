package com.zjzcn.test.pipeline;


public interface Handler<T, R> {

    R handle(T t) throws Exception;

}
