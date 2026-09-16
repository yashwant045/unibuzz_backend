package com.unibuzz.crm.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/secure")
    public String secureEndpoint() {
        return "You accessed protected endpoint!";
    }
}