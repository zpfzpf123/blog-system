package com.blog.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目API接口实体类
 */
@Entity
@Table(name = "project_apis")
public class ProjectApi {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "method", length = 20)
    private String method; // GET, POST, PUT, DELETE, PATCH
    
    @Column(name = "path", nullable = false, length = 500)
    private String path;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "request_headers", columnDefinition = "TEXT")
    private String requestHeaders; // JSON格式
    
    @Column(name = "request_params", columnDefinition = "TEXT")
    private String requestParams; // JSON格式
    
    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody; // JSON格式
    
    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody; // JSON格式
    
    @Column(name = "mock_enabled")
    private Boolean mockEnabled = false;
    
    @Column(name = "mock_data", columnDefinition = "LONGTEXT")
    private String mockData; // Mock响应数据
    
    @Column(name = "mock_delay")
    private Integer mockDelay = 0; // Mock延迟(ms)
    
    @Column(name = "mock_status_code")
    private Integer mockStatusCode = 200;
    
    @Column(name = "status", length = 20)
    private String status = "pending"; // pending, developing, testing, completed
    
    @Column(name = "category", length = 100)
    private String category; // 接口分类
    
    @Column(name = "tags", length = 500)
    private String tags; // 标签，逗号分隔
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
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
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    
    public String getRequestParams() { return requestParams; }
    public void setRequestParams(String requestParams) { this.requestParams = requestParams; }
    
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    
    public Boolean getMockEnabled() { return mockEnabled; }
    public void setMockEnabled(Boolean mockEnabled) { this.mockEnabled = mockEnabled; }
    
    public String getMockData() { return mockData; }
    public void setMockData(String mockData) { this.mockData = mockData; }
    
    public Integer getMockDelay() { return mockDelay; }
    public void setMockDelay(Integer mockDelay) { this.mockDelay = mockDelay; }
    
    public Integer getMockStatusCode() { return mockStatusCode; }
    public void setMockStatusCode(Integer mockStatusCode) { this.mockStatusCode = mockStatusCode; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
