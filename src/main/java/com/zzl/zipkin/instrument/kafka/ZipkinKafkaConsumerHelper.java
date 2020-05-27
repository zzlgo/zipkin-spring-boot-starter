package com.zzl.zipkin.instrument.kafka;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

/**
 * @author zzl on 2019/3/27.
 * @description 提供对zipkin的支持
 * <p>
 * kafka消费是一次poll多条。而每条消息需要绑定一个线程去传递trace信息，所以这里采用回调方式
 * ，由业务方手动对每条消息触发trace信息。这样方便控制
 */
public class ZipkinKafkaConsumerHelper<K, V> {


    private final Tracing tracing;
    private final TraceContext.Extractor<Headers> extractor;

    public ZipkinKafkaConsumerHelper(Tracing tracing) {
        this.tracing = tracing;
        this.extractor = tracing.propagation().extractor(KafkaPropagation.HEADER_GETTER);
    }


    /**
     * 消费消息，添加trace信息
     *
     * @param record   消费的消息
     * @param callback span的生命周期，在这个callback方法内。需要在callback内保持链路完整
     */
    public void traceCallBack(ConsumerRecord<K, V> record, ConsumeCallback callback) {

        Span span = null;
        Tracer.SpanInScope scope = null;
        try {
            TraceContextOrSamplingFlags extracted = extractor.extract(record.headers());
            if (extracted.samplingFlags() == null || !extracted.extra().isEmpty()) {
                //是否需要监控
                span = tracing.tracer().nextSpan(extracted);
                if (!span.isNoop()) {
                    span.name("kafka consume").kind(Span.Kind.CONSUMER)
                            .tag("topic", record.topic())
                            .tag("partition", String.valueOf(record.partition()))
                            .tag("offset", String.valueOf(record.offset()));

                    Header header = record.headers().lastHeader(KafkaPropagation.HEADER_SENDTIME);
                    if (header != null) {
                        //记录消费延迟
                        Long start = Long.valueOf(new String(header.value(), KafkaPropagation.UTF_8));
                        span.tag("consume delay", System.currentTimeMillis() - start + "ms");
                    }
                    span.start();
                    scope = tracing.tracer().withSpanInScope(span);

                }
            }

            //执行回调，真正的消息处理过程
            callback.process();
        } catch (Exception e) {
            //记录报错
            if (span != null) {
                span.error(e);
            }
            //错误由上层来处理
            throw e;
        } finally {
            if (scope != null) {
                scope.close();
            }
            if (span != null) {
                span.finish();
            }
        }
    }


    public interface ConsumeCallback {

        void process();
    }
}
