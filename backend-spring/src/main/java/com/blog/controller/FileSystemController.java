package com.blog.controller;

import com.blog.dto.DirectoryItem;
import com.blog.dto.GitCommitInfo;
import com.blog.dto.ProjectAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文件系统控制器 - 用于浏览文件夹和解析项目
 */
@RestController
@RequestMapping("/api/filesystem")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"})
public class FileSystemController {
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 浏览指定路径的文件夹和文件
     * GET /api/filesystem/browse?path=E:/projects
     */
    @GetMapping("/browse")
    public ResponseEntity<List<DirectoryItem>> browseDirectory(@RequestParam(required = false) String path) {
        try {
            System.out.println("=== 浏览文件夹 ===");
            System.out.println("路径: " + path);
            
            // 如果路径为空，返回磁盘根目录列表
            if (path == null || path.isEmpty()) {
                List<DirectoryItem> roots = Arrays.stream(File.listRoots())
                    .map(root -> new DirectoryItem(
                        root.getPath(),
                        root.getAbsolutePath(),
                        "folder",
                        null
                    ))
                    .collect(Collectors.toList());
                return ResponseEntity.ok(roots);
            }
            
            File directory = new File(path);
            
            if (!directory.exists() || !directory.isDirectory()) {
                return ResponseEntity.badRequest().build();
            }
            
            File[] files = directory.listFiles();
            if (files == null) {
                return ResponseEntity.ok(new ArrayList<>());
            }
            
            List<DirectoryItem> items = Arrays.stream(files)
                .filter(file -> !file.isHidden()) // 过滤隐藏文件
                .sorted((a, b) -> {
                    // 文件夹排在前面
                    if (a.isDirectory() && !b.isDirectory()) return -1;
                    if (!a.isDirectory() && b.isDirectory()) return 1;
                    return a.getName().compareToIgnoreCase(b.getName());
                })
                .map(file -> new DirectoryItem(
                    file.getName(),
                    file.getAbsolutePath(),
                    file.isDirectory() ? "folder" : "file",
                    file.isFile() ? file.length() : null
                ))
                .collect(Collectors.toList());
            
            System.out.println("找到 " + items.size() + " 个项目");
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            System.err.println("浏览文件夹失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 解析项目 - 读取README.md和Git记录
     * POST /api/filesystem/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<ProjectAnalysisResult> analyzeProject(@RequestBody String projectPath) {
        try {
            System.out.println("=== 解析项目 ===");
            System.out.println("项目路径: " + projectPath);
            
            // 去除可能的引号
            projectPath = projectPath.replace("\"", "").trim();
            
            File projectDir = new File(projectPath);
            if (!projectDir.exists() || !projectDir.isDirectory()) {
                System.err.println("项目路径不存在或不是文件夹");
                return ResponseEntity.badRequest().build();
            }
            
            ProjectAnalysisResult result = new ProjectAnalysisResult();
            result.setLocalPath(projectPath);
            
            // 1. 解析项目名称（从路径获取）
            String projectName = projectDir.getName();
            result.setProjectName(projectName);
            System.out.println("项目名称: " + projectName);
            
            // 2. 读取README.md
            File readmeFile = new File(projectDir, "README.md");
            if (!readmeFile.exists()) {
                // 尝试小写
                readmeFile = new File(projectDir, "readme.md");
            }
            if (!readmeFile.exists()) {
                // 尝试大写
                readmeFile = new File(projectDir, "Readme.md");
            }
            
            if (readmeFile.exists() && readmeFile.isFile()) {
                byte[] bytes = Files.readAllBytes(readmeFile.toPath());
                String readmeContent = new String(bytes, "UTF-8");
                result.setReadmeContent(readmeContent);
                System.out.println("README.md 读取成功，长度: " + readmeContent.length());
            } else {
                System.out.println("未找到 README.md 文件");
            }
            
            // 3. 获取Git提交记录
            List<GitCommitInfo> commits = getGitCommits(projectPath);
            result.setGitCommits(commits);
            System.out.println("Git提交记录数量: " + commits.size());
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("解析项目失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
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
            
            // 执行git log命令获取最近50条提交记录
            ProcessBuilder pb = new ProcessBuilder(
                "git", "log", 
                "--max-count=50",
                "--pretty=format:%H|%an|%ad|%s",
                "--date=iso"
            );
            pb.directory(new File(projectPath));
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"))) {
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
        }
        
        return commits;
    }
}
