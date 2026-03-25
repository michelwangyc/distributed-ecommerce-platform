package edu.northeastern.warehouseservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import jakarta.annotation.PreDestroy;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WarehouseConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final AtomicInteger totalOrders = new AtomicInteger(0);
    private final ConcurrentHashMap<Integer, AtomicInteger> productTotals = new ConcurrentHashMap<>();

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void receiveMessage(
            String message,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {

        try {
            System.out.println("Received message: " + message);

            Map<String, Object> payload = objectMapper.readValue(
                    message, new TypeReference<Map<String, Object>>() {}
            );

            totalOrders.incrementAndGet();

            List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    Integer productId = (Integer) item.get("product_id");
                    Integer quantity = (Integer) item.get("quantity");

                    productTotals
                            .computeIfAbsent(productId, k -> new AtomicInteger(0))
                            .addAndGet(quantity);
                }
            }

            channel.basicAck(tag, false);

        } catch (Exception e) {
            e.printStackTrace();
            channel.basicNack(tag, false, false);
        }
    }

    @PreDestroy
    public void printSummaryOnShutdown() {
        System.out.println("=================================");
        System.out.println("Warehouse shutting down");
        System.out.println("Total orders: " + totalOrders.get());
        System.out.println("=================================");
    }

    public int getTotalOrders() {
        return totalOrders.get();
    }

    public Map<Integer, AtomicInteger> getProductTotals() {
        return productTotals;
    }
}