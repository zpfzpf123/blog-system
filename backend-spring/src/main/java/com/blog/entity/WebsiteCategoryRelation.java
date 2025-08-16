/*
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-15 11:18:35
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-08-15 12:16:03
 * @FilePath: \blog\backend-spring\src\main\java\com\blog\entity\WebsiteCategoryRelation.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.blog.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 网站分类关系中间表实体类
 * 用于管理网站和分类的多对多关系
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@Entity
@Table(name = "website_category_relations")
public class WebsiteCategoryRelation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "website_id", nullable = false)
    private Website website;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private WebsiteCategory category;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    // 构造函数
    public WebsiteCategoryRelation() {
        this.createdAt = LocalDateTime.now();
    }
    
    public WebsiteCategoryRelation(Website website, WebsiteCategory category) {
        this();
        this.website = website;
        this.category = category;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Website getWebsite() {
        return website;
    }
    
    public void setWebsite(Website website) {
        this.website = website;
    }
    
    public WebsiteCategory getCategory() {
        return category;
    }
    
    public void setCategory(WebsiteCategory category) {
        this.category = category;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public Long getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
    
    @Override
    public String toString() {
        return "WebsiteCategoryRelation{" +
                "id=" + id +
                ", websiteId=" + (website != null ? website.getId() : "null") +
                ", categoryId=" + (category != null ? category.getId() : "null") +
                ", createdAt=" + createdAt +
                '}';
    }
}
