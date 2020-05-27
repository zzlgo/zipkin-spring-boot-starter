package com.zzl.zipkin.instrument.thread;

import brave.ScopedSpan;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;

/**
 * 包装runnable，传递trace信息
 */
public class TraceRunnable implements Runnable {


    private static final String DEFAULT_SPAN_NAME = "TraceRunnable";

    private final Tracer tracer;
    private final Runnable delegate;
    private final TraceContext parent;
    private final String spanName;

    public TraceRunnable(Tracing tracing, Runnable delegate) {
        this(tracing, delegate, null);
    }

    public TraceRunnable(Tracing tracing, Runnable delegate, String spanName) {
        this.tracer = tracing.tracer();
        this.delegate = delegate;
        this.parent = tracing.currentTraceContext().get();
        this.spanName = spanName != null ? spanName : DEFAULT_SPAN_NAME;
    }

    @Override
    public void run() {
        ScopedSpan span = this.tracer.startScopedSpanWithParent(this.spanName, this.parent);
        try {
            this.delegate.run();
        } catch (Exception | Error e) {
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }
    }
}
