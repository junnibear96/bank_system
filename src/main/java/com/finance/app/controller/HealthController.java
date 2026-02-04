package com.finance.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public String healthCheck() {
        return "Spring Boot 정상 작동 중! (MariaDB 연결 완료)";
    }
}
