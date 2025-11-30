package com.blog.repository;

import com.blog.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 项目数据访问接口
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    /**
     * 按状态查找项目
     */
    List<Project> findByStatus(String status);
    
    /**
     * 按收藏状态查找项目
     */
    List<Project> findByIsFavorite(Boolean isFavorite);
    
    /**
     * 按名称模糊查询
     */
    List<Project> findByNameContainingIgnoreCase(String name);
    
    /**
     * 按状态和收藏状态查找
     */
    List<Project> findByStatusAndIsFavorite(String status, Boolean isFavorite);
}
