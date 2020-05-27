package com.zzl.zipkin.instrument.thread;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;

import java.util.concurrent.Callable;

/**
 * 包装callable，传递trace信息
 *
 * @param <V>
 */
public class TraceCallable<V> implements Callable<V> {


    private static final String DEFAULT_SPAN_NAME = "TraceCallable";

    private final Tracer tracer;
    private final Callable<V> delegate;
    private final TraceContext parent;
    private final String spanName;

    public TraceCallable(Tracing tracing, Callable<V> delegate) {
        this(tracing, delegate, null);
    }

    public TraceCallable(Tracing tracing, Callable<V> delegate, String spanName) {
        this.tracer = tracing.tracer();
        this.delegate = delegate;
        this.parent = tracing.currentTraceContext().get();
        this.spanName = spanName != null ? spanName : DEFAULT_SPAN_NAME;
    }

    @Override
    public V call() throws Exception {
        ScopedSpan span = this.tracer.startScopedSpanWithParent(this.spanName, this.parent);
        try {
            return this.delegate.call();
        } catch (Exception | Error e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }
}
