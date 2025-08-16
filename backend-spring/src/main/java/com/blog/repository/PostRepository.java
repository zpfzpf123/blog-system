package com.blog.repository;

import com.blog.entity.Post;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByCategory(Category category, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE " +
           "(:#{#categoryIds == null || #categoryIds.isEmpty()} = true OR p.category.id IN :categoryIds) AND " +
           "(:#{#tagIds == null || #tagIds.isEmpty()} = true OR EXISTS (SELECT 1 FROM p.tags t WHERE t.id IN :tagIds)) AND " +
           "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.desc) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Post> findPostsByFilters(
        @Param("categoryIds") List<Long> categoryIds,
        @Param("tagIds") List<Long> tagIds,
        @Param("search") String search,
        Pageable pageable
    );


}