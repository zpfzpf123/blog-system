package com.blog.controller;

import com.blog.entity.GitUser;
import com.blog.service.GitUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Git用户管理控制器
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@RestController
@RequestMapping("/api/git-users")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"})
public class GitUserController {
    
    @Autowired
    private GitUserService gitUserService;
    
    /**
     * 获取所有Git用户
     * GET /api/git-users
     */
    @GetMapping
    public ResponseEntity<List<GitUser>> getAllGitUsers() {
        System.out.println("=== 获取所有Git用户 ===");
        List<GitUser> users = gitUserService.getAllGitUsers();
        System.out.println("用户数量: " + users.size());
        return ResponseEntity.ok(users);
    }
    
    /**
     * 根据ID获取Git用户
     * GET /api/git-users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<GitUser> getGitUserById(@PathVariable Long id) {
        System.out.println("=== 获取Git用户详情 ===");
        System.out.println("用户ID: " + id);
        return gitUserService.getGitUserById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 获取默认Git用户
     * GET /api/git-users/default
     */
    @GetMapping("/default/user")
    public ResponseEntity<GitUser> getDefaultGitUser() {
        System.out.println("=== 获取默认Git用户 ===");
        return gitUserService.getDefaultGitUser()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建Git用户
     * POST /api/git-users
     */
    @PostMapping
    public ResponseEntity<GitUser> createGitUser(@RequestBody GitUser gitUser) {
        System.out.println("=== 创建Git用户 ===");
        System.out.println("用户名: " + gitUser.getName());
        
        try {
            GitUser created = gitUserService.createGitUser(gitUser);
            System.out.println("Git用户创建成功，ID: " + created.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            System.err.println("创建Git用户失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 更新Git用户
     * PUT /api/git-users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<GitUser> updateGitUser(@PathVariable Long id, @RequestBody GitUser gitUser) {
        System.out.println("=== 更新Git用户 ===");
        System.out.println("用户ID: " + id);
        
        try {
            GitUser updated = gitUserService.updateGitUser(id, gitUser);
            System.out.println("Git用户更新成功");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            System.err.println("更新Git用户失败: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("更新Git用户失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 删除Git用户
     * DELETE /api/git-users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGitUser(@PathVariable Long id) {
        System.out.println("=== 删除Git用户 ===");
        System.out.println("用户ID: " + id);
        
        try {
            gitUserService.deleteGitUser(id);
            System.out.println("Git用户删除成功");
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            System.err.println("删除Git用户失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 设置默认Git用户
     * POST /api/git-users/{id}/set-default
     */
    @PostMapping("/{id}/set-default")
    public ResponseEntity<Map<String, String>> setDefaultGitUser(@PathVariable Long id) {
        System.out.println("=== 设置默认Git用户 ===");
        System.out.println("用户ID: " + id);
        
        try {
            gitUserService.setDefaultGitUser(id);
            System.out.println("默认Git用户设置成功");
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "默认Git用户设置成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("设置默认Git用户失败: " + e.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            System.err.println("设置默认Git用户失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
