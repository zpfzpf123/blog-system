package com.blog.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目实体类
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@Entity
@Table(name = "projects")
public class Project {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "cover_image", length = 500)
    private String coverImage;
    
    @Column(name = "status", length = 20)
    private String status = "进行中"; // 进行中/已完成/暂停/计划中
    
    @Column(name = "progress")
    private Integer progress = 0; // 0-100
    
    @Column(name = "tech_stack", columnDefinition = "TEXT")
    private String techStack; // JSON格式存储技术栈数组
    
    @Column(name = "local_path", length = 500)
    private String localPath;
    
    @Column(name = "repo_url", length = 500)
    private String repoUrl;
    
    @Column(name = "api_base_url", length = 500)
    private String apiBaseUrl; // API测试的基础URL，如 http://localhost:8080
    
    @Column(name = "api_access_token", length = 1000)
    private String apiAccessToken; // API访问令牌，用于接口测试时的Authorization
    
    @Column(name = "readme_content", columnDefinition = "LONGTEXT")
    private String readmeContent;
    
    @Column(name = "git_commits", columnDefinition = "LONGTEXT")
    private String gitCommits;
    
    @Column(name = "is_favorite")
    private Boolean isFavorite = false;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "git_user_id")
    private Long gitUserId; // 关联的Git用户ID
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
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
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getCoverImage() {
        return coverImage;
    }
    
    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    
    public String getTechStack() {
        return techStack;
    }
    
    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }
    
    public String getLocalPath() {
        return localPath;
    }
    
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    
    public String getRepoUrl() {
        return repoUrl;
    }
    
    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }
    
    public String getApiBaseUrl() {
        return apiBaseUrl;
    }
    
    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
    
    public String getApiAccessToken() {
        return apiAccessToken;
    }
    
    public void setApiAccessToken(String apiAccessToken) {
        this.apiAccessToken = apiAccessToken;
    }
    
    public String getReadmeContent() {
        return readmeContent;
    }
    
    public void setReadmeContent(String readmeContent) {
        this.readmeContent = readmeContent;
    }
    
    public String getGitCommits() {
        return gitCommits;
    }
    
    public void setGitCommits(String gitCommits) {
        this.gitCommits = gitCommits;
    }
    
    public Boolean getIsFavorite() {
        return isFavorite;
    }
    
    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }
    
    public Integer getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public Long getGitUserId() {
        return gitUserId;
    }
    
    public void setGitUserId(Long gitUserId) {
        this.gitUserId = gitUserId;
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
}
