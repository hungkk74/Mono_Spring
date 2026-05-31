package com.monowear.repository;

import com.monowear.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Query("SELECT c FROM Coupon c WHERE UPPER(c.code) = UPPER(:code)")
    Optional<Coupon> findByCodeIgnoreCase(@Param("code") String code);

    @Modifying
    @Query("UPDATE Coupon c SET c.usedCount = COALESCE(c.usedCount, 0) + 1 WHERE c.id = :id AND (c.usageLimit IS NULL OR COALESCE(c.usedCount, 0) < c.usageLimit)")
    int incrementUsedCount(@Param("id") Long id);
}
