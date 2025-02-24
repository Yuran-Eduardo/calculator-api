package com.example.rest.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaProducerServiceTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaProducerService producerService;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        producerService = new KafkaProducerService(kafkaTemplate);
    }

    @Test
    void testSendMessageToKafka() {
        String requestId = "test-id-123";
        String message = "sum,5,3";

        producerService.sendMessage(message, requestId);

        ArgumentCaptor<ProducerRecord<String, String>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);

        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, String> capturedRecord = recordCaptor.getValue();

        // Validate that the correct topic is used
        assertEquals("calculator-topic", capturedRecord.topic());

        // Validate that the request ID is set as the key
        assertEquals(requestId, capturedRecord.key());

        // Validate the message body
        assertEquals(message, capturedRecord.value());

        // Validate that the "X-Request-ID" header is correctly added
        assertNotNull(capturedRecord.headers().lastHeader("X-Request-ID"));
        assertEquals(requestId, new String(capturedRecord.headers().lastHeader("X-Request-ID").value()));
    }
}
