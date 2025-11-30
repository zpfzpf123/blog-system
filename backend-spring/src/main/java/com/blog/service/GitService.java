package com.blog.service;

import com.blog.entity.GitUser;
import com.blog.repository.GitUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Git服务类 - 提供智能Git操作功能
 * 
 * @author 开发团队
 * @since 2024-11-30
 */
@Service
public class GitService {
    
    @Autowired
    private GitUserRepository gitUserRepository;
    
    /**
     * 检查Git状态
     * 
     * @param projectPath 项目本地路径
     * @return Git状态信息
     */
    public Map<String, Object> checkGitStatus(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File projectDir = new File(projectPath);
            if (!projectDir.exists()) {
                result.put("success", false);
                result.put("message", "项目路径不存在");
                return result;
            }
            
            // 检查是否是Git仓库
            File gitDir = new File(projectDir, ".git");
            if (!gitDir.exists()) {
                result.put("success", false);
                result.put("message", "当前项目不是Git仓库");
                return result;
            }
            
            // 执行git status命令
            ProcessBuilder pb = new ProcessBuilder("git", "status", "--porcelain");
            pb.directory(projectDir);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            
            List<String> modifiedFiles = new ArrayList<>();
            List<String> addedFiles = new ArrayList<>();
            List<String> deletedFiles = new ArrayList<>();
            List<String> untrackedFiles = new ArrayList<>();
            
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.length() < 3) continue;
                
                String status = line.substring(0, 2);
                String filePath = line.substring(3);
                
