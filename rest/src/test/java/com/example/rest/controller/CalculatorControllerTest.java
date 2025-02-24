package com.example.rest.controller;

import com.example.rest.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalculatorControllerTest {

    private KafkaProducerService kafkaProducerService;
    private CalculatorController controller;

    @BeforeEach
    void setUp() {
        kafkaProducerService = mock(KafkaProducerService.class);
        controller = new CalculatorController(kafkaProducerService);
    }

    @Test
    void testCalculateSum() {
        when(kafkaProducerService.sendMessageAndGetResult("sum,5,3", "test-id"))
                .thenReturn("{\"result\": 8}");

        ResponseEntity<String> response = controller.calculate("sum", "5", "3", "test-id");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("{\"result\": 8}", response.getBody());
    }

    @Test
    void testCalculateDivisionByZero() {
        when(kafkaProducerService.sendMessageAndGetResult("div,5,0", "test-id"))
                .thenReturn("{\"error\": \"Division by zero is not allowed\"}");

        ResponseEntity<String> response = controller.calculate("div", "5", "0", "test-id");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("{\"error\": \"Division by zero is not allowed\"}", response.getBody());
    }
}
