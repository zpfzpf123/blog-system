package com.blog.service;

import com.blog.entity.GitUser;
import com.blog.repository.GitUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Git用户服务类
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@Service
public class GitUserService {
    
    @Autowired
    private GitUserRepository gitUserRepository;
    
    /**
     * 获取所有Git用户
     */
    public List<GitUser> getAllGitUsers() {
        return gitUserRepository.findAll();
    }
    
    /**
     * 根据ID获取Git用户
     */
    public Optional<GitUser> getGitUserById(Long id) {
        return gitUserRepository.findById(id);
    }
    
    /**
     * 获取默认Git用户
     */
    public Optional<GitUser> getDefaultGitUser() {
        return gitUserRepository.findByIsDefaultTrue();
    }
    
    /**
     * 创建Git用户
     */
    @Transactional
    public GitUser createGitUser(GitUser gitUser) {
        // 如果设置为默认，先取消其他默认用户
        if (gitUser.getIsDefault() != null && gitUser.getIsDefault()) {
            clearDefaultUsers();
        }
        return gitUserRepository.save(gitUser);
    }
    
    /**
     * 更新Git用户
     */
    @Transactional
    public GitUser updateGitUser(Long id, GitUser gitUser) {
        GitUser existingUser = gitUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Git用户不存在"));
        
        // 如果设置为默认，先取消其他默认用户
        if (gitUser.getIsDefault() != null && gitUser.getIsDefault()) {
            clearDefaultUsers();
        }
        
        existingUser.setName(gitUser.getName());
        existingUser.setUsername(gitUser.getUsername());
        existingUser.setPassword(gitUser.getPassword());
        existingUser.setEmail(gitUser.getEmail());
        existingUser.setDescription(gitUser.getDescription());
        existingUser.setIsDefault(gitUser.getIsDefault());
        
        return gitUserRepository.save(existingUser);
    }
    
    /**
     * 删除Git用户
     */
    @Transactional
    public void deleteGitUser(Long id) {
        gitUserRepository.deleteById(id);
    }
    
    /**
     * 设置默认Git用户
     */
    @Transactional
    public void setDefaultGitUser(Long id) {
        // 先取消所有默认用户
        clearDefaultUsers();
        
        // 设置指定用户为默认
        GitUser user = gitUserRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Git用户不存在"));
        user.setIsDefault(true);
        gitUserRepository.save(user);
    }
    
    /**
     * 清除所有默认用户标记
     */
    private void clearDefaultUsers() {
        List<GitUser> allUsers = gitUserRepository.findAll();
        for (GitUser user : allUsers) {
            if (user.getIsDefault() != null && user.getIsDefault()) {
                user.setIsDefault(false);
                gitUserRepository.save(user);
            }
        }
    }
}
