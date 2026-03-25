package edu.northeastern.shoppingcartservice;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class ShoppingCartController {

    private final AtomicInteger cartIdGenerator = new AtomicInteger(1);
    private final AtomicInteger orderIdGenerator = new AtomicInteger(1);

    private final ConcurrentHashMap<Integer, ShoppingCart> cartStore = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate;
    private final RabbitMQPublisher rabbitMQPublisher;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ShoppingCartController(RestTemplate restTemplate, RabbitMQPublisher rabbitMQPublisher) {
        this.restTemplate = restTemplate;
        this.rabbitMQPublisher = rabbitMQPublisher;
    }

    @PostMapping("/shopping-cart")
    public ResponseEntity<?> createShoppingCart(@RequestBody Map<String, Integer> request) {
        Integer customerId = request.get("customer_id");

        if (customerId == null || customerId < 1) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "INVALID_INPUT",
                    "message", "customer_id must be a positive integer"
            ));
        }

        int cartId = cartIdGenerator.getAndIncrement();

        ShoppingCart cart = new ShoppingCart();
        cart.setShopping_cart_id(cartId);
        cart.setCustomer_id(customerId);

        cartStore.put(cartId, cart);

        return ResponseEntity.status(201).body(Map.of(
                "shopping_cart_id", cartId
        ));
    }

    @PostMapping("/shopping-carts/{shoppingCartId}/addItem")
    public ResponseEntity<?> addItem(
            @PathVariable Integer shoppingCartId,
            @RequestBody CartItem item) {

        ShoppingCart cart = cartStore.get(shoppingCartId);
        if (cart == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "NOT_FOUND",
                    "message", "Shopping cart not found"
            ));
        }

        if (item.getProduct_id() == null || item.getProduct_id() < 1 ||
                item.getQuantity() == null || item.getQuantity() < 1 || item.getQuantity() > 10000) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "INVALID_INPUT",
                    "message", "product_id must be positive and quantity must be in range 1..10000"
            ));
        }

        cart.getItems().add(item);

        return ResponseEntity.status(204).build();
    }

    @PostMapping("/shopping-carts/{shoppingCartId}/checkout")
    public ResponseEntity<?> checkout(
            @PathVariable Integer shoppingCartId,
            @RequestBody Map<String, String> request) {

        ShoppingCart cart = cartStore.get(shoppingCartId);
        if (cart == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "NOT_FOUND",
                    "message", "Shopping cart not found"
            ));
        }

        if (cart.getItems().isEmpty()) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "INVALID_CART",
                    "message", "Shopping cart is empty"
            ));
        }

        String creditCardNumber = request.get("credit_card_number");
        if (creditCardNumber == null || creditCardNumber.isBlank()) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "INVALID_INPUT",
                    "message", "credit_card_number is required"
            ));
        }

        //String ccaUrl = "http://localhost:8082/credit-card-authorizer/authorize";
        String ccaUrl = "http://credit-card-authorizer:8082/credit-card-authorizer/authorize";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(
                Map.of("credit_card_number", creditCardNumber),
                headers
        );

        try {
            ResponseEntity<String> ccaResponse =
                    restTemplate.postForEntity(ccaUrl, entity, String.class);

            if (ccaResponse.getStatusCode().is2xxSuccessful()) {
                int orderId = orderIdGenerator.getAndIncrement();

                HashMap<String, Object> payload = new HashMap<>();
                payload.put("order_id", orderId);
                payload.put("shopping_cart_id", shoppingCartId);
                payload.put("customer_id", cart.getCustomer_id());
                payload.put("items", cart.getItems());

                String message = objectMapper.writeValueAsString(payload);

                rabbitMQPublisher.publish(message);

                return ResponseEntity.ok(Map.of("order_id", orderId));
            }

            return ResponseEntity.status(500).body(Map.of(
                    "error", "CCA_ERROR",
                    "message", "Unexpected credit card authorizer response"
            ));

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                return ResponseEntity.status(400).body(Map.of(
                        "error", "INVALID_INPUT",
                        "message", "Credit card number format is invalid"
                ));
            }

            if (e.getStatusCode().value() == 402) {
                return ResponseEntity.status(402).body(Map.of(
                        "error", "DECLINED",
                        "message", "Credit card was declined"
                ));
            }

            return ResponseEntity.status(500).body(Map.of(
                    "error", "CCA_ERROR",
                    "message", "Credit card authorizer returned an error"
            ));
        } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of(
                "error", "CCA_UNAVAILABLE",
                "message", "Credit card authorizer service unavailable。。。"
        ));
    }




    }
}