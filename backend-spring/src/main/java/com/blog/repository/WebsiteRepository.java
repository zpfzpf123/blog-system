package com.blog.repository;

import com.blog.entity.Website;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 网站数据访问接口
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@Repository
public interface WebsiteRepository extends JpaRepository<Website, Long> {
    
    /**
     * 根据URL查找网站
     */
    Optional<Website> findByUrl(String url);
    
    Optional<Website> findByUrlAndIsActiveTrue(String url);
    
    /**
     * 根据分类ID查找网站列表 - 保持向后兼容
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "JOIN w.categoryRelations cr " +
           "WHERE w.isActive = true AND cr.category.id = :categoryId")
    List<Website> findByCategoryIdAndIsActiveTrue(@Param("categoryId") Long categoryId);
    
    /**
     * 根据收藏状态查找网站列表
     */
    List<Website> findByIsFavoriteAndIsActiveTrue(Boolean isFavorite);
    
    /**
     * 根据分类ID查找网站（分页）- 保持向后兼容
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "JOIN w.categoryRelations cr " +
           "WHERE w.isActive = true AND cr.category.id = :categoryId")
    Page<Website> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * 根据多个分类ID查找网站（分页）- 新增多分类支持
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "JOIN w.categoryRelations cr " +
           "WHERE w.isActive = true AND cr.category.id IN :categoryIds")
    Page<Website> findByCategoryIds(@Param("categoryIds") List<Long> categoryIds, Pageable pageable);
    
    /**
     * 根据收藏状态查找网站（分页）
     */
    @Query("SELECT w FROM Website w WHERE w.isActive = true AND w.isFavorite = :isFavorite")
    Page<Website> findByIsFavorite(@Param("isFavorite") Boolean isFavorite, Pageable pageable);
    
    /**
     * 根据状态查找网站列表
     */
    List<Website> findByStatusAndIsActiveTrue(Website.WebsiteStatus status);
    
    /**
     * 根据关键词搜索网站（名称、描述、URL）
     */
    @Query("SELECT w FROM Website w WHERE w.isActive = true AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Website> findByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    /**
     * 根据分类ID和关键词搜索网站 - 保持向后兼容
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "JOIN w.categoryRelations cr " +
           "WHERE w.isActive = true AND cr.category.id = :categoryId AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Website> findByCategoryIdAndKeyword(@Param("categoryId") Long categoryId, 
                                           @Param("keyword") String keyword, 
                                           Pageable pageable);
    
    /**
     * 根据多个分类ID和关键词搜索网站 - 新增多分类支持
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "JOIN w.categoryRelations cr " +
           "WHERE w.isActive = true AND cr.category.id IN :categoryIds AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Website> findByCategoryIdsAndKeyword(@Param("categoryIds") List<Long> categoryIds, 
                                             @Param("keyword") String keyword, 
                                             Pageable pageable);
    
    /**
     * 根据收藏状态和关键词搜索网站
     */
    @Query("SELECT w FROM Website w WHERE w.isActive = true AND w.isFavorite = :isFavorite AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Website> findByIsFavoriteAndKeyword(@Param("isFavorite") Boolean isFavorite, 
                                            @Param("keyword") String keyword, 
                                            Pageable pageable);
    
    /**
     * 根据时间范围查找网站
     */
    @Query("SELECT w FROM Website w WHERE w.isActive = true AND w.createdAt BETWEEN :startTime AND :endTime")
    Page<Website> findByCreatedAtBetween(@Param("startTime") LocalDateTime startTime, 
                                        @Param("endTime") LocalDateTime endTime, 
                                        Pageable pageable);
    
    /**
     * 查找访问次数最多的网站
     */
    @Query("SELECT w FROM Website w WHERE w.isActive = true ORDER BY w.visitCount DESC")
    List<Website> findTopWebsitesByVisitCount(Pageable pageable);
    
    /**
     * 查找最近创建的网站
     */
    @Query("SELECT w FROM Website w WHERE w.isActive = true ORDER BY w.createdAt DESC")
    List<Website> findRecentWebsites(Pageable pageable);
    
    /**
     * 统计分类下的网站数量 - 保持向后兼容
     */
    @Query("SELECT COUNT(DISTINCT w) FROM Website w " +
           "JOIN w.categoryRelations cr " +
           "WHERE cr.category.id = :categoryId AND w.isActive = true")
    Long countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 按分类统计活跃网站数量（分组）- 保持向后兼容
     */
    @Query("SELECT cr.category.id, COUNT(DISTINCT w.id) FROM Website w " +
           "JOIN w.categoryRelations cr " +
           "WHERE w.isActive = true GROUP BY cr.category.id")
    List<Object[]> countActiveGroupByCategory();
    
    /**
     * 统计收藏网站数量
     */
    @Query("SELECT COUNT(w) FROM Website w WHERE w.isFavorite = true AND w.isActive = true")
    Long countFavoriteWebsites();
    
    /**
     * 统计活跃网站数量
     */
    @Query("SELECT COUNT(w) FROM Website w WHERE w.isActive = true")
    Long countActiveWebsites();
    

    
    /**
     * 查找需要检查状态的网站（超过指定天数未检查）
     */
    @Query("SELECT w FROM Website w WHERE w.isActive = true AND " +
           "(w.lastCheckTime IS NULL OR w.lastCheckTime < :checkTime)")
    List<Website> findWebsitesNeedingStatusCheck(@Param("checkTime") LocalDateTime checkTime);
    
