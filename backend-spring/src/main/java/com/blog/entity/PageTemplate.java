package com.blog.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "page_templates")
public class PageTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category; // form, table, dashboard, card, layout, etc.

    @Column(name = "tech_stack")
    private String techStack; // 技术栈，如: Vue3+ElementPlus, React+AntDesign

    @Column(columnDefinition = "TEXT", nullable = false)
    private String htmlCode;

    @Column(columnDefinition = "TEXT")
    private String cssCode;

    @Column(columnDefinition = "TEXT")
    private String jsCode;

    private String thumbnailUrl;

    private Integer viewCount = 0;

    private Integer copyCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTechStack() { return techStack; }
    public void setTechStack(String techStack) { this.techStack = techStack; }

    public String getHtmlCode() { return htmlCode; }
    public void setHtmlCode(String htmlCode) { this.htmlCode = htmlCode; }

    public String getCssCode() { return cssCode; }
    public void setCssCode(String cssCode) { this.cssCode = cssCode; }

    public String getJsCode() { return jsCode; }
    public void setJsCode(String jsCode) { this.jsCode = jsCode; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getCopyCount() { return copyCount; }
    public void setCopyCount(Integer copyCount) { this.copyCount = copyCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
