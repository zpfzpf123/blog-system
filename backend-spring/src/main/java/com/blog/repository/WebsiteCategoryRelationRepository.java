package com.blog.repository;

import com.blog.entity.WebsiteCategoryRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 网站分类关系Repository接口
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@Repository
public interface WebsiteCategoryRelationRepository extends JpaRepository<WebsiteCategoryRelation, Long> {
    
    /**
     * 根据网站ID查找所有分类关系
     */
    List<WebsiteCategoryRelation> findByWebsiteId(Long websiteId);
    
    /**
     * 根据分类ID查找所有网站关系
     */
    List<WebsiteCategoryRelation> findByCategoryId(Long categoryId);
    
    /**
     * 根据网站ID删除所有分类关系
     */
    void deleteByWebsiteId(Long websiteId);
    
    /**
     * 根据分类ID删除所有网站关系
     */
    void deleteByCategoryId(Long categoryId);
    
    /**
     * 根据网站ID和分类ID查找关系
     */
    WebsiteCategoryRelation findByWebsiteIdAndCategoryId(Long websiteId, Long categoryId);
    
    /**
     * 检查网站和分类是否存在关系
     */
    boolean existsByWebsiteIdAndCategoryId(Long websiteId, Long categoryId);
}
