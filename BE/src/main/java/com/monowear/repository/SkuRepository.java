package com.monowear.repository;

import com.monowear.entity.Sku;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SkuRepository extends JpaRepository<Sku, Long> {

    Optional<Sku> findBySkuCode(String skuCode);

    List<Sku> findByProductIdAndIsActiveTrue(Long productId);

    long countByProductId(Long productId);

    // Trừ tồn kho (trả 1 nếu thành công, 0 nếu không đủ)
    @Modifying
    @Query("UPDATE Sku s SET s.stock = s.stock - :qty, s.version = s.version + 1 " +
           "WHERE s.id = :id AND s.stock >= :qty")
    int deductStock(@Param("id") Long id, @Param("qty") int qty);

    // Hoàn tồn kho
    @Modifying
    @Query("UPDATE Sku s SET s.stock = s.stock + :qty, s.version = s.version + 1 WHERE s.id = :id")
    int restoreStock(@Param("id") Long id, @Param("qty") int qty);
}
