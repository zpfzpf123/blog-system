package com.blog.dto;

/**
 * 目录项DTO
 */
public class DirectoryItem {
    private String name;
    private String path;
    private String type; // "folder" or "file"
    private Long size;
    
    public DirectoryItem() {
    }
    
    public DirectoryItem(String name, String path, String type, Long size) {
        this.name = name;
        this.path = path;
        this.type = type;
        this.size = size;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Long getSize() {
        return size;
    }
    
    public void setSize(Long size) {
        this.size = size;
    }
}
