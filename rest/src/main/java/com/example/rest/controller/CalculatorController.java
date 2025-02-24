package com.example.rest.controller;

import com.example.rest.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class CalculatorController {

    private static final Logger logger = LoggerFactory.getLogger(CalculatorController.class);
    private final KafkaProducerService kafkaProducerService;

    public CalculatorController(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<String> calculate(
            @RequestParam String operation,
            @RequestParam String a,
            @RequestParam String b,
            @RequestHeader(value = "X-Request-ID", required = false) String requestId) {

        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put("requestId", requestId);
        logger.info("Received request: {} {} {} with Request ID: {}", operation, a, b, requestId);

        String response = kafkaProducerService.sendMessageAndGetResult(operation + "," + a + "," + b, requestId);

        MDC.clear();

        return ResponseEntity.ok()
                .header("X-Request-ID", requestId)
                .body(response);
    }
}
