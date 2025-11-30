package com.blog.dto;

import java.util.List;

/**
 * 项目解析结果DTO
 */
public class ProjectAnalysisResult {
    private String projectName;
    private String readmeContent;
    private String localPath;
    private List<GitCommitInfo> gitCommits;
    
    public String getProjectName() {
        return projectName;
    }
    
    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
    
    public String getReadmeContent() {
        return readmeContent;
    }
    
    public void setReadmeContent(String readmeContent) {
        this.readmeContent = readmeContent;
    }
    
    public String getLocalPath() {
        return localPath;
    }
    
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
    
    public List<GitCommitInfo> getGitCommits() {
        return gitCommits;
    }
    
    public void setGitCommits(List<GitCommitInfo> gitCommits) {
        this.gitCommits = gitCommits;
    }
}
