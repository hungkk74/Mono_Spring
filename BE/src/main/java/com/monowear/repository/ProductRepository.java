package com.monowear.repository;

import com.monowear.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findBySlug(String slug);

    @Query("SELECT DISTINCT p FROM Product p JOIN FETCH p.category LEFT JOIN FETCH p.skus WHERE p.id IN :ids ORDER BY p.id DESC")
    List<Product> findAllWithDetailsByIds(@Param("ids") List<Long> ids);
}
