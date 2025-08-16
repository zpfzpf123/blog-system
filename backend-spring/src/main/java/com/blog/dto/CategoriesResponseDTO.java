package com.blog.dto;

import java.util.List;

public class CategoriesResponseDTO {
    private List<CategoryDTO> categories;
    private PaginationDTO pagination;

    public CategoriesResponseDTO() {}

    public CategoriesResponseDTO(List<CategoryDTO> categories, PaginationDTO pagination) {
        this.categories = categories;
        this.pagination = pagination;
    }

    // Getters and Setters
    public List<CategoryDTO> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryDTO> categories) {
        this.categories = categories;
    }

    public PaginationDTO getPagination() {
        return pagination;
    }

    public void setPagination(PaginationDTO pagination) {
        this.pagination = pagination;
    }
}
