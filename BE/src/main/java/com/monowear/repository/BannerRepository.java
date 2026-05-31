package com.monowear.repository;

import com.monowear.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    List<Banner> findByIsActiveTrueOrderByDisplayOrderAsc();

    long countByIsActiveTrue();

    @Query("SELECT COALESCE(MAX(b.displayOrder), 0) FROM Banner b")
    int findMaxDisplayOrder();
}
