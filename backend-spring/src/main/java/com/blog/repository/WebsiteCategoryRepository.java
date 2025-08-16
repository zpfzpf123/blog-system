package com.blog.repository;

import com.blog.entity.WebsiteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 网站分类数据访问接口
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@Repository
public interface WebsiteCategoryRepository extends JpaRepository<WebsiteCategory, Long> {
    
    /**
     * 根据名称查找分类
     */
    Optional<WebsiteCategory> findByName(String name);
    
    /**
     * 查找所有活跃的分类
     */
    List<WebsiteCategory> findByIsActiveTrue();
    
    /**
     * 根据排序顺序查找活跃的分类
     */
    List<WebsiteCategory> findByIsActiveTrueOrderBySortOrderAscNameAsc();
    
    /**
     * 统计活跃分类数量
     */
    Long countByIsActiveTrue();
    
    /**
     * 根据名称和活跃状态查找分类
     */
    Optional<WebsiteCategory> findByNameAndIsActiveTrue(String name);
    
    /**
     * 根据名称模糊搜索分类
     */
    @Query("SELECT c FROM WebsiteCategory c WHERE c.isActive = true AND LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<WebsiteCategory> findByNameContainingIgnoreCaseAndIsActiveTrue(String keyword);
}
