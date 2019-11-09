package com.zjzcn.test.pipeline;


public class PipelineException extends RuntimeException {

    public PipelineException(Throwable e) {
        super(e);
    }

    public PipelineException(String message) {
        super(message);
    }

    public PipelineException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
