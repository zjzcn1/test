package com.zjzcn.test.control.transport;

public class Response extends Message {

	private Exception exception;

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

}
