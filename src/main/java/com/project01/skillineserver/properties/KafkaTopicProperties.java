package com.project01.skillineserver.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka")
public class KafkaTopicProperties {
    private String mediaUploadedTopic;
    private String paymentTransaction;
}