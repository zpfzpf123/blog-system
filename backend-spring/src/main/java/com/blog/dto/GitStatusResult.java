package com.blog.dto;

import java.util.List;

/**
 * Git状态检查结果
 */
public class GitStatusResult {
    private boolean success;
    private String message;
    private boolean hasChanges;
    private List<String> modifiedFiles;
    private List<String> untrackedFiles;
    private int modifiedCount;
    private int untrackedCount;
    private String currentBranch;

    public GitStatusResult() {
    }

    public GitStatusResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isHasChanges() {
        return hasChanges;
    }

    public void setHasChanges(boolean hasChanges) {
        this.hasChanges = hasChanges;
    }

    public List<String> getModifiedFiles() {
        return modifiedFiles;
    }

    public void setModifiedFiles(List<String> modifiedFiles) {
        this.modifiedFiles = modifiedFiles;
    }

    public List<String> getUntrackedFiles() {
        return untrackedFiles;
    }

    public void setUntrackedFiles(List<String> untrackedFiles) {
        this.untrackedFiles = untrackedFiles;
    }

    public int getModifiedCount() {
        return modifiedCount;
    }

    public void setModifiedCount(int modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    public int getUntrackedCount() {
        return untrackedCount;
    }

    public void setUntrackedCount(int untrackedCount) {
        this.untrackedCount = untrackedCount;
    }

    public String getCurrentBranch() {
        return currentBranch;
    }

    public void setCurrentBranch(String currentBranch) {
        this.currentBranch = currentBranch;
    }
}
