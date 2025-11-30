package com.blog.repository;

import com.blog.entity.CssAnimation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CssAnimationRepository extends JpaRepository<CssAnimation, Long> {
    List<CssAnimation> findByCategory(String category);

    boolean existsByTitle(String title);
}
