package com.blog.service.impl;

import com.blog.dto.CategoryDTO;
import com.blog.entity.WebsiteCategory;
import com.blog.repository.WebsiteCategoryRepository;
import com.blog.repository.WebsiteRepository;
import com.blog.service.WebsiteCategoryService;
import com.blog.controller.WebsiteCategoryController.CategoryCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 网站分类服务实现类
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@Service
@Transactional
public class WebsiteCategoryServiceImpl implements WebsiteCategoryService {
    
    @Autowired
    private WebsiteCategoryRepository categoryRepository;
    
    @Autowired
    private WebsiteRepository websiteRepository;
    
    @Override
    public List<CategoryDTO> getAllCategories() {
        List<WebsiteCategory> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc();
        // 预取所有分类的计数，减少 N+1 查询
        java.util.Map<Long, Long> counts = new java.util.HashMap<>();
        for (Object[] row : websiteRepository.countActiveGroupByCategory()) {
            Long cid = (Long) row[0];
            Long cnt = (Long) row[1];
            counts.put(cid, cnt);
        }
        return categories.stream()
                .map(cat -> {
                    CategoryDTO dto = new CategoryDTO(cat);
                    Long count = counts.getOrDefault(cat.getId(), 0L);
                    dto.setWebsiteCount(count);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<CategoryDTO> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .filter(WebsiteCategory::getIsActive)
                .map(CategoryDTO::new);
    }
    
    @Override
    public CategoryDTO createCategory(CategoryCreateRequest request) {
        // 检查分类名称是否已存在
        if (categoryRepository.findByNameAndIsActiveTrue(request.getName()).isPresent()) {
            throw new RuntimeException("分类名称已存在: " + request.getName());
        }
        
        WebsiteCategory category = new WebsiteCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setColor(request.getColor() != null ? request.getColor() : "#409EFF");
        category.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        category.setIsActive(true);
        
        WebsiteCategory savedCategory = categoryRepository.save(category);
        return new CategoryDTO(savedCategory);
    }
    
    @Override
    public CategoryDTO updateCategory(Long id, CategoryCreateRequest request) {
        WebsiteCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        
        // 检查名称是否与其他分类重复
        Optional<WebsiteCategory> existingCategory = categoryRepository.findByNameAndIsActiveTrue(request.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
            throw new RuntimeException("分类名称已存在: " + request.getName());
        }
        
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getColor() != null) {
            category.setColor(request.getColor());
        }
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        
        WebsiteCategory savedCategory = categoryRepository.save(category);
        return new CategoryDTO(savedCategory);
    }
    
    @Override
    public void deleteCategory(Long id) {
        WebsiteCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类不存在: " + id));
        
        // 软删除：设置为非活跃状态
        category.setIsActive(false);
        categoryRepository.save(category);
    }
    
    @Override
    public List<CategoryDTO> searchCategories(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllCategories();
        }
        
        List<WebsiteCategory> categories = categoryRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword);
        return categories.stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<CategoryDTO> getPopularCategories(int limit) {
        // 这里可以根据网站数量来排序，暂时返回所有活跃分类
        List<WebsiteCategory> categories = categoryRepository.findByIsActiveTrueOrderBySortOrderAscNameAsc();
        java.util.Map<Long, Long> counts = new java.util.HashMap<>();
        for (Object[] row : websiteRepository.countActiveGroupByCategory()) {
            Long cid = (Long) row[0];
            Long cnt = (Long) row[1];
            counts.put(cid, cnt);
        }
        return categories.stream()
                .limit(limit)
                .map(cat -> {
                    CategoryDTO dto = new CategoryDTO(cat);
                    dto.setWebsiteCount(counts.getOrDefault(cat.getId(), 0L));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    
    @Override
    public void updateCategoryOrder(List<com.blog.controller.WebsiteCategoryController.CategoryOrderRequest> orderRequests) {
        for (com.blog.controller.WebsiteCategoryController.CategoryOrderRequest request : orderRequests) {
            WebsiteCategory category = categoryRepository.findById(request.getId())
                    .orElseThrow(() -> new RuntimeException("分类不存在: " + request.getId()));
            
            category.setSortOrder(request.getSortOrder());
            categoryRepository.save(category);
        }
    }
}
