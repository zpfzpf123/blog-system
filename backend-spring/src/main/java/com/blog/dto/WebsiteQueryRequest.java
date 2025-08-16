package com.blog.dto;

// import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 网站查询请求DTO
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
public class WebsiteQueryRequest {
    
    private String keyword;
    private Long categoryId; // 保持向后兼容
    private List<Long> categoryIds; // 新增多分类支持
    private Boolean isFavorite;
    private Boolean isActive;
    private String status;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private Integer page = 1;
    private Integer size = 12;
    
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    // 构造函数
    public WebsiteQueryRequest() {}
    
    // Getter和Setter方法
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public List<Long> getCategoryIds() {
        return categoryIds;
    }
    
    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
    
    public Boolean getIsFavorite() {
        return isFavorite;
    }
    
    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
    
    public String getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    // 业务方法
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    public boolean hasCategoryFilter() {
        return categoryId != null || (categoryIds != null && !categoryIds.isEmpty());
    }
    
    public boolean hasFavoriteFilter() {
        return isFavorite != null;
    }
    
    public boolean hasActiveFilter() {
        return isActive != null;
    }
    
    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty();
    }
    
    public boolean hasTimeRange() {
        return startTime != null && endTime != null;
    }
    
    public int getOffset() {
        return (page - 1) * size;
    }
    
    @Override
    public String toString() {
        return "WebsiteQueryRequest{" +
                "keyword='" + keyword + '\'' +
                ", categoryId=" + categoryId +
                ", categoryIds=" + categoryIds +
                ", isFavorite=" + isFavorite +
                ", isActive=" + isActive +
                ", status='" + status + '\'' +
                ", sortBy='" + sortBy + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                ", page=" + page +
                ", size=" + size +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}