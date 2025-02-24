package com.example.calculator.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaConsumerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "calculator-topic", groupId = "calculator-group")
    public void consumeMessage(
            String message,
            @Header(name = "X-Request-ID", required = false) String requestId) {

        MDC.put("requestId", requestId);
        logger.info("[{}] Received message: {}", requestId, message);

        try {
            String[] parts = message.split(",");
            if (parts.length != 3) {
                sendResponse(requestId, "{\"error\": \"Invalid request format\"}");
                return;
            }

            String operation = parts[0].trim().toLowerCase();
            BigDecimal a = new BigDecimal(parts[1]);
            BigDecimal b = new BigDecimal(parts[2]);

            BigDecimal result;

            switch (operation) {
                case "sum":
                    result = a.add(b);
                    break;
                case "sub":
                    result = a.subtract(b);
                    break;
                case "mul":
                case "mult": // Garantir que ambas versões são aceitas
                    result = a.multiply(b);
                    break;
                case "div":
                    if (b.equals(BigDecimal.ZERO)) { // Tratamento correto da divisão por zero
                        sendResponse(requestId, "{\"error\": \"Division by zero is not allowed\"}");
                        return;
                    }
                    result = a.divide(b, 10, RoundingMode.HALF_UP);
                    break;
                default:
                    sendResponse(requestId, "{\"error\": \"Unsupported operation\"}");
                    return;
            }

            logger.info("[{}] Calculation Result: {}", requestId, result);
            sendResponse(requestId, "{\"result\": " + result.toPlainString() + "}");

        } catch (NumberFormatException e) {
            logger.error("[{}] Error parsing numbers: {}", requestId, e.getMessage());
            sendResponse(requestId, "{\"error\": \"Invalid number format\"}");
        } catch (Exception e) {
            logger.error("[{}] Unexpected error: {}", requestId, e.getMessage());
            sendResponse(requestId, "{\"error\": \"Error processing request\"}");
        } finally {
            MDC.clear();
        }
    }

    private void sendResponse(String requestId, String response) {
        logger.info("[{}] Sending response: {}", requestId, response);

        if (requestId == null || requestId.isEmpty()) {
            logger.error("Request ID is null or empty, response may not be received correctly.");
            return;
        }

        ProducerRecord<String, String> record = new ProducerRecord<>("calculator-response-topic", requestId, response);
        record.headers().add(new RecordHeader("X-Request-ID", requestId.getBytes())); // Adiciona o header

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        logger.error("[{}] Error sending response to Kafka: {}", requestId, ex.getMessage());
                    } else {
                        logger.info("[{}] Response successfully sent to Kafka", requestId);
                    }
                });
    }

}
