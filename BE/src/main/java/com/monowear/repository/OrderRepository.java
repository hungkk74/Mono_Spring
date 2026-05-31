package com.monowear.repository;

import com.monowear.entity.Order;
import com.monowear.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByUserId(Long userId);

    long countByUserIdAndStatus(Long userId, OrderStatus status);

    @Query("SELECT o.id FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Long> findIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT o FROM Order o JOIN FETCH o.user LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.sku s LEFT JOIN FETCH s.product WHERE o.id IN :ids ORDER BY o.createdAt DESC")
    List<Order> findAllWithDetailsByIds(@Param("ids") List<Long> ids);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.sku s " +
           "LEFT JOIN FETCH s.product JOIN FETCH o.user WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items i LEFT JOIN FETCH i.sku WHERE o.id = :id")
    Optional<Order> findByIdWithItemsAndSkus(@Param("id") Long id);
}
