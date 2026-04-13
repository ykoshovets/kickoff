package com.kickoff.match_service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MatchEventVerifier {

    @KafkaListener(topics = "match.results", groupId = "verifier-group")
    public void verify(ConsumerRecord<String, String> record) {
        log.info("Kafka message received: {}", record.value());
    }
}