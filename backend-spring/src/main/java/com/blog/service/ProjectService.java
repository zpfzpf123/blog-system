package com.blog.service;

import com.blog.entity.Project;

import java.util.List;
import java.util.Optional;

/**
 * 项目服务接口
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
public interface ProjectService {
    
    /**
     * 获取所有项目
     */
    List<Project> getAllProjects();
    
    /**
     * 根据ID获取项目
     */
    Optional<Project> getProjectById(Long id);
    
    /**
     * 创建项目
     */
    Project createProject(Project project);
    
    /**
     * 更新项目
     */
    Project updateProject(Long id, Project project);
    
    /**
     * 删除项目
     */
    void deleteProject(Long id);
    
    /**
     * 按状态查找项目
     */
    List<Project> getProjectsByStatus(String status);
    
    /**
     * 查找收藏的项目
     */
    List<Project> getFavoriteProjects();
}
