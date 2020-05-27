package com.zzl.zipkin.instrument.kafka;

import brave.propagation.Propagation.Getter;
import brave.propagation.Propagation.Setter;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.Charset;

/**
 * @author zzl on 2019/3/27.
 * @description kafka传递zipkin信息
 */
final class KafkaPropagation {

    static final Charset UTF_8 = Charset.forName("UTF-8");
    static final String HEADER_SENDTIME = "sendTime";

    static final Setter<Headers, String> HEADER_SETTER = (carrier, key, value) -> {
        carrier.remove(key);
        carrier.add(key, value.getBytes(UTF_8));
    };

    static final Getter<Headers, String> HEADER_GETTER = (carrier, key) -> {
        Header header = carrier.lastHeader(key);
        if (header == null) {
            return null;
        }
        return new String(header.value(), UTF_8);
    };

    KafkaPropagation() {
    }
}
