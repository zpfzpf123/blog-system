package com.blog.repository;

import com.blog.entity.GitUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Git用户数据访问层
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@Repository
public interface GitUserRepository extends JpaRepository<GitUser, Long> {
    
    /**
     * 根据用户名查找Git用户
     * 
     * @param username Git用户名
     * @return Git用户
     */
    Optional<GitUser> findByUsername(String username);
    
    /**
     * 查找默认Git用户
     * 
     * @return 默认Git用户
     */
    Optional<GitUser> findByIsDefaultTrue();
    
    /**
     * 检查用户名是否存在
     * 
     * @param username Git用户名
     * @return 是否存在
     */
    boolean existsByUsername(String username);
}