    /**
     * 更新网站访问次数
     */
    @Query("UPDATE Website w SET w.visitCount = w.visitCount + 1, w.updatedAt = CURRENT_TIMESTAMP WHERE w.id = :id")
    void incrementVisitCount(@Param("id") Long id);
    
    /**
     * 切换网站收藏状态
     */
    @Query("UPDATE Website w SET w.isFavorite = CASE WHEN w.isFavorite = true THEN false ELSE true END, w.updatedAt = CURRENT_TIMESTAMP WHERE w.id = :id")
    void toggleFavorite(@Param("id") Long id);
    
    /**
     * 批量更新网站状态
     */
    @Query("UPDATE Website w SET w.status = :status, w.updatedAt = CURRENT_TIMESTAMP WHERE w.id IN :ids")
    void updateStatusByIds(@Param("ids") List<Long> ids, @Param("status") Website.WebsiteStatus status);
    
    /**
     * 批量删除网站（软删除）
     */
    @Query("UPDATE Website w SET w.isActive = false, w.updatedAt = CURRENT_TIMESTAMP WHERE w.id IN :ids")
    void softDeleteByIds(@Param("ids") List<Long> ids);

    /**
     * 查询所有活跃网站（分页）
     */
    @Query("SELECT w FROM Website w WHERE w.isActive = true")
    Page<Website> findAllActive(Pageable pageable);
    
    // ========== 优化的查询方法，使用JOIN FETCH避免N+1问题 ==========
    
    /**
     * 查询所有活跃网站（分页）- 优化版本，预加载分类关系
     * 注意：使用JOIN FETCH时不能直接返回Page，需要手动处理分页
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true")
    List<Website> findAllActiveWithCategories();
    
    /**
     * 根据分类ID查找网站（分页）- 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND cr.category.id = :categoryId")
    List<Website> findByCategoryIdWithCategories(@Param("categoryId") Long categoryId);
    
    /**
     * 根据多个分类ID查找网站（分页）- 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND cr.category.id IN :categoryIds")
    List<Website> findByCategoryIdsWithCategories(@Param("categoryIds") List<Long> categoryIds);
    
    /**
     * 根据收藏状态查找网站（分页）- 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND w.isFavorite = :isFavorite")
    List<Website> findByIsFavoriteWithCategories(@Param("isFavorite") Boolean isFavorite);
    
    /**
     * 根据关键词搜索网站（分页）- 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Website> findByKeywordWithCategories(@Param("keyword") String keyword);
    
    /**
     * 根据分类ID和关键词搜索网站（分页）- 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND cr.category.id = :categoryId AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Website> findByCategoryIdAndKeywordWithCategories(@Param("categoryId") Long categoryId, 
                                                          @Param("keyword") String keyword);
    
    /**
     * 根据多个分类ID和关键词搜索网站（分页）- 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND cr.category.id IN :categoryIds AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Website> findByCategoryIdsAndKeywordWithCategories(@Param("categoryIds") List<Long> categoryIds, 
                                                           @Param("keyword") String keyword);
    
    /**
     * 根据收藏状态和关键词搜索网站（分页）- 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND w.isFavorite = :isFavorite AND " +
           "(LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(w.url) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Website> findByIsFavoriteAndKeywordWithCategories(@Param("isFavorite") Boolean isFavorite, 
                                                          @Param("keyword") String keyword);
    
    /**
     * 根据时间范围查找网站（分页）- 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND w.createdAt BETWEEN :startTime AND :endTime")
    List<Website> findByCreatedAtBetweenWithCategories(@Param("startTime") LocalDateTime startTime, 
                                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查找访问次数最多的网站 - 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true ORDER BY w.visitCount DESC")
    List<Website> findTopWebsitesByVisitCountWithCategories();
    
    /**
     * 查找最近创建的网站 - 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true ORDER BY w.createdAt DESC")
    List<Website> findRecentWebsitesWithCategories();
    
    /**
     * 根据分类ID查找网站列表 - 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND cr.category.id = :categoryId")
    List<Website> findByCategoryIdAndIsActiveTrueWithCategories(@Param("categoryId") Long categoryId);
    
    /**
     * 根据收藏状态查找网站列表 - 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.isActive = true AND w.isFavorite = :isFavorite")
    List<Website> findByIsFavoriteAndIsActiveTrueWithCategories(@Param("isFavorite") Boolean isFavorite);
    
    /**
     * 根据ID查找网站 - 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.id = :id")
    Optional<Website> findByIdWithCategories(@Param("id") Long id);
    
    /**
     * 根据URL查找网站 - 优化版本，预加载分类关系
     */
    @Query("SELECT DISTINCT w FROM Website w " +
           "LEFT JOIN FETCH w.categoryRelations cr " +
           "LEFT JOIN FETCH cr.category " +
           "WHERE w.url = :url")
    Optional<Website> findByUrlWithCategories(@Param("url") String url);
}
