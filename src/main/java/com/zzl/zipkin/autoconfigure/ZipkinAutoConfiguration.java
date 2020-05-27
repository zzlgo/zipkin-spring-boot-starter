package com.zzl.zipkin.autoconfigure;

import brave.context.log4j2.ThreadContextCurrentTraceContext;
import brave.propagation.CurrentTraceContext;
import com.zzl.zipkin.instrument.http.WebHttpServerParser;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.sleuth.instrument.web.TraceWebServletAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.Sender;
import zipkin2.reporter.kafka.KafkaSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javax.servlet.DispatcherType.*;

/**
 * @author zzl on 2019/3/27.
 * @description zipkin自动配置
 */
@Configuration
@ConditionalOnClass(ByteArraySerializer.class)
@ConditionalOnProperty(value = "spring.zipkin.sender.type", havingValue = "kafka")
@EnableConfigurationProperties({ZipkinKafkaProperties.class})
@AutoConfigureOrder(-1)
public class ZipkinAutoConfiguration {


    /**
     * 自定义kafkaSender
     *
     * @param config
     * @return
     */
    @Bean
    Sender kafkaSender(ZipkinKafkaProperties config) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key.serializer", ByteArraySerializer.class.getName());
        properties.put("value.serializer", ByteArraySerializer.class.getName());
        properties.put("bootstrap.servers", join(config.getBootstrapServers()));

        return KafkaSender.newBuilder()
                .topic(config.getTopic())
                .overrides(properties)
                .build();
    }

    /**
     * 适配 sleuth 2.1.3.RELEASE 版本
     *
     * @param config
     * @return
     */
    @Bean
    Sender zipkinSender(ZipkinKafkaProperties config) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("key.serializer", ByteArraySerializer.class.getName());
        properties.put("value.serializer", ByteArraySerializer.class.getName());
        properties.put("bootstrap.servers", join(config.getBootstrapServers()));

        return KafkaSender.newBuilder()
                .topic(config.getTopic())
                .overrides(properties)
                .build();
    }

    private String join(List<String> parts) {
        StringBuilder to = new StringBuilder();
        for (int i = 0, length = parts.size(); i < length; i++) {
            to.append(parts.get(i));
            if (i + 1 < length) {
                to.append(',');
            }
        }
        return to.toString();
    }

    @Bean
    public FilterRegistrationBean filterZipkinFilter() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean(new FilterLoggingFilter());
        filterRegistrationBean.setDispatcherTypes(ASYNC, ERROR, FORWARD, INCLUDE, REQUEST);
        filterRegistrationBean.setOrder(TraceWebServletAutoConfiguration.TRACING_FILTER_ORDER + 2);
        return filterRegistrationBean;
    }


    @Bean
    @ConditionalOnProperty(name = "spring.sleuth.http.legacy.enabled",
            havingValue = "false", matchIfMissing = true)
    @ConditionalOnMissingBean
    brave.http.HttpServerParser defaultHttpServerParser() {
        return new WebHttpServerParser();
    }


    /**
     * 采用default模式
     *
     * @return
     */
    @Bean
    @ConditionalOnMissingBean
    CurrentTraceContext sleuthCurrentTraceContext() {
        return ThreadContextCurrentTraceContext.create(CurrentTraceContext.Default.create());
    }
}
