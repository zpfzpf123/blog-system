package com.blog.service.impl;

import com.blog.entity.Project;
import com.blog.repository.ProjectRepository;
import com.blog.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 项目服务实现类
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@Service
@Transactional
public class ProjectServiceImpl implements ProjectService {
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    @Override
    public Optional<Project> getProjectById(Long id) {
        return projectRepository.findById(id);
    }
    
    @Override
    public Project createProject(Project project) {
        if (project.getProgress() == null) {
            project.setProgress(0);
        }
        if (project.getStatus() == null || project.getStatus().isEmpty()) {
            project.setStatus("进行中");
        }
        if (project.getIsFavorite() == null) {
            project.setIsFavorite(false);
        }
        return projectRepository.save(project);
    }
    
    @Override
    public Project updateProject(Long id, Project project) {
        Project existingProject = projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("项目不存在：" + id));
        
        // 更新字段
        existingProject.setName(project.getName());
        existingProject.setDescription(project.getDescription());
        existingProject.setCoverImage(project.getCoverImage());
        existingProject.setStatus(project.getStatus());
        existingProject.setProgress(project.getProgress());
        existingProject.setTechStack(project.getTechStack());
        existingProject.setLocalPath(project.getLocalPath());
        existingProject.setRepoUrl(project.getRepoUrl());
        existingProject.setApiBaseUrl(project.getApiBaseUrl());
        existingProject.setReadmeContent(project.getReadmeContent());
        existingProject.setGitCommits(project.getGitCommits());
        existingProject.setIsFavorite(project.getIsFavorite());
        existingProject.setSortOrder(project.getSortOrder());
        
        return projectRepository.save(existingProject);
    }
    
    @Override
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("项目不存在：" + id);
        }
        projectRepository.deleteById(id);
    }
    
    @Override
    public List<Project> getProjectsByStatus(String status) {
        return projectRepository.findByStatus(status);
    }
    
    @Override
    public List<Project> getFavoriteProjects() {
        return projectRepository.findByIsFavorite(true);
    }
}
