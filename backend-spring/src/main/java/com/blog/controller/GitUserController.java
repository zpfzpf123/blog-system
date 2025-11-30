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
 * 提供Git账号的增删改查和默认账号设置功能
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
     * 
     * @return Git用户列表
     */
    @GetMapping
    public ResponseEntity<List<GitUser>> getAllGitUsers() {
        try {
            List<GitUser> gitUsers = gitUserService.getAllGitUsers();
            return ResponseEntity.ok(gitUsers);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 根据ID获取Git用户
     * GET /api/git-users/{id}
     * 
     * @param id Git用户ID
     * @return Git用户
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getGitUserById(@PathVariable Long id) {
        try {
            return gitUserService.getGitUserById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取Git用户失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 获取默认Git用户
     * GET /api/git-users/default/user
     * 
     * @return 默认Git用户
     */
    @GetMapping("/default/user")
    public ResponseEntity<?> getDefaultGitUser() {
        try {
            return gitUserService.getDefaultGitUser()
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "获取默认Git用户失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 创建Git用户
     * POST /api/git-users
     * 
     * @param gitUser Git用户对象
     * @return 创建的Git用户
     */
    @PostMapping
    public ResponseEntity<?> createGitUser(@RequestBody GitUser gitUser) {
        try {
            GitUser created = gitUserService.createGitUser(gitUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "创建Git用户失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 更新Git用户
     * PUT /api/git-users/{id}
     * 
     * @param id Git用户ID
     * @param gitUser 更新的Git用户信息
     * @return 更新后的Git用户
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGitUser(@PathVariable Long id, @RequestBody GitUser gitUser) {
        try {
            GitUser updated = gitUserService.updateGitUser(id, gitUser);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "更新Git用户失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 删除Git用户
     * DELETE /api/git-users/{id}
     * 
     * @param id Git用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGitUser(@PathVariable Long id) {
        try {
            gitUserService.deleteGitUser(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "删除成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "删除Git用户失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    /**
     * 设置为默认Git用户
     * POST /api/git-users/{id}/set-default
     * 
     * @param id Git用户ID
     * @return 设置后的Git用户
     */
    @PostMapping("/{id}/set-default")
    public ResponseEntity<?> setAsDefault(@PathVariable Long id) {
        try {
            GitUser updated = gitUserService.setAsDefault(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("message", "设置默认账号失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
