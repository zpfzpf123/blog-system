package com.blog.repository;

import com.blog.entity.PageTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageTemplateRepository extends JpaRepository<PageTemplate, Long> {
    
    List<PageTemplate> findByCategory(String category);
    
    List<PageTemplate> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT DISTINCT p.category FROM PageTemplate p")
    List<String> findAllCategories();
    
    @Query("SELECT DISTINCT p.techStack FROM PageTemplate p WHERE p.techStack IS NOT NULL")
    List<String> findAllTechStacks();
    
    List<PageTemplate> findByTechStack(String techStack);
    
    List<PageTemplate> findAllByOrderByCreatedAtDesc();
    
    List<PageTemplate> findAllByOrderByCopyCountDesc();
}
