package com.blog.dto;

import com.blog.dto.CategoryDTO;
import com.blog.dto.TagDTO;
import java.time.LocalDateTime;
import java.util.List;

public class PostDTO {
    private Long id;
    private String title;
    private String desc;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private CategoryDTO category;
    private List<TagDTO> tags;

    // Constructors
    public PostDTO() {}

    public PostDTO(Long id, String title, String desc, String content, String author, 
                   LocalDateTime createdAt, CategoryDTO category, List<TagDTO> tags) {
        this.id = id;
        this.title = title;
        this.desc = desc;
        this.content = content;
        this.author = author;
        this.createdAt = createdAt;
        this.category = category;
        this.tags = tags;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public CategoryDTO getCategory() {
        return category;
    }

    public void setCategory(CategoryDTO category) {
        this.category = category;
    }

    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }
}