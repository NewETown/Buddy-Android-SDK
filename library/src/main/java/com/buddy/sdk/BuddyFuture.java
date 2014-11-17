package com.buddy.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by shawn on 7/7/14.
 */
class BuddyFuture<V> implements Future<V> {

    private boolean done;
    private V value;
    private CountDownLatch waitHandle = new CountDownLatch(1);

    private Object SyncObj = new Object();

    private class CallbackWrapper {
        public BuddyFutureCallback callback;
        public BuddyFuture<V> handle;
    }

    private List<CallbackWrapper> callbacks;

    public BuddyFuture() {

    }

    public BuddyFuture<V> continueWith(final BuddyFutureCallback<V> callback) {

        if (!done) {
            if (callbacks == null) {
                callbacks = new ArrayList<CallbackWrapper>();
            }

            CallbackWrapper wrapper = new CallbackWrapper();
            wrapper.callback = callback;
            wrapper.handle = new BuddyFuture<V>();
            callbacks.add(wrapper);
            return wrapper.handle;

        } else {
            callback.completed(this);
        }
        return this;
    }



    public void setValue(V val) {
        synchronized (SyncObj) {
            value = val;

            if (!done) {
                done = true;
                if (callbacks != null) {
                    for (CallbackWrapper cb : callbacks) {
                        cb.handle.setValue(val);
                    }
                }
                SyncObj.notify();
            }
        }

    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        synchronized (SyncObj) {
            if (!done) {
                SyncObj.wait();
            }
        }
        return value;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (!done) {
            synchronized (SyncObj) {
                SyncObj.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
            }
        }
        return value;
    }
}
