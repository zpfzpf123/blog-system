package com.blog.repository;

import com.blog.entity.ProjectApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectApiRepository extends JpaRepository<ProjectApi, Long> {
    List<ProjectApi> findByProjectIdOrderBySortOrderAsc(Long projectId);
    List<ProjectApi> findByProjectIdAndCategoryOrderBySortOrderAsc(Long projectId, String category);
    List<ProjectApi> findByProjectIdAndStatusOrderBySortOrderAsc(Long projectId, String status);
    List<ProjectApi> findByProjectIdAndMockEnabledTrueOrderBySortOrderAsc(Long projectId);
    
    @Query("SELECT DISTINCT a.category FROM ProjectApi a WHERE a.projectId = ?1 AND a.category IS NOT NULL")
    List<String> findDistinctCategoriesByProjectId(Long projectId);
    
    void deleteByProjectId(Long projectId);
}
