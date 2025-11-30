package com.blog.controller;

import com.blog.dto.GitCommitInfo;
import com.blog.dto.GitStatusResult;
import com.blog.entity.Project;
import com.blog.service.ProjectService;
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
            
            // 执行git log命令获取提交记录 - 获取最近500条
            ProcessBuilder pb = new ProcessBuilder(
                "git", "log", 
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
}
