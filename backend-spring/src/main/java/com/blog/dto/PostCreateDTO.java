package com.blog.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;

public class PostCreateDTO {
    @NotBlank(message = "文章标题不能为空")
    private String title;

    private String desc;

    @NotBlank(message = "文章内容不能为空")
    private String content;

    private String author;
    private Long categoryId;
    private List<Long> tagIds;

    public PostCreateDTO() {
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }
}