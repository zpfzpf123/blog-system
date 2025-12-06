package com.blog.controller;

import com.blog.dto.GitCommitInfo;
import com.blog.dto.GitStatusResult;
import com.blog.entity.Project;
import com.blog.service.ProjectService;
import com.blog.service.GitSmartCommitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    private GitSmartCommitService gitSmartCommitService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
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
            
            // 根据操作系统打开文件夹
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                Runtime.getRuntime().exec("explorer.exe \"" + folder.getAbsolutePath() + "\"");
            } else if (os.contains("mac")) {
                Runtime.getRuntime().exec(new String[]{"open", folder.getAbsolutePath()});
            } else if (os.contains("nix") || os.contains("nux")) {
                Runtime.getRuntime().exec(new String[]{"xdg-open", folder.getAbsolutePath()});
            } else {
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
     * 刷新项目的 Git 提交记录
     * POST /api/projects/{id}/refresh-commits
     */
    @PostMapping("/{id}/refresh-commits")
    public ResponseEntity<Map<String, Object>> refreshGitCommits(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                response.put("success", false);
                response.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取最新的 Git 提交记录
            List<GitCommitInfo> commits = getGitCommits(localPath);
            
            if (commits.isEmpty()) {
                response.put("success", false);
                response.put("message", "该项目不是 Git 仓库或没有提交记录");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 更新项目的 Git 提交记录
            String commitsJson = objectMapper.writeValueAsString(commits);
            project.setGitCommits(commitsJson);
            projectService.updateProject(id, project);
            
            response.put("success", true);
            response.put("message", "成功刷新 " + commits.size() + " 条提交记录");
            response.put("count", commits.size());
            response.put("commits", commits);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "刷新失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取Git提交记录
     */
    private List<GitCommitInfo> getGitCommits(String projectPath) {
        List<GitCommitInfo> commits = new ArrayList<>();
        
        try {
            // 检查是否是Git仓库
            File gitDir = new File(projectPath, ".git");
            if (!gitDir.exists()) {
                System.out.println("不是Git仓库");
                return commits;
            }
            
            // 先执行 git fetch 同步远程仓库的最新提交
            try {
                ProcessBuilder fetchPb = new ProcessBuilder("git", "fetch", "--all");
                fetchPb.directory(new File(projectPath));
                fetchPb.redirectErrorStream(true);
                Process fetchProcess = fetchPb.start();
                
                // 等待fetch完成，最多等待30秒
                fetchProcess.waitFor(30, java.util.concurrent.TimeUnit.SECONDS);
                
                // 打印fetch结果（用于调试）
                try (java.io.BufferedReader fetchReader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(fetchProcess.getInputStream(), "UTF-8"))) {
                    String fetchLine;
                    while ((fetchLine = fetchReader.readLine()) != null) {
                        System.out.println("Git fetch: " + fetchLine);
                    }
                }
            } catch (Exception fetchError) {
                // fetch失败不影响读取本地提交记录
                System.err.println("Git fetch 失败（将继续读取本地记录）: " + fetchError.getMessage());
            }
            
            // 执行git log命令获取提交记录 - 获取最近500条（包括所有分支）
            ProcessBuilder pb = new ProcessBuilder(
                "git", "log", 
                "--all",  // 显示所有分支的提交
                "--max-count=500",
                "--pretty=format:%H|%an|%ad|%s",
                "--date=iso"
            );
            pb.directory(new File(projectPath));
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("\\|", 4);
                    if (parts.length >= 4) {
                        GitCommitInfo commit = new GitCommitInfo(
                            parts[0], // hash
                            parts[1], // author
                            parts[2], // date
                            parts[3]  // message
                        );
                        commits.add(commit);
                    }
                }
            }
            
            process.waitFor();
            
        } catch (Exception e) {
            System.err.println("获取Git记录失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return commits;
    }
    
    /**
     * 获取Git分支列表（本地分支和远程分支）
     * GET /api/projects/{id}/git/branches
     */
    @GetMapping("/{id}/git/branches")
    public ResponseEntity<Map<String, Object>> getGitBranches(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.put("success", false);
                result.put("message", "该项目不是Git仓库");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 获取当前分支
            String currentBranch = getCurrentBranch(localPath);
            
            // 获取所有本地分支
            List<String> localBranches = getLocalBranches(localPath);
            
            // 获取所有远程分支
            List<String> remoteBranches = getRemoteBranches(localPath);
            
            result.put("success", true);
            result.put("currentBranch", currentBranch);
            result.put("localBranches", localBranches);
            result.put("remoteBranches", remoteBranches);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取分支列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 获取当前分支名称
     */
    private String getCurrentBranch(String projectPath) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("git", "branch", "--show-current");
        pb.directory(new File(projectPath));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String branch = reader.readLine();
            if (branch != null && !branch.trim().isEmpty()) {
                return branch.trim();
            }
        }
        
        process.waitFor();
        return "unknown";
    }
    
    /**
     * 获取所有本地分支
     */
    private List<String> getLocalBranches(String projectPath) throws Exception {
        List<String> branches = new ArrayList<>();
        
        ProcessBuilder pb = new ProcessBuilder("git", "branch");
        pb.directory(new File(projectPath));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 去掉*号和空格
                String branch = line.replaceAll("^[\\s*]+", "").trim();
                if (!branch.isEmpty()) {
                    branches.add(branch);
                }
            }
        }
        
        process.waitFor();
        return branches;
    }
    
    /**
     * 获取所有远程分支
     */
    private List<String> getRemoteBranches(String projectPath) throws Exception {
        List<String> branches = new ArrayList<>();
        
        ProcessBuilder pb = new ProcessBuilder("git", "branch", "-r");
        pb.directory(new File(projectPath));
        pb.redirectErrorStream(true);
        Process process = pb.start();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 去掉前面的空格
                String branch = line.trim();
                // 过滤掉HEAD指向
                if (!branch.isEmpty() && !branch.contains("->")) {
                    branches.add(branch);
                }
            }
        }
        
        process.waitFor();
        return branches;
    }
    
    /**
     * Git环境检查 - 第一步
     * POST /api/projects/{id}/git/check-status
     */
    @PostMapping("/{id}/git/check-status")
    public ResponseEntity<GitStatusResult> checkGitStatus(@PathVariable Long id) {
        GitStatusResult result = new GitStatusResult();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.setSuccess(false);
                result.setMessage("本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.setSuccess(false);
                result.setMessage("该项目不是Git仓库");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 执行 git status 命令
            GitStatusResult statusResult = executeGitStatus(localPath);
            
            return ResponseEntity.ok(statusResult);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("环境检查失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 执行git status命令并解析结果
     */
    private GitStatusResult executeGitStatus(String projectPath) throws Exception {
        GitStatusResult result = new GitStatusResult();
        List<String> modifiedFiles = new ArrayList<>();
        List<String> untrackedFiles = new ArrayList<>();
        
        try {
            // 获取当前分支
            ProcessBuilder branchPb = new ProcessBuilder("git", "branch", "--show-current");
            branchPb.directory(new File(projectPath));
            branchPb.redirectErrorStream(true);
            Process branchProcess = branchPb.start();
            
            try (BufferedReader branchReader = new BufferedReader(
                    new InputStreamReader(branchProcess.getInputStream(), "UTF-8"))) {
                String branch = branchReader.readLine();
                if (branch != null && !branch.trim().isEmpty()) {
                    result.setCurrentBranch(branch.trim());
                }
            }
            branchProcess.waitFor();
            
            // 执行git status --porcelain命令
            ProcessBuilder statusPb = new ProcessBuilder("git", "status", "--porcelain");
            statusPb.directory(new File(projectPath));
            statusPb.redirectErrorStream(true);
            Process statusProcess = statusPb.start();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(statusProcess.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() < 3) continue;
                    
                    String status = line.substring(0, 2);
                    String fileName = line.substring(3).trim();
                    
                    // 解析文件状态
                    if (status.startsWith("??")) {
                        // 未跟踪的文件
                        untrackedFiles.add(fileName);
                    } else if (!status.trim().isEmpty()) {
                        // 已修改的文件（包括暂存区和工作区）
                        modifiedFiles.add(fileName);
                    }
                }
            }
            
            statusProcess.waitFor();
            
            // 设置结果
            result.setSuccess(true);
            result.setModifiedFiles(modifiedFiles);
            result.setUntrackedFiles(untrackedFiles);
            result.setModifiedCount(modifiedFiles.size());
            result.setUntrackedCount(untrackedFiles.size());
            result.setHasChanges(modifiedFiles.size() > 0 || untrackedFiles.size() > 0);
            
            if (result.isHasChanges()) {
                result.setMessage("检测到 " + (modifiedFiles.size() + untrackedFiles.size()) + " 个文件变更");
            } else {
                result.setMessage("工作区干净，没有待提交的更改");
            }
            
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("执行git status失败: " + e.getMessage());
            throw e;
        }
        
        return result;
    }
    
    /**
     * Git本地提交 - 第二步：暂存文件
     * POST /api/projects/{id}/git/add
     */
    @PostMapping("/{id}/git/add")
    public ResponseEntity<Map<String, Object>> gitAdd(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.put("success", false);
                result.put("message", "该项目不是Git仓库");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 执行 git add .
            ProcessBuilder pb = new ProcessBuilder("git", "add", ".");
            pb.directory(new File(localPath));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 读取命令输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                result.put("success", true);
                result.put("message", "已暂存所有更改");
                result.put("output", output.toString());
            } else {
                result.put("success", false);
                result.put("message", "暂存失败: " + output.toString());
                result.put("exitCode", exitCode);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "执行git add失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Git本地提交 - 第二步：提交到本地仓库
     * POST /api/projects/{id}/git/commit
     */
    @PostMapping("/{id}/git/commit")
    public ResponseEntity<Map<String, Object>> gitCommit(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取提交信息
            String commitMessage = requestBody.get("message");
            if (commitMessage == null || commitMessage.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "提交信息不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.put("success", false);
                result.put("message", "该项目不是Git仓库");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 执行 git commit -m "message"
            ProcessBuilder pb = new ProcessBuilder("git", "commit", "-m", commitMessage);
            pb.directory(new File(localPath));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 读取命令输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                result.put("success", true);
                result.put("message", "本地提交成功");
                result.put("output", output.toString());
                
                // 解析commit hash（从输出中提取）
                String outputStr = output.toString();
                if (outputStr.contains("[")) {
                    int start = outputStr.indexOf("[") + 1;
                    int end = outputStr.indexOf("]");
                    if (end > start) {
                        String commitInfo = outputStr.substring(start, end);
                        result.put("commitInfo", commitInfo);
                    }
                }
            } else {
                // 检查是否是"没有更改"的情况
                String outputStr = output.toString();
                if (outputStr.contains("nothing to commit") || outputStr.contains("no changes")) {
                    result.put("success", true);
                    result.put("message", "工作区干净，没有需要提交的更改");
                    result.put("noChanges", true);
                } else {
                    result.put("success", false);
                    result.put("message", "提交失败: " + outputStr);
                    result.put("exitCode", exitCode);
                }
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "执行git commit失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Git智能同步 - 第三步：获取远程更新
     * POST /api/projects/{id}/git/fetch
     */
    @PostMapping("/{id}/git/fetch")
    public ResponseEntity<Map<String, Object>> gitFetch(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.put("success", false);
                result.put("message", "该项目不是Git仓库");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 执行 git fetch
            ProcessBuilder pb = new ProcessBuilder("git", "fetch");
            pb.directory(new File(localPath));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 读取命令输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                result.put("success", true);
                result.put("message", "获取远程最新状态完成");
                result.put("output", output.toString());
            } else {
                result.put("success", false);
                result.put("message", "获取远程更新失败: " + output.toString());
                result.put("exitCode", exitCode);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "执行git fetch失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Git智能同步 - 第三步：变基拉取
     * POST /api/projects/{id}/git/pull-rebase
     */
    @PostMapping("/{id}/git/pull-rebase")
    public ResponseEntity<Map<String, Object>> gitPullRebase(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.put("success", false);
                result.put("message", "该项目不是Git仓库");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 执行 git pull --rebase
            ProcessBuilder pb = new ProcessBuilder("git", "pull", "--rebase");
            pb.directory(new File(localPath));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 读取命令输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            String outputStr = output.toString();
            
            // 检查是否有冲突
            boolean hasConflict = outputStr.contains("CONFLICT") || 
                                 outputStr.contains("conflict") ||
                                 exitCode != 0;
            
            if (exitCode == 0) {
                result.put("success", true);
                result.put("hasConflict", false);
                
                // 判断是否已经是最新的
                if (outputStr.contains("Already up to date") || 
                    outputStr.contains("Already up-to-date") ||
                    outputStr.contains("Current branch") && outputStr.contains("up to date")) {
                    result.put("message", "当前分支已是最新");
                    result.put("upToDate", true);
                } else {
                    result.put("message", "变基拉取成功");
                    result.put("upToDate", false);
                }
                
                result.put("output", outputStr);
            } else {
                // 检查是否是冲突
                if (hasConflict) {
                    result.put("success", false);
                    result.put("hasConflict", true);
                    result.put("message", "检测到代码冲突，请在IDE中解决后继续");
                    result.put("output", outputStr);
                    
                    // 获取冲突文件列表
                    List<String> conflictFiles = getConflictFiles(localPath);
                    result.put("conflictFiles", conflictFiles);
                    result.put("conflictCount", conflictFiles.size());
                    
                    // 不执行abort，保留冲突现场让用户解决
                    result.put("rebaseAborted", false);
                    result.put("abortMessage", "请解决冲突后点击'继续推送'");
                } else {
                    result.put("success", false);
                    result.put("hasConflict", false);
                    result.put("message", "拉取失败: " + outputStr);
                    result.put("exitCode", exitCode);
                }
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "执行git pull --rebase失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 检查是否处于rebase状态
     * POST /api/projects/{id}/git/check-rebase-status
     */
    @PostMapping("/{id}/git/check-rebase-status")
    public ResponseEntity<Map<String, Object>> checkRebaseStatus(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            // 检查 .git/rebase-merge 或 .git/rebase-apply 目录
            File rebaseMerge = new File(projectDir, ".git/rebase-merge");
            File rebaseApply = new File(projectDir, ".git/rebase-apply");
            
            boolean inRebase = rebaseMerge.exists() || rebaseApply.exists();
            
            result.put("success", true);
            result.put("inRebase", inRebase);
            
            if (inRebase) {
                result.put("message", "当前处于rebase状态");
            } else {
                result.put("message", "当前不在rebase状态");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "检查rebase状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 继续推送（解决冲突后）
     * POST /api/projects/{id}/git/continue-push
     * 自动执行：git add . → git rebase --continue → git push
     */
    @PostMapping("/{id}/git/continue-push")
    public ResponseEntity<Map<String, Object>> continuePush(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 步骤1: git add . (暂存所有解决后的文件)
            ProcessBuilder addPb = new ProcessBuilder("git", "add", ".");
            addPb.directory(new File(localPath));
            addPb.redirectErrorStream(true);
            Process addProcess = addPb.start();
            
            StringBuilder addOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(addProcess.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    addOutput.append(line).append("\n");
                }
            }
            
            int addExitCode = addProcess.waitFor();
            if (addExitCode != 0) {
                result.put("success", false);
                result.put("message", "暂存文件失败: " + addOutput.toString());
                result.put("step", "add");
                return ResponseEntity.ok(result);
            }
            
            // 步骤2: git rebase --continue
            ProcessBuilder continuePb = new ProcessBuilder("git", "rebase", "--continue");
            continuePb.directory(new File(localPath));
            continuePb.redirectErrorStream(true);
            Process continueProcess = continuePb.start();
            
            StringBuilder continueOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(continueProcess.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    continueOutput.append(line).append("\n");
                }
            }
            
            int continueExitCode = continueProcess.waitFor();
            String continueOutputStr = continueOutput.toString();
            
            // 检查是否还有冲突
            if (continueOutputStr.contains("CONFLICT") || continueOutputStr.contains("conflict")) {
                result.put("success", false);
                result.put("hasConflict", true);
                result.put("message", "仍有未解决的冲突，请继续解决");
                result.put("output", continueOutputStr);
                result.put("step", "rebase-continue");
                return ResponseEntity.ok(result);
            }
            
            if (continueExitCode != 0) {
                result.put("success", false);
                result.put("message", "继续rebase失败: " + continueOutputStr);
                result.put("step", "rebase-continue");
                return ResponseEntity.ok(result);
            }
            
            // 步骤3: git push
            ProcessBuilder pushPb = new ProcessBuilder("git", "push");
            pushPb.directory(new File(localPath));
            pushPb.redirectErrorStream(true);
            Process pushProcess = pushPb.start();
            
            StringBuilder pushOutput = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(pushProcess.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    pushOutput.append(line).append("\n");
                }
            }
            
            int pushExitCode = pushProcess.waitFor();
            String pushOutputStr = pushOutput.toString();
            
            if (pushExitCode == 0) {
                result.put("success", true);
                result.put("message", "推送成功！");
                result.put("output", pushOutputStr);
            } else {
                result.put("success", false);
                result.put("message", "推送失败: " + pushOutputStr);
                result.put("step", "push");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "继续推送失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * Git推送代码 - 第五步
     * POST /api/projects/{id}/git/push
     * 支持指定本地分支和远程分支
     */
    @PostMapping("/{id}/git/push")
    public ResponseEntity<Map<String, Object>> gitPush(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.put("success", false);
                result.put("message", "该项目不是Git仓库");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 获取分支参数（可选）
            String localBranch = requestBody != null ? requestBody.get("localBranch") : null;
            String remoteBranch = requestBody != null ? requestBody.get("remoteBranch") : null;
            
            // 构建 git push 命令
            List<String> pushCommand = new ArrayList<>();
            pushCommand.add("git");
            pushCommand.add("push");
            
            // 如果指定了分支，添加分支参数
            if (remoteBranch != null && !remoteBranch.trim().isEmpty()) {
                // 解析远程分支，格式可能是 origin/main 或 main
                String[] parts = remoteBranch.split("/");
                String remote = parts.length > 1 ? parts[0] : "origin";
                String branch = parts.length > 1 ? parts[1] : remoteBranch;
                
                pushCommand.add(remote);
                
                if (localBranch != null && !localBranch.trim().isEmpty()) {
                    // 推送本地分支到远程分支: git push origin localBranch:remoteBranch
                    pushCommand.add(localBranch + ":" + branch);
                } else {
                    // 只推送到指定的远程分支: git push origin remoteBranch
                    pushCommand.add(branch);
                }
            }
            
            // 执行 git push
            ProcessBuilder pb = new ProcessBuilder(pushCommand);
            pb.directory(new File(localPath));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 读取命令输出
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            String outputStr = output.toString();
            
            if (exitCode == 0) {
                result.put("success", true);
                result.put("message", "推送成功！");
                result.put("output", outputStr);
                
                // 检查是否是 "Everything up-to-date"
                if (outputStr.contains("Everything up-to-date") || 
                    outputStr.contains("up to date")) {
                    result.put("upToDate", true);
                    result.put("message", "已是最新，无需推送");
                } else {
                    result.put("upToDate", false);
                }
            } else {
                // 检查常见的push失败原因
                if (outputStr.contains("rejected") || outputStr.contains("non-fast-forward")) {
                    result.put("success", false);
                    result.put("needPull", true);
                    result.put("message", "推送被拒绝：远程有新提交，需要先拉取");
                } else if (outputStr.contains("no upstream") || outputStr.contains("set-upstream")) {
                    result.put("success", false);
                    result.put("needSetUpstream", true);
                    result.put("message", "未设置上游分支，请先配置远程分支");
                } else {
                    result.put("success", false);
                    result.put("message", "推送失败: " + outputStr);
                }
                result.put("exitCode", exitCode);
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "执行git push失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 获取冲突文件列表
     */
    private List<String> getConflictFiles(String projectPath) {
        List<String> conflictFiles = new ArrayList<>();
        
        try {
            // 执行 git diff --name-only --diff-filter=U 获取冲突的文件
            ProcessBuilder pb = new ProcessBuilder("git", "diff", "--name-only", "--diff-filter=U");
            pb.directory(new File(projectPath));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        conflictFiles.add(line.trim());
                    }
                }
            }
            
            process.waitFor();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return conflictFiles;
    }
    
    /**
     * 获取项目目录树结构
     * GET /api/projects/{id}/directory-tree
     */
    @GetMapping("/{id}/directory-tree")
    public ResponseEntity<Map<String, Object>> getDirectoryTree(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 构建目录树
            Map<String, Object> tree = buildDirectoryTree(projectDir, projectDir, 0, 3);
            
            result.put("success", true);
            result.put("tree", tree);
            result.put("projectPath", localPath);
            result.put("message", "获取目录树成功");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取目录树失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 递归构建目录树
     */
    private Map<String, Object> buildDirectoryTree(File file, File projectRoot, int depth, int maxDepth) {
        Map<String, Object> node = new HashMap<>();
        
        // 计算相对路径
        String absolutePath = file.getAbsolutePath();
        String rootPath = projectRoot.getAbsolutePath();
        String relativePath = absolutePath.equals(rootPath) ? "." : 
            absolutePath.substring(rootPath.length() + 1).replace("\\", "/");
        
        node.put("name", file.getName().isEmpty() ? file.getAbsolutePath() : file.getName());
        node.put("path", relativePath);
        node.put("isDirectory", file.isDirectory());
        
        // 如果是目录且未达到最大深度，递归获取子节点
        if (file.isDirectory() && depth < maxDepth) {
            File[] children = file.listFiles();
            if (children != null) {
                List<Map<String, Object>> childrenList = new ArrayList<>();
                
                // 过滤掉.git等隐藏文件夹和node_modules等
                for (File child : children) {
                    String name = child.getName();
                    // 跳过隐藏文件和一些常见的忽略目录
                    if (name.startsWith(".") || 
                        name.equals("node_modules") || 
                        name.equals("target") ||
                        name.equals("dist") ||
                        name.equals("build") ||
                        name.equals("out")) {
                        continue;
                    }
                    
                    childrenList.add(buildDirectoryTree(child, projectRoot, depth + 1, maxDepth));
                }
                
                // 排序：目录在前，文件在后，同类按名称排序
                childrenList.sort((a, b) -> {
                    boolean aIsDir = (boolean) a.get("isDirectory");
                    boolean bIsDir = (boolean) b.get("isDirectory");
                    if (aIsDir != bIsDir) {
                        return aIsDir ? -1 : 1;
                    }
                    return ((String) a.get("name")).compareToIgnoreCase((String) b.get("name"));
                });
                
                node.put("children", childrenList);
                node.put("hasChildren", !childrenList.isEmpty());
            } else {
                node.put("children", new ArrayList<>());
                node.put("hasChildren", false);
            }
        } else if (file.isDirectory()) {
            // 达到最大深度，标记为可展开但不加载子节点
            node.put("hasChildren", true);
            node.put("children", new ArrayList<>());
        }
        
        return node;
    }
    
    /**
     * 展开目录节点（懒加载）
     * POST /api/projects/{id}/expand-directory
     */
    @PostMapping("/{id}/expand-directory")
    public ResponseEntity<Map<String, Object>> expandDirectory(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String relativePath = requestBody.get("path");
            if (relativePath == null) {
                result.put("success", false);
                result.put("message", "路径参数缺失");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            File projectDir = new File(localPath);
            
            // 构建目标文件路径
            File targetDir = relativePath.equals(".") ? projectDir : 
                new File(projectDir, relativePath.replace("/", File.separator));
            
            if (!targetDir.exists() || !targetDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "目录不存在");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 获取子节点
            List<Map<String, Object>> children = new ArrayList<>();
            File[] files = targetDir.listFiles();
            
            if (files != null) {
                for (File file : files) {
                    String name = file.getName();
                    if (name.startsWith(".") || 
                        name.equals("node_modules") || 
                        name.equals("target") ||
                        name.equals("dist") ||
                        name.equals("build") ||
                        name.equals("out")) {
                        continue;
                    }
                    
                    children.add(buildDirectoryTree(file, projectDir, 0, 0));
                }
                
                // 排序
                children.sort((a, b) -> {
                    boolean aIsDir = (boolean) a.get("isDirectory");
                    boolean bIsDir = (boolean) b.get("isDirectory");
                    if (aIsDir != bIsDir) {
                        return aIsDir ? -1 : 1;
                    }
                    return ((String) a.get("name")).compareToIgnoreCase((String) b.get("name"));
                });
            }
            
            result.put("success", true);
            result.put("children", children);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "展开目录失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 选择本地文件/文件夹
     * POST /api/projects/{id}/select-file
     */
    @PostMapping("/{id}/select-file")
    public ResponseEntity<Map<String, Object>> selectFile(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // Windows PowerShell方式选择文件
            ProcessBuilder pb = new ProcessBuilder(
                "powershell.exe",
                "-Command",
                "Add-Type -AssemblyName System.Windows.Forms; " +
                "$f = New-Object System.Windows.Forms.OpenFileDialog; " +
                "$f.InitialDirectory = '" + localPath.replace("'", "''") + "'; " +
                "$f.Filter = 'All Files (*.*)|*.*'; " +
                "$f.Title = '选择要查看历史的文件或文件夹'; " +
                "$f.CheckFileExists = $false; " +
                "$f.CheckPathExists = $true; " +
                "$f.ValidateNames = $false; " +
                "if ($f.ShowDialog() -eq 'OK') { $f.FileName } else { 'CANCELLED' }"
            );
            
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
            }
            
            int exitCode = process.waitFor();
            String selectedPath = output.toString().trim();
            
            if (exitCode == 0 && !selectedPath.isEmpty() && !selectedPath.equals("CANCELLED")) {
                try {
                    // 使用 Java NIO Path API 进行更可靠的路径处理
                    java.nio.file.Path selectedFilePath = java.nio.file.Paths.get(selectedPath).toRealPath();
                    java.nio.file.Path projectDirPath = projectDir.toPath().toRealPath();
                    
                    // 检查选择的文件是否在项目目录内
                    if (!selectedFilePath.startsWith(projectDirPath)) {
                        result.put("success", false);
                        result.put("message", "所选文件不在项目目录内");
                        result.put("debug_selectedPath", selectedFilePath.toString());
                        result.put("debug_projectPath", projectDirPath.toString());
                        return ResponseEntity.badRequest().body(result);
                    }
                    
                    // 计算相对路径
                    java.nio.file.Path relativeFilePath = projectDirPath.relativize(selectedFilePath);
                    String relativePath = relativeFilePath.toString().replace("\\", "/");
                    
                    // 如果是项目根目录，使用 "."
                    if (relativePath.isEmpty()) {
                        relativePath = ".";
                    }
                    
                    result.put("success", true);
                    result.put("relativePath", relativePath);
                    result.put("absolutePath", selectedFilePath.toString());
                    result.put("isDirectory", java.nio.file.Files.isDirectory(selectedFilePath));
                    result.put("message", "文件选择成功");
                    
                } catch (java.nio.file.NoSuchFileException e) {
                    result.put("success", false);
                    result.put("message", "选择的文件不存在: " + selectedPath);
                    return ResponseEntity.badRequest().body(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    result.put("success", false);
                    result.put("message", "处理文件路径失败: " + e.getMessage());
                    return ResponseEntity.badRequest().body(result);
                }
            } else {
                // 用户取消选择
                result.put("success", false);
                result.put("cancelled", true);
                result.put("message", "用户取消选择");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "选择文件失败: " + (e.getMessage() != null ? e.getMessage() : "未知错误，请确保在Windows环境中运行"));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 获取文件或文件夹的Git历史记录
     * POST /api/projects/{id}/file-history
     */
    @PostMapping("/{id}/file-history")
    public ResponseEntity<Map<String, Object>> getFileHistory(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取文件路径参数
            String filePath = requestBody.get("filePath");
            if (filePath == null || filePath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件路径不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            File projectDir = new File(localPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                result.put("success", false);
                result.put("message", "本地路径不存在: " + localPath);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.put("success", false);
                result.put("message", "该项目不是Git仓库");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 执行 git log --follow --format="{hash}|||{author}|||{date}|||{message}" -- filePath
            List<String> command = new ArrayList<>();
            command.add("git");
            command.add("log");
            command.add("--follow"); // 跟踪文件重命名
            command.add("--format=%H|||%an|||%ai|||%s"); // 自定义格式：hash|||author|||date|||message
            command.add("--");
            command.add(filePath);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(localPath));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 读取命令输出
            List<Map<String, String>> commits = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        String[] parts = line.split("\\|\\|\\|");
                        if (parts.length >= 4) {
                            Map<String, String> commit = new HashMap<>();
                            commit.put("hash", parts[0].trim());
                            commit.put("author", parts[1].trim());
                            commit.put("date", parts[2].trim());
                            commit.put("message", parts[3].trim());
                            commits.add(commit);
                        }
                    }
                }
            }
            
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                result.put("success", true);
                result.put("commits", commits);
                result.put("count", commits.size());
                result.put("filePath", filePath);
                result.put("message", "成功获取 " + commits.size() + " 条提交记录");
            } else {
                result.put("success", false);
                result.put("message", "获取文件历史失败，请检查文件路径是否正确");
                result.put("commits", new ArrayList<>());
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "获取文件历史失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 通过WebStorm打开文件
     * POST /api/projects/{id}/open-file
     */
    @PostMapping("/{id}/open-file")
    public ResponseEntity<Map<String, Object>> openFileInWebStorm(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取文件名
            String fileName = requestBody.get("fileName");
            if (fileName == null || fileName.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件名不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "该项目没有配置本地路径");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 构建完整的文件路径
            File projectDir = new File(localPath);
            File targetFile = new File(projectDir, fileName);
            
            if (!targetFile.exists()) {
                result.put("success", false);
                result.put("message", "文件不存在: " + fileName);
                return ResponseEntity.badRequest().body(result);
            }
            
            // 获取文件的绝对路径
            String absolutePath = targetFile.getAbsolutePath();
            
            // 尝试使用 webstorm 命令打开文件
            // Windows下的WebStorm命令通常是 webstorm64.exe 或 webstorm.cmd
            ProcessBuilder pb;
            
            // 尝试多种WebStorm启动方式
            String[] commands = {
                "webstorm64.exe",
                "webstorm.cmd",
                "webstorm",
                "idea64.exe"  // 作为备选，如果用户使用IntelliJ IDEA
            };
            
            boolean opened = false;
            StringBuilder errorOutput = new StringBuilder();
            
            for (String command : commands) {
                try {
                    pb = new ProcessBuilder(command, absolutePath);
                    pb.redirectErrorStream(true);
                    Process process = pb.start();
                    
                    // 等待一小段时间检查是否成功启动
                    Thread.sleep(500);
                    
                    if (process.isAlive() || process.exitValue() == 0) {
                        opened = true;
                        result.put("success", true);
                        result.put("message", "文件已在WebStorm中打开");
                        result.put("filePath", absolutePath);
                        result.put("command", command);
                        break;
                    }
                } catch (Exception e) {
                    errorOutput.append(command).append(": ").append(e.getMessage()).append("; ");
                    // 继续尝试下一个命令
                }
            }
            
            if (!opened) {
                result.put("success", false);
                result.put("message", "无法打开WebStorm，请确保WebStorm已安装并配置到系统PATH中。错误信息: " + errorOutput.toString());
                result.put("filePath", absolutePath);
                result.put("hint", "您可以手动在WebStorm中打开此文件");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "打开文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    /**
     * 读取文件内容
     */
    @PostMapping("/{id}/read-file")
    public ResponseEntity<Map<String, Object>> readFile(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            System.out.println("读取文件请求 - 项目ID: " + id);
            System.out.println("请求体: " + requestBody);
            
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            System.out.println("项目信息: " + project.getName());
            
            String filePath = requestBody.get("filePath");
            System.out.println("文件路径: " + filePath);
            
            if (filePath == null || filePath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "文件路径不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 构建完整的文件路径
            String localPath = project.getLocalPath();
            System.out.println("项目本地路径: " + localPath);
            
            if (localPath == null || localPath.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "项目本地路径未配置");
                return ResponseEntity.badRequest().body(result);
            }
            
            Path projectPath = Paths.get(localPath);
            Path fullPath = projectPath.resolve(filePath).normalize();
            System.out.println("完整文件路径: " + fullPath.toString());
            
            // 安全检查：确保文件在项目目录内
            if (!fullPath.startsWith(projectPath)) {
                System.out.println("安全检查失败 - 文件不在项目目录内");
                result.put("success", false);
                result.put("message", "文件路径不在项目目录内");
                return ResponseEntity.badRequest().body(result);
            }
            
            // 检查文件是否存在
            if (!Files.exists(fullPath)) {
                System.out.println("文件不存在: " + fullPath);
                result.put("success", false);
                result.put("message", "文件不存在: " + fullPath.toString());
                return ResponseEntity.ok(result);
            }
            
            // 检查是否是文件而不是目录
            if (!Files.isRegularFile(fullPath)) {
                System.out.println("路径不是文件: " + fullPath);
                result.put("success", false);
                result.put("message", "路径不是一个文件");
                return ResponseEntity.ok(result);
            }
            
            // 读取文件内容
            System.out.println("开始读取文件...");
            String content = new String(Files.readAllBytes(fullPath), StandardCharsets.UTF_8);
            System.out.println("文件读取成功，长度: " + content.length());
            
            result.put("success", true);
            result.put("content", content);
            result.put("filePath", filePath);
            result.put("fileName", fullPath.getFileName().toString());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("读取文件异常: " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "读取文件失败: " + e.getClass().getName() + " - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result);
        }
    }
    
    // ==================== 新的Git智能提交API ====================
    
    /**
     * 统一的Git智能提交API
     * POST /api/projects/{id}/smart-commit
     * 
     * 完整流程：
     * 1. 检查工作区状态
     * 2. 暂存文件
     * 3. 拉取远程代码（rebase）
     * 4. 如有冲突，返回冲突信息
     * 5. 提交到本地
     * 6. 推送到远程（支持自动重试）
     */
    @PostMapping("/{id}/smart-commit")
    public ResponseEntity<Map<String, Object>> smartCommit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 获取项目信息
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "项目未配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 获取参数
            String commitMessage = (String) requestBody.get("commitMessage");
            List<String> selectedFiles = (List<String>) requestBody.get("selectedFiles");
            String localBranch = (String) requestBody.get("localBranch");
            String remoteBranch = (String) requestBody.get("remoteBranch");
            Integer maxRetries = (Integer) requestBody.getOrDefault("maxRetries", 3);
            
            if (commitMessage == null || commitMessage.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "提交信息不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 步骤1: 检查工作区
            Map<String, Object> statusResult = gitSmartCommitService.checkWorkingDirectory(localPath);
            if (!(Boolean) statusResult.get("success")) {
                return ResponseEntity.badRequest().body(statusResult);
            }
            
            if (!(Boolean) statusResult.get("hasChanges")) {
                response.put("success", true);
                response.put("noChanges", true);
                response.put("message", "工作区干净，没有需要提交的更改");
                return ResponseEntity.ok(response);
            }
            
            response.put("step1_status", statusResult);
            
            // 步骤2: 暂存文件
            Map<String, Object> stageResult = gitSmartCommitService.stageFiles(localPath, selectedFiles);
            if (!(Boolean) stageResult.get("success")) {
                response.put("success", false);
                response.put("step", "stage");
                response.put("message", stageResult.get("message"));
                return ResponseEntity.ok(response);
            }
            
            response.put("step2_stage", stageResult);
            
            // 步骤3: 拉取远程代码
            Map<String, Object> pullResult = gitSmartCommitService.pullWithRebase(localPath);
            
            // 如果有冲突，返回冲突信息
            if (pullResult.containsKey("hasConflict") && (Boolean) pullResult.get("hasConflict")) {
                response.put("success", false);
                response.put("step", "pull");
                response.put("hasConflict", true);
                response.put("conflictFiles", pullResult.get("conflictFiles"));
                response.put("conflictCount", pullResult.get("conflictCount"));
                response.put("message", "检测到代码冲突，请解决后继续");
                response.put("needResolveConflict", true);
                return ResponseEntity.ok(response);
            }
            
            if (!(Boolean) pullResult.get("success")) {
                response.put("success", false);
                response.put("step", "pull");
                response.put("message", pullResult.get("message"));
                return ResponseEntity.ok(response);
            }
            
            response.put("step3_pull", pullResult);
            
            // 步骤4: 提交到本地
            Map<String, Object> commitResult = gitSmartCommitService.commitToLocal(localPath, commitMessage);
            if (!(Boolean) commitResult.get("success")) {
                response.put("success", false);
                response.put("step", "commit");
                response.put("message", commitResult.get("message"));
                return ResponseEntity.ok(response);
            }
            
            response.put("step4_commit", commitResult);
            
            // 步骤5: 推送到远程
            Map<String, Object> pushResult = gitSmartCommitService.pushToRemote(
                localPath, localBranch, remoteBranch, maxRetries);
            
            if (!(Boolean) pushResult.get("success")) {
                response.put("success", false);
                response.put("step", "push");
                response.put("message", pushResult.get("message"));
                response.put("needPull", pushResult.getOrDefault("needPull", false));
                response.put("retryCount", pushResult.get("retryCount"));
                return ResponseEntity.ok(response);
            }
            
            response.put("step5_push", pushResult);
            
            // 全部成功
            response.put("success", true);
            response.put("message", "智能提交完成");
            response.put("commitHash", commitResult.get("commitHash"));
            response.put("retryCount", pushResult.get("retryCount"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "智能提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 继续提交（解决冲突后）
     * POST /api/projects/{id}/continue-commit
     */
    @PostMapping("/{id}/continue-commit")
    public ResponseEntity<Map<String, Object>> continueCommit(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String commitMessage = (String) requestBody.get("commitMessage");
            String localBranch = (String) requestBody.get("localBranch");
            String remoteBranch = (String) requestBody.get("remoteBranch");
            Integer maxRetries = (Integer) requestBody.getOrDefault("maxRetries", 3);
            
            // 继续rebase
            Map<String, Object> rebaseResult = gitSmartCommitService.continueRebase(localPath);
            
            if (!(Boolean) rebaseResult.get("success")) {
                // 如果还有冲突
                if (rebaseResult.containsKey("hasConflict") && (Boolean) rebaseResult.get("hasConflict")) {
                    response.put("success", false);
                    response.put("hasConflict", true);
                    response.put("conflictFiles", rebaseResult.get("conflictFiles"));
                    response.put("message", "仍有未解决的冲突");
                    return ResponseEntity.ok(response);
                }
                
                response.put("success", false);
                response.put("message", rebaseResult.get("message"));
                return ResponseEntity.ok(response);
            }
            
            response.put("rebaseResult", rebaseResult);
            
            // 提交到本地（如果需要）
            if (commitMessage != null && !commitMessage.trim().isEmpty()) {
                Map<String, Object> commitResult = gitSmartCommitService.commitToLocal(localPath, commitMessage);
                if (!(Boolean) commitResult.get("success") && 
                    !commitResult.containsKey("noChanges")) {
                    response.put("success", false);
                    response.put("message", commitResult.get("message"));
                    return ResponseEntity.ok(response);
                }
                response.put("commitResult", commitResult);
            }
            
            // 推送到远程
            Map<String, Object> pushResult = gitSmartCommitService.pushToRemote(
                localPath, localBranch, remoteBranch, maxRetries);
            
            if (!(Boolean) pushResult.get("success")) {
                response.put("success", false);
                response.put("message", pushResult.get("message"));
                response.put("needPull", pushResult.getOrDefault("needPull", false));
                return ResponseEntity.ok(response);
            }
            
            response.put("pushResult", pushResult);
            response.put("success", true);
            response.put("message", "继续提交成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "继续提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 取消提交（中止rebase）
     * POST /api/projects/{id}/abort-commit
     */
    @PostMapping("/{id}/abort-commit")
    public ResponseEntity<Map<String, Object>> abortCommit(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.abortRebase(localPath);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "取消提交失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 切换分支
     * POST /api/projects/{id}/switch-branch
     */
    @PostMapping("/{id}/switch-branch")
    public ResponseEntity<Map<String, Object>> switchBranch(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String branchName = requestBody.get("branchName");
            
            if (branchName == null || branchName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "分支名不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitSmartCommitService.switchBranch(localPath, branchName);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "切换分支失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // ==================== 新增Git功能API ====================
    
    /**
     * 获取详细的Git状态
     * GET /api/projects/{id}/git/detailed-status
     */
    @GetMapping("/{id}/git/detailed-status")
    public ResponseEntity<Map<String, Object>> getDetailedGitStatus(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            if (localPath == null || localPath.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "项目未配置本地路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitSmartCommitService.getDetailedStatus(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取状态失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取提交历史
     * GET /api/projects/{id}/git/commits?limit=0 (0表示不限制)
     */
    @GetMapping("/{id}/git/commits")
    public ResponseEntity<Map<String, Object>> getCommitHistory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.getCommitHistory(localPath, limit);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取提交历史失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取某次提交的文件变更
     * GET /api/projects/{id}/git/commits/{hash}/files
     */
    @GetMapping("/{id}/git/commits/{hash}/files")
    public ResponseEntity<Map<String, Object>> getCommitFiles(
            @PathVariable Long id,
            @PathVariable String hash) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.getCommitFiles(localPath, hash);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取提交文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取某次提交中特定文件的差异
     * GET /api/projects/{id}/git/commits/{hash}/diff?filePath=xxx
     */
    @GetMapping("/{id}/git/commits/{hash}/diff")
    public ResponseEntity<Map<String, Object>> getCommitFileDiff(
            @PathVariable Long id,
            @PathVariable String hash,
            @RequestParam(required = false) String filePath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.getCommitFileDiff(localPath, hash, filePath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取文件差异失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 创建新分支
     * POST /api/projects/{id}/git/branches/create
     */
    @PostMapping("/{id}/git/branches/create")
    public ResponseEntity<Map<String, Object>> createBranch(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String branchName = (String) requestBody.get("branchName");
            Boolean checkout = (Boolean) requestBody.getOrDefault("checkout", false);
            
            Map<String, Object> result = gitSmartCommitService.createBranch(localPath, branchName, checkout);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "创建分支失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 删除分支
     * POST /api/projects/{id}/git/branches/delete
     */
    @PostMapping("/{id}/git/branches/delete")
    public ResponseEntity<Map<String, Object>> deleteBranch(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String branchName = (String) requestBody.get("branchName");
            Boolean force = (Boolean) requestBody.getOrDefault("force", false);
            Boolean remote = (Boolean) requestBody.getOrDefault("remote", false);
            
            Map<String, Object> result = gitSmartCommitService.deleteBranch(localPath, branchName, force, remote);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "删除分支失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 合并分支
     * POST /api/projects/{id}/git/merge
     */
    @PostMapping("/{id}/git/merge")
    public ResponseEntity<Map<String, Object>> mergeBranch(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String branchName = requestBody.get("branchName");
            
            Map<String, Object> result = gitSmartCommitService.mergeBranch(localPath, branchName);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "合并分支失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Stash 暂存修改
     * POST /api/projects/{id}/git/stash
     */
    @PostMapping("/{id}/git/stash")
    public ResponseEntity<Map<String, Object>> stash(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String message = requestBody != null ? requestBody.get("message") : null;
            
            Map<String, Object> result = gitSmartCommitService.stash(localPath, message);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "暂存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取 Stash 列表
     * GET /api/projects/{id}/git/stash/list
     */
    @GetMapping("/{id}/git/stash/list")
    public ResponseEntity<Map<String, Object>> getStashList(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.getStashList(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取暂存列表失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 应用 Stash
     * POST /api/projects/{id}/git/stash/apply
     */
    @PostMapping("/{id}/git/stash/apply")
    public ResponseEntity<Map<String, Object>> applyStash(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Integer index = (Integer) requestBody.getOrDefault("index", 0);
            Boolean pop = (Boolean) requestBody.getOrDefault("pop", false);
            
            Map<String, Object> result = gitSmartCommitService.applyStash(localPath, index, pop);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "应用暂存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 删除 Stash
     * POST /api/projects/{id}/git/stash/drop
     */
    @PostMapping("/{id}/git/stash/drop")
    public ResponseEntity<Map<String, Object>> dropStash(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Integer index = (Integer) requestBody.getOrDefault("index", 0);
            
            Map<String, Object> result = gitSmartCommitService.dropStash(localPath, index);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "删除暂存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取文件 Diff
     * POST /api/projects/{id}/git/diff
     */
    @PostMapping("/{id}/git/diff")
    public ResponseEntity<Map<String, Object>> getFileDiff(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String filePath = (String) requestBody.get("filePath");
            Boolean staged = (Boolean) requestBody.getOrDefault("staged", false);
            
            Map<String, Object> result = gitSmartCommitService.getFileDiff(localPath, filePath, staged);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取Diff失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 暂存单个文件
     * POST /api/projects/{id}/git/stage
     */
    @PostMapping("/{id}/git/stage")
    public ResponseEntity<Map<String, Object>> stageFile(
            @PathVariable Long id,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String filePath = (String) requestBody.get("filePath");
            
            List<String> files = new ArrayList<>();
            files.add(filePath);
            
            Map<String, Object> result = gitSmartCommitService.stageFiles(localPath, files);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "暂存文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 取消暂存文件
     * POST /api/projects/{id}/git/unstage
     */
    @PostMapping("/{id}/git/unstage")
    public ResponseEntity<Map<String, Object>> unstageFile(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String filePath = requestBody.get("filePath");
            
            Map<String, Object> result = gitSmartCommitService.unstageFile(localPath, filePath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "取消暂存失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 放弃文件修改
     * POST /api/projects/{id}/git/discard
     */
    @PostMapping("/{id}/git/discard")
    public ResponseEntity<Map<String, Object>> discardChanges(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String filePath = requestBody.get("filePath");
            
            Map<String, Object> result = gitSmartCommitService.discardChanges(localPath, filePath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "放弃修改失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 回退到某个提交
     * POST /api/projects/{id}/git/reset
     */
    @PostMapping("/{id}/git/reset")
    public ResponseEntity<Map<String, Object>> resetToCommit(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String commitHash = requestBody.get("commitHash");
            String mode = requestBody.getOrDefault("mode", "mixed");
            
            Map<String, Object> result = gitSmartCommitService.resetToCommit(localPath, commitHash, mode);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "回退失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 回退单个文件到某个提交的状态
     * POST /api/projects/{id}/git/revert-file
     */
    @PostMapping("/{id}/git/revert-file")
    public ResponseEntity<Map<String, Object>> revertSingleFile(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String commitHash = requestBody.get("commitHash");
            String filePath = requestBody.get("filePath");
            
            if (commitHash == null || filePath == null) {
                response.put("success", false);
                response.put("message", "缺少必要参数");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitSmartCommitService.revertFileToCommit(localPath, commitHash, filePath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "回退文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Cherry-pick 拣选提交
     * POST /api/projects/{id}/git/cherry-pick
     */
    @PostMapping("/{id}/git/cherry-pick")
    public ResponseEntity<Map<String, Object>> cherryPick(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String commitHash = requestBody.get("commitHash");
            
            if (commitHash == null) {
                response.put("success", false);
                response.put("message", "缺少提交哈希");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitSmartCommitService.cherryPick(localPath, commitHash);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Cherry-pick 失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取当前冲突文件列表
     * GET /api/projects/{id}/git/conflicts
     */
    @GetMapping("/{id}/git/conflicts")
    public ResponseEntity<Map<String, Object>> getConflictFiles(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.getConflictFiles(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取冲突文件失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 获取冲突文件内容
     * GET /api/projects/{id}/git/conflict-content
     */
    @GetMapping("/{id}/git/conflict-content")
    public ResponseEntity<Map<String, Object>> getConflictContent(
            @PathVariable Long id,
            @RequestParam String filePath) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.getConflictFileContent(localPath, filePath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "获取文件内容失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 标记文件为已解决
     * POST /api/projects/{id}/git/mark-resolved
     */
    @PostMapping("/{id}/git/mark-resolved")
    public ResponseEntity<Map<String, Object>> markFileResolved(
            @PathVariable Long id,
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            String filePath = requestBody.get("filePath");
            
            if (filePath == null) {
                response.put("success", false);
                response.put("message", "缺少文件路径");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> result = gitSmartCommitService.markFileResolved(localPath, filePath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "标记失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 中止当前操作（rebase/merge/cherry-pick）
     * POST /api/projects/{id}/git/abort-operation
     */
    @PostMapping("/{id}/git/abort-operation")
    public ResponseEntity<Map<String, Object>> abortOperation(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.abortCurrentOperation(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "中止操作失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 继续当前操作（解决冲突后）
     * POST /api/projects/{id}/git/continue-operation
     */
    @PostMapping("/{id}/git/continue-operation")
    public ResponseEntity<Map<String, Object>> continueOperation(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new RuntimeException("项目不存在"));
            
            String localPath = project.getLocalPath();
            Map<String, Object> result = gitSmartCommitService.continueCurrentOperation(localPath);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "继续操作失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
