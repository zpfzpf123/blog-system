package com.blog.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 网站实体类
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@Entity
@Table(name = "websites")
public class Website {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "url", nullable = false, unique = true)
    private String url;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    // 移除单分类关系，改为多对多关系
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "category_id", nullable = false)
    // private WebsiteCategory category;
    
    // 添加多对多关系映射
    @OneToMany(mappedBy = "website", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WebsiteCategoryRelation> categoryRelations = new ArrayList<>();
    
    // 便利方法：获取分类ID列表
    public List<Long> getCategoryIds() {
        return categoryRelations.stream()
                .map(relation -> relation.getCategory().getId())
                .collect(Collectors.toList());
    }
    
    // 便利方法：获取分类名称列表
    public List<String> getCategoryNames() {
        return categoryRelations.stream()
                .map(relation -> relation.getCategory().getName())
                .collect(Collectors.toList());
    }
    
    // 便利方法：获取分类颜色列表
    public List<String> getCategoryColors() {
        return categoryRelations.stream()
                .map(relation -> relation.getCategory().getColor())
                .collect(Collectors.toList());
    }
    
    // 便利方法：添加分类
    public void addCategory(WebsiteCategory category) {
        WebsiteCategoryRelation relation = new WebsiteCategoryRelation(this, category);
        categoryRelations.add(relation);
    }
    
    // 便利方法：移除分类
    public void removeCategory(WebsiteCategory category) {
        categoryRelations.removeIf(relation -> relation.getCategory().getId().equals(category.getId()));
    }
    
    // 便利方法：清空所有分类
    public void clearCategories() {
        categoryRelations.clear();
    }
    
    // 便利方法：设置分类列表
    public void setCategories(List<WebsiteCategory> categories) {
        clearCategories();
        if (categories != null) {
            categories.forEach(this::addCategory);
        }
    }
    
    // 便利方法：强制清空所有分类关系（用于更新操作）
    public void forceClearCategories() {
        // 标记所有关系为删除状态
        categoryRelations.forEach(relation -> {
            // 这里可以设置删除标记，或者依赖JPA的级联删除
        });
        categoryRelations.clear();
    }
    
    @Column(name = "icon")
    private String icon;
    
    @Column(name = "favicon")
    private String favicon;
    
    @Column(name = "screenshot")
    private String screenshot;
    

    
    @Column(name = "is_favorite")
    private Boolean isFavorite;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "status", length = 20)
    @Convert(converter = WebsiteStatusConverter.class)
    private WebsiteStatus status;
    
    @Column(name = "last_check_time")
    private LocalDateTime lastCheckTime;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    

    
    // 网站状态枚举
    public enum WebsiteStatus {
        ACTIVE("active"),
        INACTIVE("inactive"),
        BROKEN("broken");
        
        private final String value;
        
        WebsiteStatus(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return this.value;
        }
        
        // 从字符串值创建枚举
        public static WebsiteStatus fromValue(String value) {
            if (value == null) {
                return null;
            }
            for (WebsiteStatus status : WebsiteStatus.values()) {
                if (status.value.equalsIgnoreCase(value)) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Unknown WebsiteStatus value: " + value);
        }
        
        @Override
        public String toString() {
            return this.value;
        }
    }
    
    // 构造函数
    public Website() {

        this.isFavorite = false;
        this.isActive = true;
        this.status = WebsiteStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Website(String name, String url, String description) {
        this();
        this.name = name;
        this.url = url;
        this.description = description;
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
    
    public List<WebsiteCategoryRelation> getCategoryRelations() {
        return categoryRelations;
    }
    
    public void setCategoryRelations(List<WebsiteCategoryRelation> categoryRelations) {
        this.categoryRelations = categoryRelations;
    }
    
    // 保持向后兼容的方法
    public WebsiteCategory getCategory() {
        if (categoryRelations != null && !categoryRelations.isEmpty()) {
            return categoryRelations.get(0).getCategory();
        }
        return null;
    }
    
    public void setCategory(WebsiteCategory category) {
        clearCategories();
        if (category != null) {
            addCategory(category);
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public WebsiteStatus getStatus() {
        return status;
    }
    
    public void setStatus(WebsiteStatus status) {
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
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    public Long getUpdatedBy() {
        return updatedBy;
    }
    
    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }
    

    
    // 业务方法
    
    public void toggleFavorite() {
        this.isFavorite = !this.isFavorite;
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "Website{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", description='" + description + '\'' +
                ", category=" + (getCategory() != null ? getCategory().getName() : "null") +
                ", icon='" + icon + '\'' +

                ", isFavorite=" + isFavorite +
                ", isActive=" + isActive +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
