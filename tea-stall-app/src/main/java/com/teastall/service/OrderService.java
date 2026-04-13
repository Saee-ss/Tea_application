package com.teastall.service;

import com.teastall.dto.BillingDtos.*;
import com.teastall.model.*;
import com.teastall.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final double TAX_RATE = 5.0;

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        Order order = Order.builder()
                .tableNumber(req.getTableNumber())
                .customerName(req.getCustomerName())
                .discountPercent(req.getDiscountPercent() != null ? req.getDiscountPercent() : 0.0)
                .status(Order.OrderStatus.OPEN)
                .build();

        List<OrderItem> items = req.getItems().stream().map(itemReq -> {
            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found: " + itemReq.getMenuItemId()));
            return OrderItem.builder()
                    .order(order)
                    .menuItem(menuItem)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(menuItem.getPrice())
                    .lineTotal(menuItem.getPrice() * itemReq.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        order.setItems(items);
        calculateTotals(order);

        Order saved = orderRepository.save(order);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse closeOrder(Long orderId) {
        Order order = getOrderEntity(orderId);
        order.setStatus(Order.OrderStatus.CLOSED);
        order.setClosedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = getOrderEntity(orderId);
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setClosedAt(LocalDateTime.now());
        return toResponse(orderRepository.save(order));
    }

    public OrderResponse getOrder(Long id) {
        return toResponse(getOrderEntity(id));
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getOpenOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.OPEN)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<OrderResponse> getTodaysOrders() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        return orderRepository
                .findByCreatedAtBetweenOrderByCreatedAtDesc(startOfDay, endOfDay)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public DailySummary getDailySummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        Double revenue = orderRepository.getRevenueForDay(Order.OrderStatus.CLOSED, startOfDay, endOfDay);
        Long orders = orderRepository.getOrderCountForDay(Order.OrderStatus.CLOSED, startOfDay, endOfDay);
        Long items = orderRepository.getItemsSoldForDay(Order.OrderStatus.CLOSED, startOfDay, endOfDay);

        List<Order> todaysOrders = orderRepository.findOrdersForDay(startOfDay, endOfDay);
        double totalDiscount = todaysOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.CLOSED)
                .mapToDouble(o -> o.getDiscountAmount() != null ? o.getDiscountAmount() : 0)
                .sum();
        double totalTax = todaysOrders.stream()
                .filter(o -> o.getStatus() == Order.OrderStatus.CLOSED)
                .mapToDouble(o -> o.getTaxAmount() != null ? o.getTaxAmount() : 0)
                .sum();

        return DailySummary.builder()
                .date(LocalDate.now().toString())
                .totalRevenue(round(revenue != null ? revenue : 0))
                .totalOrders(orders != null ? orders : 0)
                .totalTax(round(totalTax))
                .totalDiscount(round(totalDiscount))
                .itemsSold(items != null ? items : 0)
                .build();
    }

    private Order getOrderEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    private void calculateTotals(Order order) {
        double subtotal = order.getItems().stream()
                .mapToDouble(i -> i.getUnitPrice() * i.getQuantity()).sum();
        double disc = order.getDiscountPercent() != null ? order.getDiscountPercent() : 0;
        double discAmount = round(subtotal * disc / 100);
        double afterDisc = subtotal - discAmount;
        double tax = round(afterDisc * TAX_RATE / 100);
        double total = round(afterDisc + tax);

        order.setSubtotal(round(subtotal));
        order.setDiscountAmount(discAmount);
        order.setTaxAmount(tax);
        order.setTotalAmount(total);
    }

    private double round(double val) {
        return Math.round(val * 100.0) / 100.0;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> OrderItemResponse.builder()
                        .id(i.getId())
                        .menuItemId(i.getMenuItem().getId())
                        .itemName(i.getMenuItem().getName())
                        .category(i.getMenuItem().getCategory())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .lineTotal(i.getLineTotal())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .tableNumber(order.getTableNumber())
                .customerName(order.getCustomerName())
                .status(order.getStatus().name())
                .createdAt(order.getCreatedAt())
                .closedAt(order.getClosedAt())
                .items(itemResponses)
                .discountPercent(order.getDiscountPercent())
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .taxAmount(order.getTaxAmount())
                .totalAmount(order.getTotalAmount())
                .build();
    }
}
