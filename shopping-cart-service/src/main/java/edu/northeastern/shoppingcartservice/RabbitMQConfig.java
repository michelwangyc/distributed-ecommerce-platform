package edu.northeastern.shoppingcartservice;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String QUEUE_NAME = "warehouse-queue";

    @Bean
    public Queue warehouseQueue() {
        return new Queue(QUEUE_NAME, true);
    }
}