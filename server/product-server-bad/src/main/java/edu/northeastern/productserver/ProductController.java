package edu.northeastern.productserver;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ProductController {

    private final AtomicInteger productIdGenerator = new AtomicInteger(1);
    private final Map<Integer, Product> productStore = new ConcurrentHashMap<>();

    @PostMapping("/product")
    public ResponseEntity<?> createProduct(@RequestBody Product product) {


        if (Math.random() < 0.5) {
            return ResponseEntity
                    .status(503)
                    .body(Map.of(
                            "error", "SIMULATED_FAILURE",
                            "message", "This is a bad product instance"
                    ));
        }

        int newId = productIdGenerator.getAndIncrement();
        product.setProduct_id(newId);
        productStore.put(newId, product);

        return ResponseEntity
                .status(201)
                .body(Map.of("product_id", newId));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Integer productId) {
        Product product = productStore.get(productId);
        if (product == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "NOT_FOUND",
                    "message", "Product not found"
            ));
        }
        return ResponseEntity.ok(product);
    }
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}