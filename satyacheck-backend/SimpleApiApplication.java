package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class SimpleApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleApiApplication.class, args);
    }

    @RestController
    public static class SimpleController {
        
        @GetMapping("/api/health")
        public Map<String, Object> healthCheck() {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "up");
            response.put("message", "API is running");
            return response;
        }
        
        @GetMapping("/api/test")
        public Map<String, Object> test() {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Test endpoint working");
            response.put("timestamp", System.currentTimeMillis());
            return response;
        }
    }
}