package com.zzl.zipkin.instrument.thread;

import brave.Tracing;

import java.util.concurrent.*;

/**
 * 包装线程池，传递trace信息
 */
public class TraceableScheduledExecutorService extends TraceableExecutorService implements ScheduledExecutorService {

    private static final String DEFAULT_SPAN_NAME = "Traceable-ScheduledExecutorService";


    public TraceableScheduledExecutorService(ExecutorService delegate, Tracing tracing) {
        super(delegate, tracing, DEFAULT_SPAN_NAME);
    }

    public TraceableScheduledExecutorService(ExecutorService delegate, Tracing tracing, String spanName) {
        super(delegate, tracing, spanName);
    }

    private ScheduledExecutorService getScheduledExecutorService() {
        return (ScheduledExecutorService) super.delegate;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        Runnable r = new TraceRunnable(tracing(), command, super.spanName);
        return getScheduledExecutorService().schedule(r, delay, unit);
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        Callable<V> c = new TraceCallable<>(tracing(), callable, super.spanName);
        return getScheduledExecutorService().schedule(c, delay, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        Runnable r = new TraceRunnable(tracing(), command, super.spanName);
        return getScheduledExecutorService().scheduleAtFixedRate(r, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        Runnable r = new TraceRunnable(tracing(), command, super.spanName);
        return getScheduledExecutorService().scheduleWithFixedDelay(r, initialDelay, delay, unit);
    }

}
