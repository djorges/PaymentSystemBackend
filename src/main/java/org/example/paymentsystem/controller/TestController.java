package org.example.paymentsystem.controller;

import org.example.paymentsystem.model.RegisterRequestDto;
import org.example.paymentsystem.model.TokenResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TODO: Delete this
 * Project: PaymentSystem
 * Author: djorges
 * Date: 06/07/2025
 */

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping("/")
    public ResponseEntity<String> index(){
       return ResponseEntity.ok("Hello World");
    }
}
