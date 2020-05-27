package com.zzl.zipkin.instrument.kafka;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Headers;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Future;

/**
 * @author zzl on 2019/3/27.
 * @description 提供对zipkin的支持
 */
public class ZipkinKafkaProducer<K, V> extends KafkaProducer<K, V> {


    private final Tracing tracing;
    private final TraceContext.Injector<Headers> injector;

    public ZipkinKafkaProducer(Map<String, Object> configs, Tracing tracing) {
        super(configs);
        this.tracing = tracing;
        this.injector = tracing.propagation().injector(KafkaPropagation.HEADER_SETTER);
    }

    public ZipkinKafkaProducer(Properties properties, Tracing tracing) {
        super(properties);
        this.tracing = tracing;
        this.injector = tracing.propagation().injector(KafkaPropagation.HEADER_SETTER);
    }


    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> record) {
        return this.send(record, null);
    }

    @Override
    public Future<RecordMetadata> send(ProducerRecord<K, V> record, Callback callback) {


        Span span = tracing.tracer().nextSpan();
        tracing.propagation().keys().forEach(key -> record.headers().remove(key));
        injector.inject(span.context(), record.headers());
        if (!span.isNoop()) {
            span.name("kafka send").kind(Span.Kind.PRODUCER)
                    .tag("topic", record.topic());
            //记录发送时间
            record.headers().add(KafkaPropagation.HEADER_SENDTIME, String.valueOf(System.currentTimeMillis()).getBytes(KafkaPropagation.UTF_8));
            if (record.key() instanceof String && !"".equals(record.key())) {
                span.tag("key", record.key().toString());
            }
            span.start();
        }
        try (Tracer.SpanInScope ws = tracing.tracer().withSpanInScope(span)) {
            //发送
            return super.send(record, new TracingCallback(span, callback));
        } catch (Throwable e) {
            //出现异常，需要自己finish
            span.error(e).finish();
            //错误由上层来处理
            throw e;
        }
    }


    private class TracingCallback implements Callback {

        final Span span;
        final Callback wrappedCallback;

        TracingCallback(Span span, Callback wrappedCallback) {
            this.span = span;
            this.wrappedCallback = wrappedCallback;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {

            if (exception != null) {
                span.error(exception);
            }
            span.finish();
            if (wrappedCallback != null) {
                wrappedCallback.onCompletion(metadata, exception);
            }
        }
    }
}
