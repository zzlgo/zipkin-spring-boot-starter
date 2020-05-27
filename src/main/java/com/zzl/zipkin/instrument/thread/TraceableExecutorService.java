package com.zzl.zipkin.instrument.thread;

import brave.Tracing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;


/**
 * 包装线程池，传递trace信息
 */
public class TraceableExecutorService implements ExecutorService {

    private static final String DEFAULT_SPAN_NAME = "TraceableExecutorService";

    final ExecutorService delegate;
    private final Tracing tracing;
    final String spanName;


    public TraceableExecutorService(ExecutorService delegate, Tracing tracing) {
        this(delegate, tracing, DEFAULT_SPAN_NAME);
    }

    public TraceableExecutorService(ExecutorService delegate, Tracing tracing, String spanName) {
        this.delegate = delegate;
        this.tracing = tracing;
        this.spanName = spanName;
    }

    @Override
    public void execute(Runnable command) {
        final Runnable r = new TraceRunnable(tracing(), command, this.spanName);
        this.delegate.execute(r);
    }

    @Override
    public void shutdown() {
        this.delegate.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return this.delegate.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return this.delegate.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return this.delegate.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.delegate.awaitTermination(timeout, unit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        Callable<T> c = new TraceCallable<>(tracing(), task, this.spanName);
        return this.delegate.submit(c);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        Runnable r = new TraceRunnable(tracing(), task, this.spanName);
        return this.delegate.submit(r, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        Runnable r = new TraceRunnable(tracing(), task, this.spanName);
        return this.delegate.submit(r);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return this.delegate.invokeAll(wrapCallableCollection(tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException {
        return this.delegate.invokeAll(wrapCallableCollection(tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return this.delegate.invokeAny(wrapCallableCollection(tasks));
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.invokeAny(wrapCallableCollection(tasks), timeout, unit);
    }

    private <T> Collection<? extends Callable<T>> wrapCallableCollection(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> ts = new ArrayList<>();
        for (Callable<T> task : tasks) {
            if (!(task instanceof TraceCallable)) {
                ts.add(new TraceCallable<>(tracing(), task, this.spanName));
            }
        }
        return ts;
    }

    Tracing tracing() {
        return this.tracing;
    }

}
