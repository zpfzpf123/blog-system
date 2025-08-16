package com.blog.service;

import com.blog.dto.CategoryDTO;
import com.blog.controller.WebsiteCategoryController.CategoryCreateRequest;

import java.util.List;
import java.util.Optional;

/**
 * 网站分类服务接口
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
public interface WebsiteCategoryService {
    
    /**
     * 获取所有分类
     */
    List<CategoryDTO> getAllCategories();
    
    /**
     * 根据ID获取分类
     */
    Optional<CategoryDTO> getCategoryById(Long id);
    
    /**
     * 创建分类
     */
    CategoryDTO createCategory(CategoryCreateRequest request);
    
    /**
     * 更新分类
     */
    CategoryDTO updateCategory(Long id, CategoryCreateRequest request);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
    
    /**
     * 搜索分类
     */
    List<CategoryDTO> searchCategories(String keyword);
    
    /**
     * 获取热门分类
     */
    List<CategoryDTO> getPopularCategories(int limit);
    
    /**
     * 批量更新分类顺序
     */
    void updateCategoryOrder(List<com.blog.controller.WebsiteCategoryController.CategoryOrderRequest> orderRequests);
}
