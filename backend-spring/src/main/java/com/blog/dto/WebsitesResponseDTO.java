package com.blog.dto;

import java.util.List;

/**
 * 网站列表响应DTO
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
public class WebsitesResponseDTO {
    
    private List<WebsiteDTO> websites;
    private PaginationDTO pagination;
    private Long totalCount;
    private Long totalPages;
    
    // 构造函数
    public WebsitesResponseDTO() {}
    
    public WebsitesResponseDTO(List<WebsiteDTO> websites, PaginationDTO pagination, Long totalCount) {
        this.websites = websites;
        this.pagination = pagination;
        this.totalCount = totalCount;
        this.totalPages = (long) Math.ceil((double) totalCount / pagination.getPageSize());
    }
    
    // Getter和Setter方法
    public List<WebsiteDTO> getWebsites() {
        return websites;
    }
    
    public void setWebsites(List<WebsiteDTO> websites) {
        this.websites = websites;
    }
    
    public PaginationDTO getPagination() {
        return pagination;
    }
    
    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }
    
    public Long getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
    
    public Long getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Long totalPages) {
        this.totalPages = totalPages;
    }
    
    @Override
    public String toString() {
        return "WebsitesResponseDTO{" +
                "websites=" + websites +
                ", pagination=" + pagination +
                ", totalCount=" + totalCount +
                ", totalPages=" + totalPages +
                '}';
    }
}
