package com.zjzcn.test.control.transport;

public interface Future<V> {

    boolean cancel();

    boolean isCancelled();

    boolean isDone();

    boolean isSuccess();

    V get();

}