                if (status.contains("M")) {
                    modifiedFiles.add(filePath);
                } else if (status.contains("A")) {
                    addedFiles.add(filePath);
                } else if (status.contains("D")) {
                    deletedFiles.add(filePath);
                } else if (status.contains("?")) {
                    untrackedFiles.add(filePath);
                }
            }
            
            int exitCode = process.waitFor();
            reader.close();
            
            boolean hasChanges = !modifiedFiles.isEmpty() || !addedFiles.isEmpty() || 
                               !deletedFiles.isEmpty() || !untrackedFiles.isEmpty();
            
            result.put("success", true);
            result.put("hasChanges", hasChanges);
            result.put("modifiedFiles", modifiedFiles);
            result.put("addedFiles", addedFiles);
            result.put("deletedFiles", deletedFiles);
            result.put("untrackedFiles", untrackedFiles);
            result.put("totalChanges", modifiedFiles.size() + addedFiles.size() + deletedFiles.size() + untrackedFiles.size());
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "检查Git状态失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 智能生成提交信息
     * 根据文件变更类型生成符合规范的提交信息
     * 
     * @param statusInfo Git状态信息
     * @return 提交信息
     */
    public String generateCommitMessage(Map<String, Object> statusInfo) {
        @SuppressWarnings("unchecked")
        List<String> modifiedFiles = (List<String>) statusInfo.get("modifiedFiles");
        @SuppressWarnings("unchecked")
        List<String> addedFiles = (List<String>) statusInfo.get("addedFiles");
        @SuppressWarnings("unchecked")
        List<String> deletedFiles = (List<String>) statusInfo.get("deletedFiles");
        @SuppressWarnings("unchecked")
        List<String> untrackedFiles = (List<String>) statusInfo.get("untrackedFiles");
        
        // 分析变更类型
        String type = determineCommitType(modifiedFiles, addedFiles, deletedFiles, untrackedFiles);
        String scope = determineCommitScope(modifiedFiles, addedFiles, deletedFiles, untrackedFiles);
        String description = generateCommitDescription(modifiedFiles, addedFiles, deletedFiles, untrackedFiles);
        
        // 构建符合Conventional Commits规范的提交信息
        StringBuilder message = new StringBuilder();
        message.append(type);
        
        if (scope != null && !scope.isEmpty()) {
            message.append("(").append(scope).append(")");
        }
        
        message.append(": ").append(description);
        
        // 添加详细信息
        List<String> details = new ArrayList<>();
        if (!modifiedFiles.isEmpty()) {
            details.add("- 修改了 " + modifiedFiles.size() + " 个文件");
        }
        if (!addedFiles.isEmpty() || !untrackedFiles.isEmpty()) {
            details.add("- 新增了 " + (addedFiles.size() + untrackedFiles.size()) + " 个文件");
        }
        if (!deletedFiles.isEmpty()) {
            details.add("- 删除了 " + deletedFiles.size() + " 个文件");
        }
        
        if (!details.isEmpty()) {
            message.append("\n\n");
            message.append(String.join("\n", details));
        }
        
        return message.toString();
    }
    
    /**
     * 确定提交类型
     */
    private String determineCommitType(List<String> modified, List<String> added, List<String> deleted, List<String> untracked) {
        // 检查是否有新增文件
        boolean hasNewFiles = !added.isEmpty() || !untracked.isEmpty();
        boolean hasModified = !modified.isEmpty();
        boolean hasDeleted = !deleted.isEmpty();
        
        // 分析文件类型
        boolean hasDocFiles = hasDocFiles(modified, added);
        boolean hasStyleFiles = hasStyleFiles(modified, added);
        boolean hasTestFiles = hasTestFiles(modified, added);
        
        if (hasDocFiles && modified.size() + added.size() + deleted.size() + untracked.size() <= 2) {
            return "docs";
        }
        
        if (hasTestFiles) {
            return "test";
        }
        
        if (hasStyleFiles && !hasNewFiles && modified.size() <= 3) {
            return "style";
        }
        
        if (hasDeleted && !hasNewFiles && modified.isEmpty()) {
            return "refactor";
        }
        
        if (hasNewFiles) {
            return "feat";
        }
        
        if (hasModified && !hasNewFiles) {
            return "fix";
        }
        
        return "chore";
    }
    
    /**
     * 检查是否有文档文件
     */
    private boolean hasDocFiles(List<String>... fileLists) {
        for (List<String> files : fileLists) {
            if (files == null) continue;
            for (String file : files) {
                String lower = file.toLowerCase();
                if (lower.endsWith(".md") || lower.contains("readme") || lower.contains("doc")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检查是否有样式文件
     */
    private boolean hasStyleFiles(List<String>... fileLists) {
        for (List<String> files : fileLists) {
            if (files == null) continue;
            for (String file : files) {
                String lower = file.toLowerCase();
                if (lower.endsWith(".css") || lower.endsWith(".scss") || lower.endsWith(".less")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检查是否有测试文件
     */
    private boolean hasTestFiles(List<String>... fileLists) {
        for (List<String> files : fileLists) {
            if (files == null) continue;
            for (String file : files) {
                String lower = file.toLowerCase();
                if (lower.contains("test") || lower.contains("spec")) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 确定提交范围
     */
    private String determineCommitScope(List<String> modified, List<String> added, List<String> deleted, List<String> untracked) {
        Set<String> scopes = new HashSet<>();
        
        List<String> allFiles = new ArrayList<>();
        allFiles.addAll(modified);
        allFiles.addAll(added);
        allFiles.addAll(deleted);
        allFiles.addAll(untracked);
        
        for (String file : allFiles) {
            if (file.contains("controller") || file.contains("Controller")) {
                scopes.add("controller");
            } else if (file.contains("service") || file.contains("Service")) {
                scopes.add("service");
            } else if (file.contains("component") || file.contains("Component")) {
                scopes.add("component");
            } else if (file.contains("view") || file.contains("View") || file.contains("page")) {
                scopes.add("view");
            } else if (file.contains("api") || file.contains("API")) {
                scopes.add("api");
            } else if (file.contains("util") || file.contains("Utils")) {
                scopes.add("utils");
            } else if (file.contains("config") || file.contains("Config")) {
                scopes.add("config");
            }
        }
        
        if (scopes.isEmpty()) {
            return null;
        }
        
        if (scopes.size() == 1) {
            return scopes.iterator().next();
        }
        
        return String.join(",", scopes);
    }
    
    /**
     * 生成提交描述
     */
    private String generateCommitDescription(List<String> modified, List<String> added, List<String> deleted, List<String> untracked) {
        int totalChanges = modified.size() + added.size() + deleted.size() + untracked.size();
        
        if (!added.isEmpty() || !untracked.isEmpty()) {
            return "添加新功能和文件";
        }
        
        if (!deleted.isEmpty() && modified.isEmpty()) {
            return "删除废弃代码";
        }
        
        if (!modified.isEmpty()) {
            if (totalChanges <= 2) {
                return "优化代码实现";
            } else if (totalChanges <= 5) {
                return "更新多个模块功能";
            } else {
                return "大规模重构和优化";
            }
        }
        
        return "更新项目文件";
    }
    
    /**
     * 执行git fetch获取远程更新
     * 
     * @param projectPath 项目路径
     * @return 执行结果
     */
    public Map<String, Object> fetchFromRemote(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            if (!projectDir.exists()) {
                result.put("success", false);
                result.put("message", "项目目录不存在: " + projectPath);
                result.put("logs", logs);
                return result;
            }
            
            // 执行git fetch
            logs.add("执行: git fetch origin");
            Map<String, Object> fetchResult = executeGitCommand(projectDir, "git", "fetch", "origin");
            logs.addAll((List<String>) fetchResult.get("output"));
            
            if (!(Boolean) fetchResult.get("success")) {
                result.put("success", false);
                result.put("message", "获取远程更新失败");
                result.put("logs", logs);
                return result;
            }
            
            result.put("success", true);
            result.put("message", "成功获取远程更新");
            result.put("logs", logs);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "执行Fetch操作失败: " + e.getMessage());
            result.put("logs", logs);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 从远程仓库拉取最新代码（使用rebase）
     * 
     * @param projectPath 项目路径
     * @return 执行结果
     */
    public Map<String, Object> pullFromRemote(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            if (!projectDir.exists()) {
                result.put("success", false);
                result.put("message", "项目目录不存在: " + projectPath);
                result.put("logs", logs);
                return result;
            }
            
            // 执行git pull --rebase
            logs.add("执行: git pull --rebase origin");
            Map<String, Object> pullResult = executeGitCommand(projectDir, "git", "pull", "--rebase", "origin");
            logs.addAll((List<String>) pullResult.get("output"));
            
            if (!(Boolean) pullResult.get("success")) {
                // 检查是否有冲突
                String output = String.join("\n", (List<String>) pullResult.get("output"));
                if (output.contains("CONFLICT") || output.contains("conflict")) {
                    // 获取冲突文件列表
                    List<String> conflictFiles = getConflictFiles(projectPath);
                    
                    result.put("success", false);
                    result.put("hasConflict", true);
                    result.put("conflictFiles", conflictFiles);
                    result.put("message", "代码拉取时检测到 " + conflictFiles.size() + " 个冲突文件");
                    result.put("logs", logs);
                    return result;
                }
                
                result.put("success", false);
                result.put("message", "拉取远程代码失败");
                result.put("logs", logs);
                return result;
            }
            
            result.put("success", true);
            result.put("message", "代码拉取成功");
            result.put("logs", logs);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "执行Pull操作失败: " + e.getMessage());
            result.put("logs", logs);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 获取冲突文件列表
     * 
     * @param projectPath 项目路径
     * @return 冲突文件列表
     */
    public List<String> getConflictFiles(String projectPath) {
        List<String> conflictFiles = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            // 执行 git diff --name-only --diff-filter=U
            Map<String, Object> result = executeGitCommand(projectDir, "git", "diff", "--name-only", "--diff-filter=U");
            
            if ((Boolean) result.get("success")) {
                List<String> output = (List<String>) result.get("output");
                for (String line : output) {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty()) {
                        conflictFiles.add(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return conflictFiles;
    }
    
    /**
     * 解决冲突文件（使用指定策略）
     * 
     * @param projectPath 项目路径
     * @param filePath 文件路径
     * @param strategy 解决策略：ours(使用本地) / theirs(使用远程)
     * @return 执行结果
     */
    public Map<String, Object> resolveConflict(String projectPath, String filePath, String strategy) {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            if ("ours".equals(strategy)) {
                logs.add("使用本地版本解决冲突: " + filePath);
                Map<String, Object> checkoutResult = executeGitCommand(projectDir, "git", "checkout", "--ours", filePath);
                logs.addAll((List<String>) checkoutResult.get("output"));
                
                if (!(Boolean) checkoutResult.get("success")) {
                    result.put("success", false);
                    result.put("message", "解决冲突失败");
                    result.put("logs", logs);
                    return result;
                }
            } else if ("theirs".equals(strategy)) {
                logs.add("使用远程版本解决冲突: " + filePath);
                Map<String, Object> checkoutResult = executeGitCommand(projectDir, "git", "checkout", "--theirs", filePath);
                logs.addAll((List<String>) checkoutResult.get("output"));
                
                if (!(Boolean) checkoutResult.get("success")) {
                    result.put("success", false);
                    result.put("message", "解决冲突失败");
                    result.put("logs", logs);
                    return result;
                }
            }
            
            // 添加已解决的文件
            logs.add("暂存已解决的文件...");
            Map<String, Object> addResult = executeGitCommand(projectDir, "git", "add", filePath);
            logs.addAll((List<String>) addResult.get("output"));
            
            result.put("success", true);
            result.put("message", "冲突已解决");
            result.put("logs", logs);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "解决冲突失败: " + e.getMessage());
            result.put("logs", logs);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 继续rebase操作
     * 
     * @param projectPath 项目路径
     * @return 执行结果
     */
    public Map<String, Object> continueRebase(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            logs.add("执行: git rebase --continue");
            Map<String, Object> rebaseResult = executeGitCommand(projectDir, "git", "rebase", "--continue");
            logs.addAll((List<String>) rebaseResult.get("output"));
            
            if (!(Boolean) rebaseResult.get("success")) {
                result.put("success", false);
                result.put("message", "继续rebase失败");
                result.put("logs", logs);
                return result;
            }
            
            result.put("success", true);
            result.put("message", "Rebase继续成功");
            result.put("logs", logs);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "继续rebase失败: " + e.getMessage());
            result.put("logs", logs);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 中止rebase操作
     * 
     * @param projectPath 项目路径
     * @return 执行结果
     */
    public Map<String, Object> abortRebase(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            logs.add("执行: git rebase --abort");
            Map<String, Object> rebaseResult = executeGitCommand(projectDir, "git", "rebase", "--abort");
            logs.addAll((List<String>) rebaseResult.get("output"));
            
            if (!(Boolean) rebaseResult.get("success")) {
                result.put("success", false);
                result.put("message", "中止rebase失败");
                result.put("logs", logs);
                return result;
            }
            
            result.put("success", true);
            result.put("message", "已中止rebase操作");
            result.put("logs", logs);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "中止rebase失败: " + e.getMessage());
            result.put("logs", logs);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 执行Git提交和推送（支持选择性提交、分支选择、自动重试和Personal Access Token认证）
     * 
     * @param projectPath 项目路径
     * @param commitMessage 提交信息
     * @param selectedFiles 选择要提交的文件列表（null表示提交所有）
     * @param targetBranch 目标分支（null表示当前分支）
     * @param shouldPush 是否推送到远程
     * @param maxRetries 最大重试次数
     * @param gitUserId Git用户ID（用于认证）
     * @return 执行结果
     */
    public Map<String, Object> commitAndPush(String projectPath, String commitMessage, List<String> selectedFiles, String targetBranch, boolean shouldPush, int maxRetries, Long gitUserId) {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            if (!projectDir.exists()) {
                result.put("success", false);
                result.put("message", "项目目录不存在: " + projectPath);
                result.put("logs", logs);
                return result;
            }
            
            // 获取Git用户凭证
            GitUser gitUser = null;
            if (gitUserId != null) {
                gitUser = gitUserRepository.findById(gitUserId).orElse(null);
            }
            if (gitUser == null) {
                // 如果没有指定或找不到，使用默认用户
                gitUser = gitUserRepository.findByIsDefaultTrue().orElse(null);
            }
            
            // 配置Git凭证（如果有的话）
            String originalRemoteUrl = null;
            if (gitUser != null) {
                logs.add("使用Git用户: " + gitUser.getName());
                originalRemoteUrl = configureGitCredentials(projectDir, gitUser);
            }
            
            // 1. git pull - 先拉取最新代码
            logs.add("正在拉取远程代码...");
            Map<String, Object> pullResult = executeGitCommand(projectDir, "git", "pull", "--rebase");
            logs.addAll((List<String>) pullResult.get("output"));
            
            if (!(Boolean) pullResult.get("success")) {
                // 检查是否有冲突
                if (pullResult.get("output").toString().contains("CONFLICT")) {
                    result.put("success", false);
                    result.put("hasConflict", true);
                    result.put("message", "检测到代码冲突，请手动解决冲突后继续");
                    result.put("logs", logs);
                    return result;
                }
            }
            
            // 2. git add - 根据选择添加文件
            logs.add("\n正在暂存文件...");
            Map<String, Object> addResult;
            
            if (selectedFiles == null || selectedFiles.isEmpty()) {
                // 添加所有文件
                addResult = executeGitCommand(projectDir, "git", "add", "-A");
            } else {
                // 只添加选中的文件
                List<String> addCommand = new ArrayList<>();
                addCommand.add("git");
                addCommand.add("add");
                addCommand.addAll(selectedFiles);
                addResult = executeGitCommand(projectDir, addCommand.toArray(new String[0]));
                logs.add("已添加 " + selectedFiles.size() + " 个文件");
            }
            
            logs.addAll((List<String>) addResult.get("output"));
            
            if (!(Boolean) addResult.get("success")) {
                result.put("success", false);
                result.put("message", "暂存文件失败");
                result.put("logs", logs);
                return result;
            }
            
            // 3. git commit
            logs.add("\n正在提交代码...");
            Map<String, Object> commitResult = executeGitCommand(projectDir, "git", "commit", "-m", commitMessage);
            logs.addAll((List<String>) commitResult.get("output"));
            
            if (!(Boolean) commitResult.get("success")) {
                // 检查是否没有变更
                if (commitResult.get("output").toString().contains("nothing to commit")) {
                    result.put("success", false);
                    result.put("message", "没有需要提交的变更");
                    result.put("logs", logs);
                    return result;
                }
                
                result.put("success", false);
                result.put("message", "提交代码失败");
                result.put("logs", logs);
                return result;
            }
            
            // 4. git push - 支持自动重试（如果shouldPush为true）
            int retryCount = 0;
            
            if (shouldPush) {
                logs.add("\n正在推送到远程仓库...");
                
                // 如果指定了目标分支，推送到指定分支
                boolean pushSuccess = false;
                
                while (!pushSuccess && retryCount <= maxRetries) {
                    if (retryCount > 0) {
                        logs.add("\n第 " + retryCount + " 次重试推送...");
                        Thread.sleep(2000); // 等待2秒后重试
                    }
                    
                    Map<String, Object> pushResult;
                    if (targetBranch != null && !targetBranch.trim().isEmpty()) {
                        pushResult = executeGitCommand(projectDir, "git", "push", "origin", "HEAD:" + targetBranch);
                    } else {
                        pushResult = executeGitCommand(projectDir, "git", "push");
                    }
                    logs.addAll((List<String>) pushResult.get("output"));
                    
                    if ((Boolean) pushResult.get("success")) {
                        pushSuccess = true;
                    } else {
                        retryCount++;
                        if (retryCount <= maxRetries) {
                            logs.add("推送失败，准备重试...");
                        }
                    }
                }
                
                if (!pushSuccess) {
                    result.put("success", false);
                    result.put("message", "推送到远程仓库失败（已重试 " + retryCount + " 次），但代码已成功提交到本地");
                    result.put("logs", logs);
                    result.put("retryCount", retryCount);
                    return result;
                }
                
                logs.add("\n✅ 代码提交并推送成功！");
                if (retryCount > 0) {
                    logs.add("（重试了 " + retryCount + " 次）");
                }
                result.put("message", "代码已成功提交并推送到远程仓库");
            } else {
                logs.add("\n✅ 代码本地提交成功！");
                logs.add("（已跳过推送到远程仓库）");
                result.put("message", "代码已成功提交到本地仓库");
            }
            
            result.put("success", true);
            result.put("logs", logs);
            result.put("retryCount", retryCount);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "执行Git操作失败: " + e.getMessage());
            result.put("logs", logs);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 兼容旧版本的方法（无targetBranch和shouldPush）
     */
    public Map<String, Object> commitAndPush(String projectPath, String commitMessage, List<String> selectedFiles, int maxRetries, Long gitUserId) {
        return commitAndPush(projectPath, commitMessage, selectedFiles, null, true, maxRetries, gitUserId);
    }
    
    /**
     * 兼容旧版本的方法（只有基本参数）
     */
    public Map<String, Object> commitAndPush(String projectPath, String commitMessage) {
        return commitAndPush(projectPath, commitMessage, null, null, true, 0, null);
    }
    
    /**
     * 执行Git命令
     */
    private Map<String, Object> executeGitCommand(File workDir, String... command) {
        Map<String, Object> result = new HashMap<>();
        List<String> output = new ArrayList<>();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir);
            pb.redirectErrorStream(true);
            
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8"));
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.add(line);
            }
            
            int exitCode = process.waitFor();
            reader.close();
            
            result.put("success", exitCode == 0);
            result.put("exitCode", exitCode);
            result.put("output", output);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("output", Arrays.asList("执行命令失败: " + e.getMessage()));
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 解决冲突后继续提交
     */
    public Map<String, Object> continueAfterConflict(String projectPath, String commitMessage) {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            // 检查是否还有冲突
            Map<String, Object> statusResult = checkGitStatus(projectPath);
            if ((Boolean) statusResult.get("success")) {
                // 继续rebase
                logs.add("正在继续rebase...");
                Map<String, Object> continueResult = executeGitCommand(projectDir, "git", "rebase", "--continue");
                logs.addAll((List<String>) continueResult.get("output"));
                
                // 然后执行push
                logs.add("\n正在推送到远程仓库...");
                Map<String, Object> pushResult = executeGitCommand(projectDir, "git", "push");
                logs.addAll((List<String>) pushResult.get("output"));
                
                if ((Boolean) pushResult.get("success")) {
                    logs.add("\n✅ 代码提交成功！");
                    result.put("success", true);
                    result.put("message", "冲突已解决，代码成功推送");
                } else {
                    result.put("success", false);
                    result.put("message", "推送失败");
                }
            } else {
                result.put("success", false);
                result.put("message", "检查状态失败");
            }
            
            result.put("logs", logs);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
            result.put("logs", logs);
        }
        
        return result;
    }
    
    /**
     * 获取Git分支列表
     * 
     * @param projectPath 项目路径
     * @return 分支信息
     */
    public Map<String, Object> getBranches(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File projectDir = new File(projectPath);
            
            // 获取当前分支
            Map<String, Object> currentBranchResult = executeGitCommand(projectDir, "git", "branch", "--show-current");
            String currentBranch = "";
            if ((Boolean) currentBranchResult.get("success")) {
                List<String> output = (List<String>) currentBranchResult.get("output");
                if (!output.isEmpty()) {
                    currentBranch = output.get(0).trim();
                }
            }
            
            // 获取所有本地分支
            Map<String, Object> branchesResult = executeGitCommand(projectDir, "git", "branch");
            List<String> branches = new ArrayList<>();
            
            if ((Boolean) branchesResult.get("success")) {
                List<String> output = (List<String>) branchesResult.get("output");
                for (String line : output) {
                    String branch = line.trim().replace("* ", "");
                    if (!branch.isEmpty()) {
                        branches.add(branch);
                    }
                }
            }
            
            // 获取所有远程分支
            Map<String, Object> remoteBranchesResult = executeGitCommand(projectDir, "git", "branch", "-r");
            List<String> remoteBranches = new ArrayList<>();
            
            if ((Boolean) remoteBranchesResult.get("success")) {
                List<String> output = (List<String>) remoteBranchesResult.get("output");
                for (String line : output) {
                    String branch = line.trim();
                    if (!branch.isEmpty() && !branch.contains("HEAD ->")) {
                        // 移除origin/前缀
                        branch = branch.replace("origin/", "");
                        if (!branches.contains(branch) && !remoteBranches.contains(branch)) {
                            remoteBranches.add(branch);
                        }
                    }
                }
            }
            
            result.put("success", true);
            result.put("currentBranch", currentBranch);
            result.put("localBranches", branches);
            result.put("remoteBranches", remoteBranches);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取分支列表失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 切换分支
     * 
     * @param projectPath 项目路径
     * @param branchName 分支名称
     * @return 执行结果
     */
    public Map<String, Object> switchBranch(String projectPath, String branchName) {
        Map<String, Object> result = new HashMap<>();
        List<String> logs = new ArrayList<>();
        
        try {
            File projectDir = new File(projectPath);
            
            // 检查是否有未提交的变更
            Map<String, Object> statusResult = checkGitStatus(projectPath);
            if ((Boolean) statusResult.get("hasChanges")) {
                result.put("success", false);
                result.put("message", "有未提交的变更，请先提交或暂存");
                result.put("hasChanges", true);
                return result;
            }
            
            // 切换分支
            logs.add("正在切换到分支: " + branchName);
            Map<String, Object> checkoutResult = executeGitCommand(projectDir, "git", "checkout", branchName);
            logs.addAll((List<String>) checkoutResult.get("output"));
            
            if ((Boolean) checkoutResult.get("success")) {
                logs.add("\n✅ 分支切换成功！");
                result.put("success", true);
                result.put("message", "已切换到分支: " + branchName);
                result.put("currentBranch", branchName);
            } else {
                result.put("success", false);
                result.put("message", "切换分支失败");
            }
            
            result.put("logs", logs);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "切换分支失败: " + e.getMessage());
            result.put("logs", logs);
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 保存Git忽略规则
     * 
     * @param projectPath 项目路径
     * @param ignoreFiles 要忽略的文件列表
     * @return 执行结果
     */
    public Map<String, Object> saveIgnoreRules(String projectPath, List<String> ignoreFiles) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File projectDir = new File(projectPath);
            File configFile = new File(projectDir, ".git-commit-config.json");
            
            Map<String, Object> config = new HashMap<>();
            config.put("ignoreFiles", ignoreFiles);
            config.put("updateTime", System.currentTimeMillis());
            
            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
            
            result.put("success", true);
            result.put("message", "忽略规则已保存");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "保存忽略规则失败: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 读取Git忽略规则
     * 
     * @param projectPath 项目路径
     * @return 忽略的文件列表
     */
    public Map<String, Object> loadIgnoreRules(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            File projectDir = new File(projectPath);
            File configFile = new File(projectDir, ".git-commit-config.json");
            
            if (!configFile.exists()) {
                result.put("success", true);
                result.put("ignoreFiles", new ArrayList<String>());
                return result;
            }
            
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> config = mapper.readValue(configFile, Map.class);
            
            result.put("success", true);
            result.put("ignoreFiles", config.get("ignoreFiles"));
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "读取忽略规则失败: " + e.getMessage());
            result.put("ignoreFiles", new ArrayList<String>());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 配置Git凭证（使用Personal Access Token）
     * 
     * @param projectDir 项目目录
     * @param gitUser Git用户
     * @return 原始的remote URL（用于恢复）
     */
    private String configureGitCredentials(File projectDir, GitUser gitUser) {
        try {
            // 获取当前remote URL
            Map<String, Object> getUrlResult = executeGitCommand(projectDir, "git", "remote", "get-url", "origin");
            
            if (!(Boolean) getUrlResult.get("success")) {
                return null;
            }
            
            List<String> output = (List<String>) getUrlResult.get("output");
            if (output.isEmpty()) {
                return null;
            }
            
            String originalUrl = output.get(0).trim();
            
            // 如果URL已经包含认证信息，先移除
            String cleanUrl = originalUrl.replaceAll("https://[^@]+@", "https://");
            
            // 如果是HTTPS URL，添加token
            if (cleanUrl.startsWith("https://")) {
                String token = gitUser.getPassword();
                String authenticatedUrl = cleanUrl.replace("https://", "https://" + token + "@");
                
                // 设置新的remote URL
                executeGitCommand(projectDir, "git", "remote", "set-url", "origin", authenticatedUrl);
                
                // 配置用户信息（如果有）
                if (gitUser.getUsername() != null && !gitUser.getUsername().isEmpty()) {
                    executeGitCommand(projectDir, "git", "config", "user.name", gitUser.getUsername());
                }
                if (gitUser.getEmail() != null && !gitUser.getEmail().isEmpty()) {
                    executeGitCommand(projectDir, "git", "config", "user.email", gitUser.getEmail());
                }
            }
            
            return originalUrl;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 恢复原始的remote URL
     * 
     * @param projectDir 项目目录
     * @param originalUrl 原始URL
     */
    private void restoreOriginalRemoteUrl(File projectDir, String originalUrl) {
        if (originalUrl != null) {
            try {
                executeGitCommand(projectDir, "git", "remote", "set-url", "origin", originalUrl);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
