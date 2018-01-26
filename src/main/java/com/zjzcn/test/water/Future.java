package com.zjzcn.test.water;

public interface Future<V> {

    boolean cancel();

    boolean isCancelled();

    boolean isDone();

    boolean isSuccess();

    V get();

}
