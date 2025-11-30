package com.blog.service;

import com.blog.entity.GitUser;
import com.blog.repository.GitUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Git用户业务逻辑层
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
     * 
     * @return Git用户列表
     */
    public List<GitUser> getAllGitUsers() {
        return gitUserRepository.findAll();
    }
    
    /**
     * 根据ID获取Git用户
     * 
     * @param id Git用户ID
     * @return Git用户
     */
    public Optional<GitUser> getGitUserById(Long id) {
        return gitUserRepository.findById(id);
    }
    
    /**
     * 获取默认Git用户
     * 
     * @return 默认Git用户
     */
    public Optional<GitUser> getDefaultGitUser() {
        return gitUserRepository.findByIsDefaultTrue();
    }
    
    /**
     * 创建Git用户
     * 
     * @param gitUser Git用户对象
     * @return 创建的Git用户
     */
    @Transactional
    public GitUser createGitUser(GitUser gitUser) {
        // 检查用户名是否已存在
        if (gitUserRepository.existsByUsername(gitUser.getUsername())) {
            throw new RuntimeException("用户名已存在：" + gitUser.getUsername());
        }
        
        // 如果设置为默认账号，先取消其他账号的默认状态
        if (Boolean.TRUE.equals(gitUser.getIsDefault())) {
            clearDefaultStatus();
        }
        
        return gitUserRepository.save(gitUser);
    }
    
    /**
     * 更新Git用户
     * 
     * @param id Git用户ID
     * @param gitUser 更新的Git用户信息
     * @return 更新后的Git用户
     */
    @Transactional
    public GitUser updateGitUser(Long id, GitUser gitUser) {
        GitUser existingUser = gitUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Git用户不存在，ID: " + id));
        
        // 检查用户名是否与其他用户冲突
        if (!existingUser.getUsername().equals(gitUser.getUsername()) 
                && gitUserRepository.existsByUsername(gitUser.getUsername())) {
            throw new RuntimeException("用户名已存在：" + gitUser.getUsername());
        }
        
        // 如果设置为默认账号，先取消其他账号的默认状态
        if (Boolean.TRUE.equals(gitUser.getIsDefault())) {
            clearDefaultStatus();
        }
        
        // 更新字段
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
     * 
     * @param id Git用户ID
     */
    @Transactional
    public void deleteGitUser(Long id) {
        if (!gitUserRepository.existsById(id)) {
            throw new RuntimeException("Git用户不存在，ID: " + id);
        }
        gitUserRepository.deleteById(id);
    }
    
    /**
     * 设置为默认Git用户
     * 
     * @param id Git用户ID
     * @return 设置后的Git用户
     */
    @Transactional
    public GitUser setAsDefault(Long id) {
        GitUser gitUser = gitUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Git用户不存在，ID: " + id));
        
        // 取消所有用户的默认状态
        clearDefaultStatus();
        
        // 设置当前用户为默认
        gitUser.setIsDefault(true);
        return gitUserRepository.save(gitUser);
    }
    
    /**
     * 清除所有用户的默认状态
     */
    private void clearDefaultStatus() {
        List<GitUser> allUsers = gitUserRepository.findAll();
        for (GitUser user : allUsers) {
            if (Boolean.TRUE.equals(user.getIsDefault())) {
                user.setIsDefault(false);
                gitUserRepository.save(user);
            }
        }
    }
}
