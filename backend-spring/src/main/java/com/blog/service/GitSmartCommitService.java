package com.blog.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Git智能提交服务
 * 提供完整的Git提交流程管理
 * 
 * @author 开发团队
 * @since 2024-12-06
 */
@Service
public class GitSmartCommitService {
    
    /**
     * 执行Git命令的通用方法
     */
    private Map<String, Object> executeGitCommand(File workDir, String... command) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(workDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            
            result.put("exitCode", exitCode);
            result.put("output", output.toString());
            result.put("success", exitCode == 0);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("output", "");
        }
        
        return result;
    }
    
    /**
     * 检查工作区状态
     */
    public Map<String, Object> checkWorkingDirectory(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // 检查目录是否存在
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            result.put("success", false);
            result.put("message", "项目路径不存在");
            return result;
        }
        
        // 检查是否是Git仓库
        File gitDir = new File(projectDir, ".git");
        if (!gitDir.exists()) {
            result.put("success", false);
            result.put("message", "不是Git仓库");
            return result;
        }
        
        // 执行 git status --porcelain
        Map<String, Object> statusResult = executeGitCommand(projectDir, "git", "status", "--porcelain");
        
        if (!(Boolean) statusResult.get("success")) {
            result.put("success", false);
            result.put("message", "无法获取Git状态");
            return result;
        }
        
        String output = (String) statusResult.get("output");
        boolean hasChanges = !output.trim().isEmpty();
        
        // 获取当前分支
        Map<String, Object> branchResult = executeGitCommand(projectDir, "git", "branch", "--show-current");
        String currentBranch = ((String) branchResult.get("output")).trim();
        
        // 解析文件状态
        List<String> modifiedFiles = new ArrayList<>();
        List<String> untrackedFiles = new ArrayList<>();
        List<String> deletedFiles = new ArrayList<>();
        
        if (hasChanges) {
            String[] lines = output.split("\n");
            for (String line : lines) {
                if (line.length() < 3) continue;
                
                String status = line.substring(0, 2);
                String file = line.substring(3);
                
                if (status.contains("M") || status.contains("A")) {
                    modifiedFiles.add(file);
                } else if (status.contains("D")) {
                    deletedFiles.add(file);
                } else if (status.contains("?")) {
                    untrackedFiles.add(file);
                }
            }
        }
        
        result.put("success", true);
        result.put("hasChanges", hasChanges);
        result.put("currentBranch", currentBranch);
        result.put("modifiedFiles", modifiedFiles);
        result.put("untrackedFiles", untrackedFiles);
        result.put("deletedFiles", deletedFiles);
        result.put("totalChanges", modifiedFiles.size() + untrackedFiles.size() + deletedFiles.size());
        
        return result;
    }
    
    /**
     * 暂存文件
     */
    public Map<String, Object> stageFiles(String projectPath, List<String> selectedFiles) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        try {
            if (selectedFiles == null || selectedFiles.isEmpty()) {
                // 暂存所有文件
                Map<String, Object> addResult = executeGitCommand(projectDir, "git", "add", ".");
                result.put("success", addResult.get("success"));
                result.put("message", (Boolean) addResult.get("success") ? "已暂存所有文件" : "暂存失败");
                result.put("output", addResult.get("output"));
            } else {
                // 暂存指定文件
                List<String> command = new ArrayList<>();
                command.add("git");
                command.add("add");
                command.addAll(selectedFiles);
                
                Map<String, Object> addResult = executeGitCommand(projectDir, command.toArray(new String[0]));
                result.put("success", addResult.get("success"));
                result.put("message", (Boolean) addResult.get("success") ? 
                    "已暂存 " + selectedFiles.size() + " 个文件" : "暂存失败");
                result.put("output", addResult.get("output"));
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "暂存文件失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 拉取远程代码（使用rebase）
     */
    public Map<String, Object> pullWithRebase(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // 先执行 fetch
        Map<String, Object> fetchResult = executeGitCommand(projectDir, "git", "fetch");
        if (!(Boolean) fetchResult.get("success")) {
            result.put("success", false);
            result.put("message", "获取远程更新失败");
            result.put("output", fetchResult.get("output"));
            result.put("step", "fetch");
            return result;
        }
        
        // 执行 pull --rebase
        Map<String, Object> pullResult = executeGitCommand(projectDir, "git", "pull", "--rebase");
        String output = (String) pullResult.get("output");
        
        // 检查是否有冲突
        boolean hasConflict = output.contains("CONFLICT") || 
                             output.contains("conflict") ||
                             output.contains("Merge conflict");
        
        if (hasConflict) {
            // 解析冲突文件
            List<String> conflictFiles = parseConflictFiles(projectDir);
            
            result.put("success", false);
            result.put("hasConflict", true);
            result.put("message", "检测到代码冲突");
            result.put("conflictFiles", conflictFiles);
            result.put("conflictCount", conflictFiles.size());
            result.put("output", output);
            return result;
        }
        
        // 检查是否已是最新
        boolean upToDate = output.contains("Already up to date") || 
                          output.contains("Current branch") ||
                          output.isEmpty();
        
        result.put("success", true);
        result.put("hasConflict", false);
        result.put("upToDate", upToDate);
        result.put("message", upToDate ? "已是最新" : "拉取成功");
        result.put("output", output);
        
        return result;
    }
    
    /**
     * 解析冲突文件列表
     */
    private List<String> parseConflictFiles(File projectDir) {
        List<String> conflictFiles = new ArrayList<>();
        
        // 执行 git diff --name-only --diff-filter=U
        Map<String, Object> diffResult = executeGitCommand(projectDir, 
            "git", "diff", "--name-only", "--diff-filter=U");
        
        if ((Boolean) diffResult.get("success")) {
            String output = (String) diffResult.get("output");
            if (!output.trim().isEmpty()) {
                String[] lines = output.trim().split("\n");
                for (String line : lines) {
                    String trimmedLine = line.trim();
                    // 过滤掉空行和Git警告信息
                    if (!trimmedLine.isEmpty() && !isGitWarningOrInfo(trimmedLine)) {
                        conflictFiles.add(trimmedLine);
                    }
                }
            }
        }
        
        return conflictFiles;
    }
    
    /**
     * 判断是否是Git的警告或提示信息（不是文件名）
     */
    private boolean isGitWarningOrInfo(String line) {
        if (line == null) return true;
        String lowerLine = line.toLowerCase();
        
        // 常见的Git警告和提示信息前缀
        return lowerLine.startsWith("warning:") ||
               lowerLine.startsWith("error:") ||
               lowerLine.startsWith("hint:") ||
               lowerLine.startsWith("fatal:") ||
               lowerLine.startsWith("note:") ||
               lowerLine.contains("crlf will be replaced by lf") ||
               lowerLine.contains("lf will be replaced by crlf") ||
               lowerLine.contains("the next time git touches it");
    }
    
    /**
     * 提交到本地仓库
     */
    public Map<String, Object> commitToLocal(String projectPath, String commitMessage) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        if (commitMessage == null || commitMessage.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "提交信息不能为空");
            return result;
        }
        
        // 执行 git commit
        Map<String, Object> commitResult = executeGitCommand(projectDir, 
            "git", "commit", "-m", commitMessage);
        
        String output = (String) commitResult.get("output");
        int exitCode = (Integer) commitResult.get("exitCode");
        
        // 检查是否没有变更
        if (output.contains("nothing to commit") || output.contains("no changes")) {
            result.put("success", true);
            result.put("noChanges", true);
            result.put("message", "没有需要提交的更改");
            return result;
        }
        
        if (exitCode == 0) {
            // 提取commit hash
            String commitHash = extractCommitHash(output);
            
            result.put("success", true);
            result.put("message", "提交成功");
            result.put("commitHash", commitHash);
            result.put("output", output);
        } else {
            result.put("success", false);
            result.put("message", "提交失败");
            result.put("output", output);
        }
        
        return result;
    }
    
    /**
     * 从commit输出中提取hash
     */
    private String extractCommitHash(String output) {
        // 匹配类似 "[main 1a2b3c4]" 的格式
        Pattern pattern = Pattern.compile("\\[\\w+\\s+([a-f0-9]+)\\]");
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    /**
     * 推送到远程仓库（支持自动重试）
     */
    public Map<String, Object> pushToRemote(String projectPath, String localBranch, 
                                           String remoteBranch, int maxRetries) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        int retryCount = 0;
        boolean pushSuccess = false;
        String lastOutput = "";
        List<String> retryLogs = new ArrayList<>();
        
        while (!pushSuccess && retryCount <= maxRetries) {
            if (retryCount > 0) {
                retryLogs.add("第 " + retryCount + " 次重试推送...");
                try {
                    Thread.sleep(2000); // 重试间隔2秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            
            // 构建push命令
            List<String> pushCommand = new ArrayList<>();
            pushCommand.add("git");
            pushCommand.add("push");
            
            if (remoteBranch != null && !remoteBranch.trim().isEmpty()) {
                String[] parts = remoteBranch.split("/", 2);
                if (parts.length == 2) {
                    String remote = parts[0];
                    String branch = parts[1];
                    pushCommand.add(remote);
                    
                    if (localBranch != null && !localBranch.trim().isEmpty()) {
                        pushCommand.add(localBranch + ":" + branch);
                    } else {
                        pushCommand.add(branch);
                    }
                }
            }
            
            Map<String, Object> pushResult = executeGitCommand(projectDir, 
                pushCommand.toArray(new String[0]));
            
            lastOutput = (String) pushResult.get("output");
            
            if ((Boolean) pushResult.get("success")) {
                pushSuccess = true;
            } else {
                retryCount++;
                
                // 检查是否需要先pull
                if (lastOutput.contains("rejected") || lastOutput.contains("non-fast-forward")) {
                    result.put("success", false);
                    result.put("needPull", true);
                    result.put("message", "远程有新提交，需要先拉取");
                    result.put("output", lastOutput);
                    return result;
                }
            }
        }
        
        if (pushSuccess) {
            boolean upToDate = lastOutput.contains("Everything up-to-date");
            result.put("success", true);
            result.put("upToDate", upToDate);
            result.put("message", upToDate ? "已是最新" : "推送成功");
            result.put("retryCount", retryCount);
            result.put("retryLogs", retryLogs);
        } else {
            result.put("success", false);
            result.put("message", "推送失败，已重试 " + retryCount + " 次");
            result.put("retryCount", retryCount);
            result.put("retryLogs", retryLogs);
            result.put("output", lastOutput);
        }
        
        return result;
    }
    
    /**
     * 继续rebase（解决冲突后）
     */
    public Map<String, Object> continueRebase(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // 检查是否还有冲突
        List<String> conflictFiles = parseConflictFiles(projectDir);
        if (!conflictFiles.isEmpty()) {
            result.put("success", false);
            result.put("hasConflict", true);
            result.put("message", "仍有未解决的冲突");
            result.put("conflictFiles", conflictFiles);
            return result;
        }
        
        // 暂存解决后的文件
        Map<String, Object> addResult = executeGitCommand(projectDir, "git", "add", ".");
        if (!(Boolean) addResult.get("success")) {
            result.put("success", false);
            result.put("message", "暂存文件失败");
            return result;
        }
        
        // 继续rebase
        Map<String, Object> rebaseResult = executeGitCommand(projectDir, "git", "rebase", "--continue");
        String output = (String) rebaseResult.get("output");
        
        // 检查是否还有冲突
        if (output.contains("CONFLICT") || output.contains("conflict")) {
            conflictFiles = parseConflictFiles(projectDir);
            result.put("success", false);
            result.put("hasConflict", true);
            result.put("message", "仍有冲突需要解决");
            result.put("conflictFiles", conflictFiles);
            return result;
        }
        
        result.put("success", true);
        result.put("message", "rebase继续成功");
        result.put("output", output);
        
        return result;
    }
    
    /**
     * 中止rebase
     */
    public Map<String, Object> abortRebase(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        Map<String, Object> abortResult = executeGitCommand(projectDir, "git", "rebase", "--abort");
        
        result.put("success", abortResult.get("success"));
        result.put("message", (Boolean) abortResult.get("success") ? "已取消rebase" : "取消失败");
        result.put("output", abortResult.get("output"));
        
        return result;
    }
    
    /**
     * 获取分支列表
     */
    public Map<String, Object> getBranches(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // 获取当前分支
        Map<String, Object> currentResult = executeGitCommand(projectDir, "git", "branch", "--show-current");
        String currentBranch = ((String) currentResult.get("output")).trim();
        
        // 获取本地分支
        Map<String, Object> localResult = executeGitCommand(projectDir, "git", "branch");
        List<String> localBranches = new ArrayList<>();
        if ((Boolean) localResult.get("success")) {
            String output = (String) localResult.get("output");
            String[] lines = output.split("\n");
            for (String line : lines) {
                String branch = line.replace("*", "").trim();
                if (!branch.isEmpty()) {
                    localBranches.add(branch);
                }
            }
        }
        
        // 获取远程分支
        Map<String, Object> remoteResult = executeGitCommand(projectDir, "git", "branch", "-r");
        List<String> remoteBranches = new ArrayList<>();
        if ((Boolean) remoteResult.get("success")) {
            String output = (String) remoteResult.get("output");
            String[] lines = output.split("\n");
            for (String line : lines) {
                String branch = line.trim();
                if (!branch.isEmpty() && !branch.contains("HEAD")) {
                    remoteBranches.add(branch);
                }
            }
        }
        
        result.put("success", true);
        result.put("currentBranch", currentBranch);
        result.put("localBranches", localBranches);
        result.put("remoteBranches", remoteBranches);
        
        return result;
    }
    
    /**
     * 切换分支
     */
    public Map<String, Object> switchBranch(String projectPath, String branchName) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // 检查是否有未提交的更改
        Map<String, Object> statusResult = checkWorkingDirectory(projectPath);
        if ((Boolean) statusResult.get("hasChanges")) {
            result.put("success", false);
            result.put("message", "有未提交的更改，无法切换分支");
            result.put("hasUncommittedChanges", true);
            return result;
        }
        
        // 切换分支
        Map<String, Object> checkoutResult = executeGitCommand(projectDir, "git", "checkout", branchName);
        
        result.put("success", checkoutResult.get("success"));
        result.put("message", (Boolean) checkoutResult.get("success") ? 
            "已切换到分支: " + branchName : "切换分支失败");
        result.put("output", checkoutResult.get("output"));
        
        return result;
    }
    
    // ==================== 新增功能 ====================
    
    /**
     * 获取详细的Git状态（包含领先/落后信息）
     */
    public Map<String, Object> getDetailedStatus(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // 基础状态检查
        Map<String, Object> basicStatus = checkWorkingDirectory(projectPath);
        if (!(Boolean) basicStatus.get("success")) {
            return basicStatus;
        }
        result.putAll(basicStatus);
        
        // 获取领先/落后远程的提交数
        String currentBranch = (String) basicStatus.get("currentBranch");
        Map<String, Object> aheadBehind = getAheadBehind(projectDir, currentBranch);
        result.put("ahead", aheadBehind.get("ahead"));
        result.put("behind", aheadBehind.get("behind"));
        
        // 获取最后一次提交信息
        Map<String, Object> lastCommit = getLastCommit(projectDir);
        result.put("lastCommit", lastCommit);
        
        // 获取暂存区文件
        Map<String, Object> stagedResult = executeGitCommand(projectDir, "git", "diff", "--cached", "--name-status");
        List<Map<String, String>> stagedFiles = new ArrayList<>();
        if ((Boolean) stagedResult.get("success")) {
            String output = (String) stagedResult.get("output");
            if (!output.trim().isEmpty()) {
                for (String line : output.trim().split("\n")) {
                    if (line.length() >= 2) {
                        Map<String, String> file = new HashMap<>();
                        file.put("status", line.substring(0, 1));
                        file.put("path", line.substring(1).trim());
                        stagedFiles.add(file);
                    }
                }
            }
        }
        result.put("stagedFiles", stagedFiles);
        result.put("stagedCount", stagedFiles.size());
        
        return result;
    }
    
    /**
     * 获取领先/落后远程的提交数
     */
    private Map<String, Object> getAheadBehind(File projectDir, String branch) {
        Map<String, Object> result = new HashMap<>();
        result.put("ahead", 0);
        result.put("behind", 0);
        
        // git rev-list --left-right --count origin/branch...branch
        Map<String, Object> countResult = executeGitCommand(projectDir, 
            "git", "rev-list", "--left-right", "--count", "origin/" + branch + "..." + branch);
        
        if ((Boolean) countResult.get("success")) {
            String output = ((String) countResult.get("output")).trim();
            String[] parts = output.split("\\s+");
            if (parts.length >= 2) {
                try {
                    result.put("behind", Integer.parseInt(parts[0]));
                    result.put("ahead", Integer.parseInt(parts[1]));
                } catch (NumberFormatException e) {
                    // 忽略解析错误
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取最后一次提交信息
     */
    private Map<String, Object> getLastCommit(File projectDir) {
        Map<String, Object> result = new HashMap<>();
        
        // git log -1 --format="%H|%h|%an|%ae|%at|%s"
        Map<String, Object> logResult = executeGitCommand(projectDir, 
            "git", "log", "-1", "--format=%H|%h|%an|%ae|%at|%s");
        
        if ((Boolean) logResult.get("success")) {
            String output = ((String) logResult.get("output")).trim();
            String[] parts = output.split("\\|", 6);
            if (parts.length >= 6) {
                result.put("hash", parts[0]);
                result.put("shortHash", parts[1]);
                result.put("author", parts[2]);
                result.put("email", parts[3]);
                try {
                    result.put("timestamp", Long.parseLong(parts[4]) * 1000);
                } catch (NumberFormatException e) {
                    result.put("timestamp", 0);
                }
                result.put("message", parts[5]);
            }
        }
        
        return result;
    }
    
    /**
     * 获取提交历史
     * @param limit 限制条数，0或负数表示不限制
     */
    public Map<String, Object> getCommitHistory(String projectPath, int limit) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // git log --format="%H|%h|%an|%ae|%at|%s" -n limit
        Map<String, Object> logResult;
        if (limit > 0) {
            logResult = executeGitCommand(projectDir, 
                "git", "log", "--format=%H|%h|%an|%ae|%at|%s", "-n", String.valueOf(limit));
        } else {
            // 不限制条数
            logResult = executeGitCommand(projectDir, 
                "git", "log", "--format=%H|%h|%an|%ae|%at|%s");
        }
        
        List<Map<String, Object>> commits = new ArrayList<>();
        
        if ((Boolean) logResult.get("success")) {
            String output = (String) logResult.get("output");
            for (String line : output.trim().split("\n")) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split("\\|", 6);
                if (parts.length >= 6) {
                    Map<String, Object> commit = new HashMap<>();
                    commit.put("hash", parts[0]);
                    commit.put("shortHash", parts[1]);
                    commit.put("author", parts[2]);
                    commit.put("email", parts[3]);
                    try {
                        commit.put("timestamp", Long.parseLong(parts[4]) * 1000);
                    } catch (NumberFormatException e) {
                        commit.put("timestamp", 0);
                    }
                    commit.put("message", parts[5]);
                    commits.add(commit);
                }
            }
        }
        
        result.put("success", true);
        result.put("commits", commits);
        result.put("total", commits.size());
        
        return result;
    }
    
    /**
     * 获取某次提交的文件变更
     */
    public Map<String, Object> getCommitFiles(String projectPath, String commitHash) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // git show --name-status --format="" commitHash
        Map<String, Object> showResult = executeGitCommand(projectDir, 
            "git", "show", "--name-status", "--format=", commitHash);
        
        List<Map<String, String>> files = new ArrayList<>();
        
        if ((Boolean) showResult.get("success")) {
            String output = (String) showResult.get("output");
            for (String line : output.trim().split("\n")) {
                if (line.trim().isEmpty()) continue;
                
                if (line.length() >= 2) {
                    Map<String, String> file = new HashMap<>();
                    String status = line.substring(0, 1);
                    String path = line.substring(1).trim();
                    
                    file.put("status", status);
                    file.put("statusText", getStatusText(status));
                    file.put("path", path);
                    files.add(file);
                }
            }
        }
        
        result.put("success", true);
        result.put("files", files);
        result.put("total", files.size());
        
        return result;
    }
    
    /**
     * 获取某次提交中特定文件的差异
     */
    public Map<String, Object> getCommitFileDiff(String projectPath, String commitHash, String filePath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // git show commitHash -- filePath
        // 或者 git diff commitHash^..commitHash -- filePath
        Map<String, Object> diffResult;
        
        if (filePath != null && !filePath.isEmpty()) {
            // 获取特定文件的差异
            diffResult = executeGitCommand(projectDir, 
                "git", "show", "--format=", commitHash, "--", filePath);
        } else {
            // 获取整个提交的差异
            diffResult = executeGitCommand(projectDir, 
                "git", "show", "--format=", commitHash);
        }
        
        result.put("success", diffResult.get("success"));
        result.put("diff", diffResult.get("output"));
        
        return result;
    }
    
    /**
     * 获取状态文本
     */
    private String getStatusText(String status) {
        switch (status) {
            case "A": return "新增";
            case "M": return "修改";
            case "D": return "删除";
            case "R": return "重命名";
            case "C": return "复制";
            default: return "未知";
        }
    }
    
    /**
     * 创建新分支
     */
    public Map<String, Object> createBranch(String projectPath, String branchName, boolean checkout) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        if (branchName == null || branchName.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "分支名称不能为空");
            return result;
        }
        
        // 检查分支名是否合法
        if (!branchName.matches("^[a-zA-Z0-9_\\-/]+$")) {
            result.put("success", false);
            result.put("message", "分支名称只能包含字母、数字、下划线、横线和斜杠");
            return result;
        }
        
        Map<String, Object> createResult;
        if (checkout) {
            // 创建并切换
            createResult = executeGitCommand(projectDir, "git", "checkout", "-b", branchName);
        } else {
            // 只创建
            createResult = executeGitCommand(projectDir, "git", "branch", branchName);
        }
        
        result.put("success", createResult.get("success"));
        result.put("message", (Boolean) createResult.get("success") ? 
            "分支 " + branchName + " 创建成功" : "创建分支失败");
        result.put("output", createResult.get("output"));
        
        return result;
    }
    
    /**
     * 删除分支
     */
    public Map<String, Object> deleteBranch(String projectPath, String branchName, boolean force, boolean remote) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        if (remote) {
            // 删除远程分支
            String[] parts = branchName.split("/", 2);
            if (parts.length == 2) {
                Map<String, Object> deleteResult = executeGitCommand(projectDir, 
                    "git", "push", parts[0], "--delete", parts[1]);
                result.put("success", deleteResult.get("success"));
                result.put("message", (Boolean) deleteResult.get("success") ? 
                    "远程分支 " + branchName + " 删除成功" : "删除远程分支失败");
                result.put("output", deleteResult.get("output"));
            } else {
                result.put("success", false);
                result.put("message", "远程分支格式错误");
            }
        } else {
            // 删除本地分支
            String flag = force ? "-D" : "-d";
            Map<String, Object> deleteResult = executeGitCommand(projectDir, "git", "branch", flag, branchName);
            result.put("success", deleteResult.get("success"));
            result.put("message", (Boolean) deleteResult.get("success") ? 
                "本地分支 " + branchName + " 删除成功" : "删除本地分支失败");
            result.put("output", deleteResult.get("output"));
        }
        
        return result;
    }
    
    /**
     * 合并分支
     */
    public Map<String, Object> mergeBranch(String projectPath, String branchName) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        Map<String, Object> mergeResult = executeGitCommand(projectDir, "git", "merge", branchName);
        String output = (String) mergeResult.get("output");
        
        // 检查是否有冲突
        boolean hasConflict = output.contains("CONFLICT") || output.contains("conflict");
        
        if (hasConflict) {
            List<String> conflictFiles = parseConflictFiles(projectDir);
            result.put("success", false);
            result.put("hasConflict", true);
            result.put("message", "合并时发生冲突");
            result.put("conflictFiles", conflictFiles);
        } else {
            result.put("success", mergeResult.get("success"));
            result.put("hasConflict", false);
            result.put("message", (Boolean) mergeResult.get("success") ? 
                "合并成功" : "合并失败");
        }
        result.put("output", output);
        
        return result;
    }
    
    /**
     * Stash 暂存当前修改
     */
    public Map<String, Object> stash(String projectPath, String message) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        Map<String, Object> stashResult;
        if (message != null && !message.trim().isEmpty()) {
            stashResult = executeGitCommand(projectDir, "git", "stash", "push", "-m", message);
        } else {
            stashResult = executeGitCommand(projectDir, "git", "stash");
        }
        
        String output = (String) stashResult.get("output");
        boolean noChanges = output.contains("No local changes");
        
        result.put("success", stashResult.get("success"));
        result.put("noChanges", noChanges);
        result.put("message", noChanges ? "没有需要暂存的修改" : 
            ((Boolean) stashResult.get("success") ? "暂存成功" : "暂存失败"));
        result.put("output", output);
        
        return result;
    }
    
    /**
     * 获取 Stash 列表
     */
    public Map<String, Object> getStashList(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        Map<String, Object> listResult = executeGitCommand(projectDir, "git", "stash", "list");
        
        List<Map<String, Object>> stashes = new ArrayList<>();
        
        if ((Boolean) listResult.get("success")) {
            String output = (String) listResult.get("output");
            int index = 0;
            for (String line : output.trim().split("\n")) {
                if (line.trim().isEmpty()) continue;
                
                Map<String, Object> stash = new HashMap<>();
                stash.put("index", index);
                stash.put("name", "stash@{" + index + "}");
                stash.put("description", line);
                stashes.add(stash);
                index++;
            }
        }
        
        result.put("success", true);
        result.put("stashes", stashes);
        result.put("total", stashes.size());
        
        return result;
    }
    
    /**
     * 应用 Stash
     */
    public Map<String, Object> applyStash(String projectPath, int index, boolean pop) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        String stashRef = "stash@{" + index + "}";
        String command = pop ? "pop" : "apply";
        
        Map<String, Object> applyResult = executeGitCommand(projectDir, "git", "stash", command, stashRef);
        
        result.put("success", applyResult.get("success"));
        result.put("message", (Boolean) applyResult.get("success") ? 
            (pop ? "已恢复并删除暂存" : "已恢复暂存") : "恢复暂存失败");
        result.put("output", applyResult.get("output"));
        
        return result;
    }
    
    /**
     * 删除 Stash
     */
    public Map<String, Object> dropStash(String projectPath, int index) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        String stashRef = "stash@{" + index + "}";
        Map<String, Object> dropResult = executeGitCommand(projectDir, "git", "stash", "drop", stashRef);
        
        result.put("success", dropResult.get("success"));
        result.put("message", (Boolean) dropResult.get("success") ? "已删除暂存" : "删除暂存失败");
        result.put("output", dropResult.get("output"));
        
        return result;
    }
    
    /**
     * 获取文件 Diff
     */
    public Map<String, Object> getFileDiff(String projectPath, String filePath, boolean staged) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        Map<String, Object> diffResult;
        if (staged) {
            diffResult = executeGitCommand(projectDir, "git", "diff", "--cached", filePath);
        } else {
            diffResult = executeGitCommand(projectDir, "git", "diff", filePath);
        }
        
        result.put("success", diffResult.get("success"));
        result.put("diff", diffResult.get("output"));
        
        return result;
    }
    
    /**
     * 取消暂存文件
     */
    public Map<String, Object> unstageFile(String projectPath, String filePath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        Map<String, Object> resetResult = executeGitCommand(projectDir, "git", "reset", "HEAD", filePath);
        
        result.put("success", resetResult.get("success"));
        result.put("message", (Boolean) resetResult.get("success") ? "已取消暂存" : "取消暂存失败");
        result.put("output", resetResult.get("output"));
        
        return result;
    }
    
    /**
     * 放弃文件修改
     */
    public Map<String, Object> discardChanges(String projectPath, String filePath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        Map<String, Object> checkoutResult = executeGitCommand(projectDir, "git", "checkout", "--", filePath);
        
        result.put("success", checkoutResult.get("success"));
        result.put("message", (Boolean) checkoutResult.get("success") ? "已放弃修改" : "放弃修改失败");
        result.put("output", checkoutResult.get("output"));
        
        return result;
    }
    
    /**
     * 回退到某个提交
     */
    public Map<String, Object> resetToCommit(String projectPath, String commitHash, String mode) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // mode: soft, mixed, hard
        String resetMode = "--" + (mode != null ? mode : "mixed");
        
        Map<String, Object> resetResult = executeGitCommand(projectDir, "git", "reset", resetMode, commitHash);
        
        result.put("success", resetResult.get("success"));
        result.put("message", (Boolean) resetResult.get("success") ? 
            "已回退到提交 " + commitHash.substring(0, 7) : "回退失败");
        result.put("output", resetResult.get("output"));
        
        return result;
    }
    
    /**
     * 回退单个文件到某个提交的状态
     * 使用 git checkout <commit> -- <file> 命令
     */
    public Map<String, Object> revertFileToCommit(String projectPath, String commitHash, String filePath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        if (filePath == null || filePath.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "文件路径不能为空");
            return result;
        }
        
        // 使用 git checkout <commit> -- <file> 将文件恢复到指定提交的状态
        Map<String, Object> checkoutResult = executeGitCommand(projectDir, 
            "git", "checkout", commitHash, "--", filePath);
        
        result.put("success", checkoutResult.get("success"));
        result.put("message", (Boolean) checkoutResult.get("success") ? 
            "文件 " + filePath + " 已回退到提交 " + commitHash.substring(0, Math.min(7, commitHash.length())) : 
            "回退文件失败");
        result.put("output", checkoutResult.get("output"));
        
        return result;
    }
    
    /**
     * Cherry-pick 拣选提交
     * 将指定提交的更改应用到当前分支
     */
    public Map<String, Object> cherryPick(String projectPath, String commitHash) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        if (commitHash == null || commitHash.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "提交哈希不能为空");
            return result;
        }
        
        // 执行 git cherry-pick
        Map<String, Object> cherryPickResult = executeGitCommand(projectDir, 
            "git", "cherry-pick", commitHash);
        
        String output = (String) cherryPickResult.get("output");
        
        // 检查是否有冲突
        boolean hasConflict = output.contains("CONFLICT") || 
                             output.contains("conflict") ||
                             output.contains("Merge conflict");
        
        if (hasConflict) {
            List<String> conflictFiles = parseConflictFiles(projectDir);
            result.put("success", false);
            result.put("hasConflict", true);
            result.put("message", "Cherry-pick 产生冲突，请手动解决");
            result.put("conflictFiles", conflictFiles);
            result.put("output", output);
            return result;
        }
        
        result.put("success", cherryPickResult.get("success"));
        result.put("hasConflict", false);
        result.put("message", (Boolean) cherryPickResult.get("success") ? 
            "Cherry-pick 成功" : "Cherry-pick 失败");
        result.put("output", output);
        
        return result;
    }
    
    /**
     * 获取当前冲突文件列表
     */
    public Map<String, Object> getConflictFiles(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        List<String> conflictFiles = parseConflictFiles(projectDir);
        
        result.put("success", true);
        result.put("conflictFiles", conflictFiles);
        result.put("count", conflictFiles.size());
        
        return result;
    }
    
    /**
     * 获取冲突文件的内容
     */
    public Map<String, Object> getConflictFileContent(String projectPath, String filePath) {
        Map<String, Object> result = new HashMap<>();
        
        // 参数校验
        if (projectPath == null || projectPath.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "项目路径不能为空");
            return result;
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "文件路径不能为空");
            return result;
        }
        
        File projectDir = new File(projectPath);
        
        // 检查项目目录是否存在
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            result.put("success", false);
            result.put("message", "项目目录不存在: " + projectPath);
            return result;
        }
        
        File file = new File(projectDir, filePath);
        String absolutePath = file.getAbsolutePath();
        
        if (!file.exists()) {
            result.put("success", false);
            result.put("message", "文件不存在: " + absolutePath);
            return result;
        }
        
        if (!file.isFile()) {
            result.put("success", false);
            result.put("message", "路径不是文件: " + absolutePath);
            return result;
        }
        
        if (!file.canRead()) {
            result.put("success", false);
            result.put("message", "文件不可读: " + absolutePath);
            return result;
        }
        
        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            result.put("success", true);
            result.put("content", content);
            result.put("filePath", absolutePath);
            result.put("fileSize", file.length());
        } catch (java.nio.file.AccessDeniedException e) {
            result.put("success", false);
            result.put("message", "文件访问被拒绝: " + absolutePath);
        } catch (java.io.IOException e) {
            result.put("success", false);
            result.put("message", "读取文件IO错误: " + e.getMessage());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "读取文件失败: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 标记文件为已解决（git add）
     */
    public Map<String, Object> markFileResolved(String projectPath, String filePath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        Map<String, Object> addResult = executeGitCommand(projectDir, "git", "add", filePath);
        
        result.put("success", addResult.get("success"));
        result.put("message", (Boolean) addResult.get("success") ? 
            "已标记为已解决" : "标记失败");
        result.put("output", addResult.get("output"));
        
        return result;
    }
    
    /**
     * 中止当前操作（自动检测是 rebase/merge/cherry-pick）
     */
    public Map<String, Object> abortCurrentOperation(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // 检测当前是什么操作
        File rebaseDir = new File(projectDir, ".git/rebase-merge");
        File rebaseApplyDir = new File(projectDir, ".git/rebase-apply");
        File mergeHead = new File(projectDir, ".git/MERGE_HEAD");
        File cherryPickHead = new File(projectDir, ".git/CHERRY_PICK_HEAD");
        
        Map<String, Object> abortResult;
        String operation;
        
        if (rebaseDir.exists() || rebaseApplyDir.exists()) {
            operation = "rebase";
            abortResult = executeGitCommand(projectDir, "git", "rebase", "--abort");
        } else if (mergeHead.exists()) {
            operation = "merge";
            abortResult = executeGitCommand(projectDir, "git", "merge", "--abort");
        } else if (cherryPickHead.exists()) {
            operation = "cherry-pick";
            abortResult = executeGitCommand(projectDir, "git", "cherry-pick", "--abort");
        } else {
            result.put("success", false);
            result.put("message", "没有正在进行的操作需要中止");
            return result;
        }
        
        result.put("success", abortResult.get("success"));
        result.put("operation", operation);
        result.put("message", (Boolean) abortResult.get("success") ? 
            "已中止 " + operation + " 操作" : "中止失败");
        result.put("output", abortResult.get("output"));
        
        return result;
    }
    
    /**
     * 继续当前操作（自动检测是 rebase/merge/cherry-pick）
     */
    public Map<String, Object> continueCurrentOperation(String projectPath) {
        Map<String, Object> result = new HashMap<>();
        File projectDir = new File(projectPath);
        
        // 先检查是否还有冲突
        List<String> conflictFiles = parseConflictFiles(projectDir);
        if (!conflictFiles.isEmpty()) {
            result.put("success", false);
            result.put("hasConflict", true);
            result.put("message", "仍有未解决的冲突");
            result.put("conflictFiles", conflictFiles);
            return result;
        }
        
        // 检测当前是什么操作
        File rebaseDir = new File(projectDir, ".git/rebase-merge");
        File rebaseApplyDir = new File(projectDir, ".git/rebase-apply");
        File mergeHead = new File(projectDir, ".git/MERGE_HEAD");
        File cherryPickHead = new File(projectDir, ".git/CHERRY_PICK_HEAD");
        
        Map<String, Object> continueResult;
        String operation;
        
        if (rebaseDir.exists() || rebaseApplyDir.exists()) {
            operation = "rebase";
            // 先暂存所有文件
            executeGitCommand(projectDir, "git", "add", ".");
            continueResult = executeGitCommand(projectDir, "git", "rebase", "--continue");
        } else if (mergeHead.exists()) {
            operation = "merge";
            // 先暂存所有文件
            executeGitCommand(projectDir, "git", "add", ".");
            // merge 需要 commit
            continueResult = executeGitCommand(projectDir, "git", "commit", "--no-edit");
        } else if (cherryPickHead.exists()) {
            operation = "cherry-pick";
            // 先暂存所有文件
            executeGitCommand(projectDir, "git", "add", ".");
            continueResult = executeGitCommand(projectDir, "git", "cherry-pick", "--continue");
        } else {
            result.put("success", true);
            result.put("message", "没有正在进行的操作");
            return result;
        }
        
        String output = (String) continueResult.get("output");
        
        // 检查是否还有冲突
        boolean hasConflict = output.contains("CONFLICT") || output.contains("conflict");
        if (hasConflict) {
            conflictFiles = parseConflictFiles(projectDir);
            result.put("success", false);
            result.put("hasConflict", true);
            result.put("message", "仍有冲突需要解决");
            result.put("conflictFiles", conflictFiles);
            return result;
        }
        
        result.put("success", continueResult.get("success"));
        result.put("operation", operation);
        result.put("message", (Boolean) continueResult.get("success") ? 
            operation + " 操作完成" : "操作失败");
        result.put("output", output);
        
        return result;
    }
}
