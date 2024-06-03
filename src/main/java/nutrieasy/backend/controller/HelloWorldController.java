package nutrieasy.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Resa S.
 * Date: 04-05-2024
 * Created in IntelliJ IDEA.
 */
@RestController
public class HelloWorldController {

    private final RestTemplate restTemplate;

    public HelloWorldController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> hello() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, World!");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }

    @GetMapping("hello-ml")
    public ResponseEntity<Map<String, String>> helloML() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Hello, Machine Learning!");
        response.put("status", "success");

        restTemplate.getForObject("http://localhost:5000/", String.class);
        return ResponseEntity.ok(response);
    }
}
