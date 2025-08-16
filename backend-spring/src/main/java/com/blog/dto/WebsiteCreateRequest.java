package com.blog.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.util.List;
import java.util.ArrayList;

/**
 * 网站创建请求DTO
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
public class WebsiteCreateRequest {
    
    @NotBlank(message = "网站名称不能为空")
    private String name;
    
    // 移除URL长度限制，允许空字符串
    private String url;
    
    @NotBlank(message = "网站描述不能为空")
    private String description;
    
    @NotNull(message = "分类ID不能为空")
    private List<Long> categoryIds;
    
    // 移除所有长度限制
    private String icon;
    
    private String favicon;
    
    private String screenshot;
    
    private Boolean isFavorite = false;
    
    // 构造函数
    public WebsiteCreateRequest() {}
    
    public WebsiteCreateRequest(String name, String url, String description, List<Long> categoryIds) {
        this.name = name;
        this.url = url;
        this.description = description;
        this.categoryIds = categoryIds;
    }
    
    // Getter和Setter方法
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<Long> getCategoryIds() {
        return categoryIds;
    }
    
    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
    
    // 保持向后兼容的方法
    public Long getCategoryId() {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            return categoryIds.get(0);
        }
        return null;
    }
    
    public void setCategoryId(Long categoryId) {
        if (categoryIds == null) {
            categoryIds = new ArrayList<>();
        }
        categoryIds.clear();
        if (categoryId != null) {
            categoryIds.add(categoryId);
        }
    }
    
    public String getIcon() {
        return icon;
    }
    
    public void setIcon(String icon) {
        this.icon = icon;
    }
    
    public String getFavicon() {
        return favicon;
    }
    
    public void setFavicon(String favicon) {
        this.favicon = favicon;
    }
    
    public String getScreenshot() {
        return screenshot;
    }
    
    public void setScreenshot(String screenshot) {
        this.screenshot = screenshot;
    }
    
    public Boolean getIsFavorite() {
        return isFavorite;
    }
    
    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
    
    @Override
    public String toString() {
        return "WebsiteCreateRequest{" +
                "name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", categoryIds=" + categoryIds +
                ", icon='" + icon + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }
}