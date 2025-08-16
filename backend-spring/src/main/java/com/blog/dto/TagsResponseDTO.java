package com.blog.dto;

import java.util.List;

public class TagsResponseDTO {
    private List<TagDTO> tags;
    private PaginationDTO pagination;

    public TagsResponseDTO() {}

    public TagsResponseDTO(List<TagDTO> tags, PaginationDTO pagination) {
        this.tags = tags;
        this.pagination = pagination;
    }

    // Getters and Setters
    public List<TagDTO> getTags() {
        return tags;
    }

    public void setTags(List<TagDTO> tags) {
        this.tags = tags;
    }

    public PaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }
}
