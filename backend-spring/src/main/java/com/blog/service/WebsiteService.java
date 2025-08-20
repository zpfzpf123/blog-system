package com.blog.service;

import com.blog.dto.WebsiteCreateRequest;
import com.blog.dto.WebsiteDTO;
import com.blog.dto.WebsiteQueryRequest;
import com.blog.dto.WebsitesResponseDTO;
import com.blog.dto.WebsiteScrapeResponse;
import com.blog.entity.Website;

import java.util.List;
import java.util.Optional;

/**
 * 网站业务服务接口
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
public interface WebsiteService {
    
    /**
     * 创建网站
     */
    WebsiteDTO createWebsite(WebsiteCreateRequest request);
    
    /**
     * 根据ID查找网站
     */
    Optional<WebsiteDTO> findWebsiteById(Long id);
    
    /**
     * 根据URL查找网站
     */
    Optional<WebsiteDTO> findWebsiteByUrl(String url);
    
    /**
     * 更新网站
     */
    WebsiteDTO updateWebsite(Long id, WebsiteCreateRequest request);
    
    /**
     * 删除网站
     */
    void deleteWebsite(Long id);
    
    /**
     * 批量删除网站
     */
    void deleteWebsites(List<Long> ids);
    
    /**
     * 查询网站列表（支持分页、搜索、筛选）
     */
    WebsitesResponseDTO findWebsites(WebsiteQueryRequest request);
    
    /**
     * 获取所有网站（用于状态监控）
     */
    List<WebsiteDTO> findAllWebsites();
    
    /**
     * 根据分类ID查找网站列表
     */
    List<WebsiteDTO> findWebsitesByCategory(Long categoryId);
    
    /**
     * 根据收藏状态查找网站列表
     */
    List<WebsiteDTO> findWebsitesByFavorite(Boolean isFavorite);
    
    /**
     * 根据关键词搜索网站
     */
    List<WebsiteDTO> searchWebsites(String keyword);
    

    
    /**
     * 获取热门网站（按访问次数排序）
     */
    List<WebsiteDTO> getPopularWebsites(int limit);
    
    /**
     * 获取最新网站
     */
    List<WebsiteDTO> getRecentWebsites(int limit);
    

    
    /**
     * 切换网站收藏状态
     */
    void toggleFavorite(Long id);
    
    /**
     * 检查网站状态
     */
    void checkWebsiteStatus(Long id);
    
    /**
     * 批量检查网站状态
     */
    void batchCheckWebsiteStatus();
    
    /**
     * 获取网站统计信息
     */
    WebsiteStatistics getWebsiteStatistics();
    
    /**
     * 导入网站数据
     */
    ImportResult importWebsites(List<WebsiteCreateRequest> requests);
    
    /**
     * 导出网站数据
     */
    List<WebsiteDTO> exportWebsites(WebsiteQueryRequest request);
    
    /**
     * 抓取网站信息
     */
    WebsiteScrapeResponse scrapeWebsiteInfo(String url);
    
    /**
     * 网站统计信息
     */
    class WebsiteStatistics {
        private Long totalWebsites;
        private Long totalCategories;


        private Long favoriteWebsites;
        private Long activeWebsites;
        
        // 构造函数、getter和setter方法
        public WebsiteStatistics() {}
        
        public WebsiteStatistics(Long totalWebsites, Long totalCategories, 
                               Long favoriteWebsites, Long activeWebsites) {
            this.totalWebsites = totalWebsites;
            this.totalCategories = totalCategories;
            this.favoriteWebsites = favoriteWebsites;
            this.activeWebsites = activeWebsites;
        }
        
        // Getter和Setter方法
        public Long getTotalWebsites() { return totalWebsites; }
        public void setTotalWebsites(Long totalWebsites) { this.totalWebsites = totalWebsites; }
        
        public Long getTotalCategories() { return totalCategories; }
        public void setTotalCategories(Long totalCategories) { this.totalCategories = totalCategories; }
        

        

        
        public Long getFavoriteWebsites() { return favoriteWebsites; }
        public void setFavoriteWebsites(Long favoriteWebsites) { this.favoriteWebsites = favoriteWebsites; }
        
        public Long getActiveWebsites() { return activeWebsites; }
        public void setActiveWebsites(Long activeWebsites) { this.activeWebsites = activeWebsites; }
    }
    
    /**
     * 导入结果
     */
    class ImportResult {
        private int totalCount;
        private int successCount;
        private int failedCount;
        private List<String> errors;
        
        // 构造函数、getter和setter方法
        public ImportResult() {}
        
        public ImportResult(int totalCount, int successCount, int failedCount, List<String> errors) {
            this.totalCount = totalCount;
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.errors = errors;
        }
        
        // Getter和Setter方法
        public int getTotalCount() { return totalCount; }
        public void setTotalCount(int totalCount) { this.totalCount = totalCount; }
        
        public int getSuccessCount() { return successCount; }
        public void setSuccessCount(int successCount) { this.successCount = successCount; }
        
        public int getFailedCount() { return failedCount; }
        public void setFailedCount(int failedCount) { this.failedCount = failedCount; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
    }
}
