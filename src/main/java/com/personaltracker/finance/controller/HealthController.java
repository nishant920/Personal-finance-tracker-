package com.personaltracker.finance.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public String health() {
        return "OK - server is up";
    }
}
