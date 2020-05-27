package com.zzl.zipkin.autoconfigure;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author zzl on 2019/3/27.
 * @description 自定义zipkin的kafka地址。监控kafka一般与业务kafka独立
 */
@ConfigurationProperties(prefix = "spring.zipkin.kafka")
public class ZipkinKafkaProperties {

    private List<String> bootstrapServers;
    private String topic;

    public List<String> getBootstrapServers() {
        return bootstrapServers;
    }

    public void setBootstrapServers(List<String> bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
