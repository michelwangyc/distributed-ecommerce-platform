package edu.northeastern.warehouseservice;

import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class WarehouseShutdownLogger {

    private final WarehouseConsumer warehouseConsumer;

    public WarehouseShutdownLogger(WarehouseConsumer warehouseConsumer) {
        this.warehouseConsumer = warehouseConsumer;
    }

    @PreDestroy
    public void onShutdown() {
        System.out.println("======================================");
        System.out.println("Warehouse shutting down...");
        System.out.println("Total number of orders: " + warehouseConsumer.getTotalOrders());
        System.out.println("======================================");
    }
}