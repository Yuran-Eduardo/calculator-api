package com.example.calculator.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaConsumerServiceTest {

    private KafkaTemplate<String, String> kafkaTemplate;
    private KafkaConsumerService consumerService;

    @BeforeEach
    void setUp() {
        kafkaTemplate = mock(KafkaTemplate.class);
        consumerService = new KafkaConsumerService(kafkaTemplate);

        // Mock KafkaTemplate.send() to return a CompletableFuture instead of null
        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(CompletableFuture.completedFuture(null));
    }

    @Test
    void testSumOperation() {
        String requestId = "test-id-1";
        String message = "sum,8,3";

        consumerService.consumeMessage(message, requestId);

        ArgumentCaptor<ProducerRecord<String, String>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, String> capturedRecord = recordCaptor.getValue();

        assertEquals("calculator-response-topic", capturedRecord.topic());
        assertEquals(requestId, capturedRecord.key());
        assertEquals("{\"result\": 11}", capturedRecord.value());
    }

    @Test
    void testDivisionByZero() {
        String requestId = "test-id-5";
        String message = "div,5,0";

        consumerService.consumeMessage(message, requestId);

        ArgumentCaptor<ProducerRecord<String, String>> responseCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(responseCaptor.capture());

        assertEquals("{\"error\": \"Division by zero is not allowed\"}", responseCaptor.getValue().value());
    }

    @Test
    void testInvalidOperation() {
        String requestId = "test-id-6";
        String message = "mod,5,3";

        consumerService.consumeMessage(message, requestId);

        ArgumentCaptor<ProducerRecord<String, String>> responseCaptor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, times(1)).send(responseCaptor.capture());

        assertEquals("{\"error\": \"Unsupported operation\"}", responseCaptor.getValue().value());
    }
}
