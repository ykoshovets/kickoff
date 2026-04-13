package com.kickoff.prediction_service.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic coinsAwardTopic() {
        return TopicBuilder.name("coins.award")
                .partitions(20)
                .replicas(1)
                .build();
    }
}