package com.blog.dto;

import java.util.List;

public class PostsResponseDTO {
    private List<PostDTO> posts;
    private PaginationDTO pagination;

    // Constructors
    public PostsResponseDTO() {}

    public PostsResponseDTO(List<PostDTO> posts, PaginationDTO pagination) {
        this.posts = posts;
        this.pagination = pagination;
    }

    // Getters and Setters
    public List<PostDTO> getPosts() {
        return posts;
    }

    public void setPosts(List<PostDTO> posts) {
        this.posts = posts;
    }

    public PaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }
}