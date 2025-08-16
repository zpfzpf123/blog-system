/*
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-14 10:23:30
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-08-15 11:16:09
 * @FilePath: \blog\backend-spring\src\main\java\com\blog\dto\WebsiteDTO.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.blog.dto;

import com.blog.entity.Website;
import com.blog.entity.WebsiteCategory;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 网站数据传输对象
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
public class WebsiteDTO {
    
    private Long id;
    private String name;
    private String url;
    private String description;
    // 保持向后兼容的字段
    private Long categoryId;
    private String categoryName;
    private String categoryColor;

    
    // 新增多分类支持字段
    private List<Long> categoryIds;
    private List<String> categoryNames;
    private List<String> categoryColors;
    private String icon;
    private String favicon;
    private String screenshot;
    private Long visitCount;
    private Boolean isFavorite;
    private Boolean isActive;
    private String status;
    private LocalDateTime lastCheckTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    

    
    // 构造函数
    public WebsiteDTO() {}
    
    public WebsiteDTO(Website website) {
        System.out.println("=== WebsiteDTO构造函数开始 ===");
        System.out.println("网站ID: " + website.getId());
        System.out.println("网站名称: " + website.getName());
        
        this.id = website.getId();
        this.name = website.getName();
        this.url = website.getUrl();
        this.description = website.getDescription();
        this.icon = website.getIcon();
        this.favicon = website.getFavicon();
        this.screenshot = website.getScreenshot();
        this.visitCount = website.getVisitCount();
        this.isFavorite = website.getIsFavorite();
        this.isActive = website.getIsActive();
        this.status = website.getStatus() != null ? website.getStatus().getValue() : null;
        this.lastCheckTime = website.getLastCheckTime();
        this.createdAt = website.getCreatedAt();
        this.updatedAt = website.getUpdatedAt();
        
        // 设置分类信息（保持向后兼容）
        System.out.println("检查单分类信息...");
        if (website.getCategory() != null) {
            WebsiteCategory category = website.getCategory();
            this.categoryId = category.getId();
            this.categoryName = category.getName();
            this.categoryColor = category.getColor();
            System.out.println("单分类信息设置: " + category.getName() + " (ID: " + category.getId() + ")");
        } else {
            System.out.println("没有单分类信息");
        }
        
        // 设置多分类信息
        System.out.println("获取多分类信息...");
        this.categoryIds = website.getCategoryIds();
        this.categoryNames = website.getCategoryNames();
        this.categoryColors = website.getCategoryColors();
        
        System.out.println("多分类IDs: " + this.categoryIds);
        System.out.println("多分类Names: " + this.categoryNames);
        System.out.println("多分类Colors: " + this.categoryColors);
        System.out.println("=== WebsiteDTO构造函数结束 ===");
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public String getCategoryColor() {
        return categoryColor;
    }
    
    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }
    
    
    
    // 多分类相关的getter和setter方法
    public List<Long> getCategoryIds() {
        return categoryIds;
    }
    
    public void setCategoryIds(List<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }
    
    public List<String> getCategoryNames() {
        return categoryNames;
    }
    
    public void setCategoryNames(List<String> categoryNames) {
        this.categoryNames = categoryNames;
    }
    
    public List<String> getCategoryColors() {
        return categoryColors;
    }
    
    public void setCategoryColors(List<String> categoryColors) {
        this.categoryColors = categoryColors;
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
    
    public Long getVisitCount() {
        return visitCount;
    }
    
    public void setVisitCount(Long visitCount) {
        this.visitCount = visitCount;
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
    
    public LocalDateTime getLastCheckTime() {
        return lastCheckTime;
    }
    
    public void setLastCheckTime(LocalDateTime lastCheckTime) {
        this.lastCheckTime = lastCheckTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    

    
    @Override
    public String toString() {
        return "WebsiteDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", icon='" + icon + '\'' +
                ", visitCount=" + visitCount +
                ", isFavorite=" + isFavorite +

                '}';
    }
}
