package com.teastall.repository;

import com.teastall.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(
            LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE DATE(o.createdAt) = CURRENT_DATE ORDER BY o.createdAt DESC")

    List<Order> findOrdersForDay(@Param("startOfDay") LocalDateTime startOfDay,
                                 @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
            SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o
            WHERE o.status = :status
            AND o.createdAt >= :startOfDay AND o.createdAt < :endOfDay
            """)
    Double getRevenueForDay(@Param("status") Order.OrderStatus status,
                            @Param("startOfDay") LocalDateTime startOfDay,
                            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
            SELECT COUNT(o) FROM Order o
            WHERE o.status = :status
            AND o.createdAt >= :startOfDay AND o.createdAt < :endOfDay
            """)
    Long getOrderCountForDay(@Param("status") Order.OrderStatus status,
                             @Param("startOfDay") LocalDateTime startOfDay,
                             @Param("endOfDay") LocalDateTime endOfDay);

    @Query("""
            SELECT COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi
            JOIN oi.order o
            WHERE o.status = :status
            AND o.createdAt >= :startOfDay AND o.createdAt < :endOfDay
            """)
    Long getItemsSoldForDay(@Param("status") Order.OrderStatus status,
                            @Param("startOfDay") LocalDateTime startOfDay,
                            @Param("endOfDay") LocalDateTime endOfDay);
}
