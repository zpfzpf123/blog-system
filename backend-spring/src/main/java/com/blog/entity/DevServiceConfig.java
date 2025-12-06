package com.blog.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 项目服务配置实体类 - 用于开发环境管理
 */
@Entity
@Table(name = "project_services")
public class DevServiceConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "project_id", nullable = false)
    private Long projectId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "type", length = 50)
    private String type; // frontend, backend, database, other
    
    @Column(name = "start_command", length = 500)
    private String startCommand;
    
    @Column(name = "stop_command", length = 500)
    private String stopCommand;
    
    @Column(name = "working_directory", length = 500)
    private String workingDirectory;
    
    @Column(name = "port")
    private Integer port;
    
    @Column(name = "health_check_url", length = 500)
    private String healthCheckUrl;
    
    @Column(name = "env_variables", columnDefinition = "TEXT")
    private String envVariables; // JSON格式的环境变量
    
    @Column(name = "auto_restart")
    private Boolean autoRestart = false;
    
    @Column(name = "start_order")
    private Integer startOrder = 0; // 启动顺序
    
    @Column(name = "enabled")
    private Boolean enabled = true;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
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
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getStartCommand() { return startCommand; }
    public void setStartCommand(String startCommand) { this.startCommand = startCommand; }
    
    public String getStopCommand() { return stopCommand; }
    public void setStopCommand(String stopCommand) { this.stopCommand = stopCommand; }
    
    public String getWorkingDirectory() { return workingDirectory; }
    public void setWorkingDirectory(String workingDirectory) { this.workingDirectory = workingDirectory; }
    
    public Integer getPort() { return port; }
    public void setPort(Integer port) { this.port = port; }
    
    public String getHealthCheckUrl() { return healthCheckUrl; }
    public void setHealthCheckUrl(String healthCheckUrl) { this.healthCheckUrl = healthCheckUrl; }
    
    public String getEnvVariables() { return envVariables; }
    public void setEnvVariables(String envVariables) { this.envVariables = envVariables; }
    
    public Boolean getAutoRestart() { return autoRestart; }
    public void setAutoRestart(Boolean autoRestart) { this.autoRestart = autoRestart; }
    
    public Integer getStartOrder() { return startOrder; }
    public void setStartOrder(Integer startOrder) { this.startOrder = startOrder; }
    
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
