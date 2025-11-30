package com.blog.controller;

import com.blog.entity.Project;
import com.blog.service.ProjectService;
import com.blog.service.GitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 项目管理控制器
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@RestController
@RequestMapping("/api/projects")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"})
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private GitService gitService;
    
    /**
     * 获取所有项目
     * GET /api/projects
     */
    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
    
    /**
     * 根据ID获取项目
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return projectService.getProjectById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * 创建项目
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<Project> createProject(@RequestBody Project project) {
        try {
            Project createdProject = projectService.createProject(project);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 更新项目
     * PUT /api/projects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable Long id, @RequestBody Project project) {
        try {
            Project updatedProject = projectService.updateProject(id, project);
            return ResponseEntity.ok(updatedProject);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 删除项目
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 按状态获取项目
     * GET /api/projects/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Project>> getProjectsByStatus(@PathVariable String status) {
        List<Project> projects = projectService.getProjectsByStatus(status);
        return ResponseEntity.ok(projects);
    }
    
    /**
     * 获取收藏的项目
     * GET /api/projects/favorites
     */
    @GetMapping("/favorites")
    public ResponseEntity<List<Project>> getFavoriteProjects() {
        List<Project> projects = projectService.getFavoriteProjects();
        return ResponseEntity.ok(projects);
    }
    
    /**
     * 打开项目本地文件夹
     * POST /api/projects/{id}/open-folder
     */
    @PostMapping("/{id}/open-folder")
    public ResponseEntity<Map<String, String>> openLocalFolder(@PathVariable Long id) {
        Map<String, String> response = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 创建File对象
            File folder = new File(localPath);
            
            // 检查路径是否存在
            if (!folder.exists()) {
                response.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 直接打开指定路径（无论是文件夹还是文件）
            // 如果是文件，Windows会在资源管理器中高亮显示该文件
            
            // 根据操作系统打开文件夹
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows系统：直接打开文件夹（不使用/select参数）
                Runtime.getRuntime().exec("explorer.exe \"" + folder.getAbsolutePath() + "\"");
            } else if (os.contains("mac")) {
                // macOS系统：使用open命令
                Runtime.getRuntime().exec(new String[]{"open", folder.getAbsolutePath()});
            } else if (os.contains("nix") || os.contains("nux")) {
                // Linux系统：尝试使用xdg-open
                Runtime.getRuntime().exec(new String[]{"xdg-open", folder.getAbsolutePath()});
            } else {
                // 其他系统：尝试使用Desktop类
                if (Desktop.isDesktopSupported()) {
                    Desktop desktop = Desktop.getDesktop();
                    desktop.open(folder);
                } else {
                    response.put("message", "当前操作系统不支持自动打开文件夹");
                    return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(response);
                }
            }
            
            response.put("message", "文件夹已打开");
            response.put("path", folder.getAbsolutePath());
            return ResponseEntity.ok(response);
            
        } catch (IOException e) {
            e.printStackTrace();
            response.put("message", "打开文件夹失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (RuntimeException e) {
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "未知错误: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 检查Git状态
     * GET /api/projects/{id}/git-status
     */
    @GetMapping("/{id}/git-status")
    public ResponseEntity<Map<String, Object>> checkGitStatus(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.checkGitStatus(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "检查Git状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Fetch远程更新
     * POST /api/projects/{id}/git-fetch
     */
    @PostMapping("/{id}/git-fetch")
    public ResponseEntity<Map<String, Object>> gitFetch(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.fetchFromRemote(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Fetch失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 拉取远程代码（使用rebase）
     * POST /api/projects/{id}/git-pull
     */
    @PostMapping("/{id}/git-pull")
    public ResponseEntity<Map<String, Object>> gitPull(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.pullFromRemote(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Pull失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取冲突文件列表
     * GET /api/projects/{id}/git-conflicts
     */
    @GetMapping("/{id}/git-conflicts")
    public ResponseEntity<Map<String, Object>> getConflicts(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<String> conflictFiles = gitService.getConflictFiles(localPath);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("conflictFiles", conflictFiles);
            response.put("count", conflictFiles.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取冲突文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 解决冲突
     * POST /api/projects/{id}/git-resolve-conflict
     */
    @PostMapping("/{id}/git-resolve-conflict")
    public ResponseEntity<Map<String, Object>> resolveConflict(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String filePath = body.get("filePath");
            String strategy = body.get("strategy"); // ours / theirs
            
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.resolveConflict(localPath, filePath, strategy);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "解决冲突失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 继续Rebase
     * POST /api/projects/{id}/git-rebase-continue
     */
    @PostMapping("/{id}/git-rebase-continue")
    public ResponseEntity<Map<String, Object>> continueRebase(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.continueRebase(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "继续Rebase失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 中止Rebase
     * POST /api/projects/{id}/git-rebase-abort
     */
    @PostMapping("/{id}/git-rebase-abort")
    public ResponseEntity<Map<String, Object>> abortRebase(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.abortRebase(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "中止Rebase失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 生成建议的提交信息
     * POST /api/projects/{id}/git-generate-message
     */
    @PostMapping("/{id}/git-generate-message")
    public ResponseEntity<Map<String, Object>> generateCommitMessage(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取状态信息
            Map<String, Object> statusInfo = (Map<String, Object>) body.get("statusInfo");
            if (statusInfo == null) {
                statusInfo = gitService.checkGitStatus(localPath);
            }
            
            // 生成提交信息
            String message = gitService.generateCommitMessage(statusInfo);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "生成提交信息失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 智能提交代码（支持文件选择、自定义提交信息、分支选择和自动重试）
     * POST /api/projects/{id}/git-commit
     */
    @PostMapping("/{id}/git-commit")
    public ResponseEntity<Map<String, Object>> smartGitCommit(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取请求参数
            String commitMessage = null;
            List<String> selectedFiles = null;
            String targetBranch = null;
            boolean shouldPush = true;
            int maxRetries = 0;
            
            if (body != null) {
                commitMessage = (String) body.get("commitMessage");
                selectedFiles = (List<String>) body.get("selectedFiles");
                targetBranch = (String) body.get("targetBranch");
                
                Object shouldPushObj = body.get("shouldPush");
                if (shouldPushObj != null) {
                    shouldPush = (Boolean) shouldPushObj;
                }
                
                Object retriesObj = body.get("maxRetries");
                if (retriesObj != null) {
                    maxRetries = ((Number) retriesObj).intValue();
                }
            }
            
            // 如果没有提供提交信息，自动生成
            if (commitMessage == null || commitMessage.trim().isEmpty()) {
                Map<String, Object> statusResult = gitService.checkGitStatus(localPath);
                
                if (!(Boolean) statusResult.get("success")) {
                    return ResponseEntity.badRequest().body(statusResult);
                }
                
                if (!(Boolean) statusResult.get("hasChanges")) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("message", "没有需要提交的变更");
                    return ResponseEntity.ok(response);
                }
                
                commitMessage = gitService.generateCommitMessage(statusResult);
            }
            
            // 执行提交和推送
            Map<String, Object> commitResult = gitService.commitAndPush(
                localPath, 
                commitMessage, 
                selectedFiles, 
                targetBranch,
                shouldPush,
                maxRetries,
                project.getGitUserId()
            );
            
            return ResponseEntity.ok(commitResult);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Git提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 解决冲突后继续提交
     * POST /api/projects/{id}/git-continue
     */
    @PostMapping("/{id}/git-continue")
    public ResponseEntity<Map<String, Object>> continueAfterConflict(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try{
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            String commitMessage = body.get("commitMessage");
            Map<String, Object> result = gitService.continueAfterConflict(localPath, commitMessage);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "继续提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取Git分支列表
     * GET /api/projects/{id}/git-branches
     */
    @GetMapping("/{id}/git-branches")
    public ResponseEntity<Map<String, Object>> getGitBranches(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.getBranches(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取分支列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 切换Git分支
     * POST /api/projects/{id}/git-switch-branch
     */
    @PostMapping("/{id}/git-switch-branch")
    public ResponseEntity<Map<String, Object>> switchGitBranch(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            String branchName = body.get("branchName");
            if (branchName == null || branchName.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "分支名称不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.switchBranch(localPath, branchName);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "切换分支失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 保存Git忽略规则
     * POST /api/projects/{id}/git-ignore-rules
     */
    @PostMapping("/{id}/git-ignore-rules")
    public ResponseEntity<Map<String, Object>> saveGitIgnoreRules(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            List<String> ignoreFiles = (List<String>) body.get("ignoreFiles");
            Map<String, Object> result = gitService.saveIgnoreRules(localPath, ignoreFiles);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存忽略规则失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取Git忽略规则
     * GET /api/projects/{id}/git-ignore-rules
     */
    @GetMapping("/{id}/git-ignore-rules")
    public ResponseEntity<Map<String, Object>> getGitIgnoreRules(@PathVariable Long id) {
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitService.loadIgnoreRules(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取忽略规则失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
