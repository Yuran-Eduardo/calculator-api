package com.example.rest.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private String latestResponse;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String sendMessageAndGetResult(String message, String requestId) {
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>("calculator-topic", requestId, message);
            record.headers().add("X-Request-ID", requestId.getBytes()); // Set header

            logger.info("[{}] Sending message to Kafka: {}", requestId, message);

            kafkaTemplate.send(record).get(2, TimeUnit.SECONDS);

            synchronized (this) {
                this.wait(2000);
            }

            return latestResponse != null ? latestResponse : "{\"error\": \"No response received\"}";
        } catch (Exception e) {
            logger.error("Failed to send Kafka message", e);
            return "{\"error\": \"Internal error\"}";
        }
    }

    public void sendMessage(String message, String requestId) {
        ProducerRecord<String, String> record = new ProducerRecord<>("calculator-topic", requestId, message);
        record.headers().add("X-Request-ID", requestId.getBytes());
        kafkaTemplate.send(record);
    }

    @KafkaListener(topics = "calculator-response-topic", groupId = "rest-group")
    public void listenResponse(String message, @Header(name = "X-Request-ID", required = false) String requestId) {
        if (requestId == null) {
            logger.error(" Received response without X-Request-ID! Response: {}", message);
            return;
        }

        logger.info("[{}]  Received response from Kafka: {}", requestId, message);

        synchronized (this) {
            this.latestResponse = message;
            this.notifyAll();
        }
    }

}
