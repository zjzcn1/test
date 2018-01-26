package com.zjzcn.test.water;

public class ResponseFuture implements Future<Response> {
	
	private volatile FutureState state = FutureState.DOING;

	private Object lock = new Object();

    private long createTime = System.currentTimeMillis();

    private Request request;

    private Response response;

    private Exception exception;

    private long timeoutMillis;

    private enum FutureState {
        DOING(),
        DONE(),
        CANCELLED()
    }

	public ResponseFuture(Request request, long timeoutMillis) {
		this.request = request;
		this.timeoutMillis = timeoutMillis;
	}

	public void onSuccess(Response response) {
		this.response = response;
		done();
	}

	public void onFailure(Response response) {
		this.exception = response.getException();
		done();
	}

    @Override
    public Response get() {
        synchronized (lock) {
            if (isDone()) {
                return getResponseOrThrowException();
            }

            long waitTime = timeoutMillis - costTimeMillis();
            if (waitTime > 0) {
                for (;;) {
                    try {
                        lock.wait(waitTime);
                    } catch (InterruptedException e) {
                        // nothing
                    }

                    if (!isDoing()) {
                        break;
                    }

                    waitTime = timeoutMillis - costTimeMillis();
                    if (waitTime <= 0) {
                        break;
                    }
                }
            }

            if (isDoing()) {
                cancel();
                throw new RuntimeException("Request timeout: requestId=" + request.getRequestId()
                        + ", costTimeMs=" + costTimeMillis());
            }
        }
        return getResponseOrThrowException();
    }

	@Override
	public boolean cancel() {
        synchronized (lock) {
            if (!isDoing()) {
                return false;
            }

            state = FutureState.CANCELLED;
            lock.notifyAll();
        }

        return true;
	}
	
    @Override
	public boolean isCancelled() {
		return state == FutureState.CANCELLED;
	}

	@Override
	public boolean isDone() {
		return state == FutureState.DONE;
	}

    @Override
	public boolean isSuccess() {
		return isDone() && (exception == null);
	}

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public long getCreateTime() {
        return createTime;
    }

	private boolean isDoing() {
		return state == FutureState.DOING;
	}

	private boolean done() {
		synchronized (lock) {
			if (!isDoing()) {
				return false;
			}

			state = FutureState.DONE;
			lock.notifyAll();
		}

		return true;
	}

    private Response getResponseOrThrowException() {
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException)exception;
            } else {
                throw new RuntimeException(exception);
            }
        }

        return response;
    }

	private long costTimeMillis() {
        return System.currentTimeMillis() - createTime;
    }

}


