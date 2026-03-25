package edu.northeastern.creditcardauthorizer;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

@RestController
public class CreditCardController {

    private static final Pattern CARD_PATTERN =
            Pattern.compile("^[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{4}$");

    private final Random random = new Random();

    @PostMapping("/credit-card-authorizer/authorize")
    public ResponseEntity<?> authorize(@RequestBody Map<String, String> request) {
        String cardNumber = request.get("credit_card_number");

        if (cardNumber == null || !CARD_PATTERN.matcher(cardNumber).matches()) {
            return ResponseEntity.status(400).body(Map.of(
                    "error", "INVALID_INPUT",
                    "message", "Credit card number format is invalid"
            ));
        }

        int value = random.nextInt(10); // 0~9
        if (value == 0) {
            return ResponseEntity.status(402).body(Map.of(
                    "error", "DECLINED",
                    "message", "Credit card was declined"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Authorized"
        ));
    }
}