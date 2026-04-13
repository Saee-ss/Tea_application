package com.teastall.controller;

import com.teastall.dto.BillingDtos.*;
import com.teastall.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(ApiResponse.ok("Order created", orderService.createOrder(req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getAllOrders()));
    }

    @GetMapping("/open")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOpenOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOpenOrders()));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getTodaysOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getTodaysOrders()));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DailySummary>> getDailySummary() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getDailySummary()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.getOrder(id)));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<ApiResponse<OrderResponse>> closeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Order closed", orderService.closeOrder(id)));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Order cancelled", orderService.cancelOrder(id)));
    }
}
