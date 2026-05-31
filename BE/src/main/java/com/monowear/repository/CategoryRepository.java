package com.monowear.repository;

import com.monowear.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByIsActiveTrue();

    @Query("SELECT c FROM Category c WHERE c.parent IS NULL AND c.isActive = true")
    List<Category> findRootCategories();

    @Query("SELECT c.id FROM Category c WHERE c.parent.id = :parentId AND c.isActive = true")
    List<Long> findChildIds(@Param("parentId") Long parentId);
}
