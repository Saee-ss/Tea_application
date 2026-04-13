package com.teastall.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

public class BillingDtos {

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreateOrderRequest {
        private String tableNumber;
        private String customerName;
        private List<OrderItemRequest> items;
        private Double discountPercent;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class OrderItemRequest {
        private Long menuItemId;
        private Integer quantity;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderResponse {
        private Long id;
        private String tableNumber;
        private String customerName;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime closedAt;
        private List<OrderItemResponse> items;
        private Double discountPercent;
        private Double subtotal;
        private Double discountAmount;
        private Double taxAmount;
        private Double totalAmount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class OrderItemResponse {
        private Long id;
        private Long menuItemId;
        private String itemName;
        private String category;
        private Integer quantity;
        private Double unitPrice;
        private Double lineTotal;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MenuItemRequest {
        private String name;
        private String category;
        private Double price;
        private String icon;
        private Boolean available;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class DailySummary {
        private String date;
        private Long totalOrders;
        private Double totalRevenue;
        private Double totalTax;
        private Double totalDiscount;
        private Long itemsSold;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ApiResponse<T> {
        private boolean success;
        private String message;
        private T data;

        public static <T> ApiResponse<T> ok(T data) {
            return new ApiResponse<>(true, "Success", data);
        }
        public static <T> ApiResponse<T> ok(String message, T data) {
            return new ApiResponse<>(true, message, data);
        }
        public static <T> ApiResponse<T> error(String message) {
            return new ApiResponse<>(false, message, null);
        }
    }
}
