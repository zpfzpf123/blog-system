package com.blog.repository;

import com.blog.entity.GitUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Git用户Repository
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@Repository
public interface GitUserRepository extends JpaRepository<GitUser, Long> {
    
    /**
     * 查找默认账号
     */
    Optional<GitUser> findByIsDefaultTrue();
    
    /**
     * 根据用户名查找
     */
    Optional<GitUser> findByUsername(String username);
}
