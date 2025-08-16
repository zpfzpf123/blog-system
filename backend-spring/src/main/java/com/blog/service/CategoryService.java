package com.blog.service;

import com.blog.entity.Category;
import com.blog.repository.CategoryRepository;
import com.blog.dto.CategoryDTO;
import com.blog.dto.CategoriesResponseDTO;
import com.blog.dto.PaginationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .sorted((c1, c2) -> c1.getId().compareTo(c2.getId()))
                .map(category -> new CategoryDTO(
                        category.getId(),
                        category.getName(),
                        category.getCreatedAt()))
                .collect(Collectors.toList());
    }
    
    // 新增：分页查询分类
    public CategoriesResponseDTO getCategoriesWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        
        List<CategoryDTO> categoryDTOs = categoryPage.getContent().stream()
                .map(category -> new CategoryDTO(
                        category.getId(),
                        category.getName(),
                        category.getCreatedAt()))
                .collect(Collectors.toList());
        
        PaginationDTO pagination = new PaginationDTO(
                page,
                categoryPage.getTotalPages(),
                categoryPage.getTotalElements()
        );
        
        return new CategoriesResponseDTO(categoryDTOs, pagination);
    }
    
    // 新增：分页搜索分类
    public CategoriesResponseDTO searchCategoriesWithPagination(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        
        // 如果搜索名为空，返回所有分类
        if (name == null || name.trim().isEmpty()) {
            return getCategoriesWithPagination(page, size);
        }
        
        // 这里需要修改Repository来支持分页搜索
        // 暂时使用内存分页作为替代方案
        List<Category> allCategories = categoryRepository.findByNameContainingIgnoreCase(name);
        
        int totalElements = allCategories.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int startIndex = (page - 1) * size;
        int endIndex = Math.min(startIndex + size, totalElements);
        
        List<CategoryDTO> categoryDTOs = allCategories.subList(startIndex, endIndex).stream()
                .map(category -> new CategoryDTO(
                        category.getId(),
                        category.getName(),
                        category.getCreatedAt()))
                .collect(Collectors.toList());
        
        PaginationDTO pagination = new PaginationDTO(page, totalPages, totalElements);
        
        return new CategoriesResponseDTO(categoryDTOs, pagination);
    }
    
    public CategoryDTO createCategory(String name) {
        // 检查分类是否已存在
        if (categoryRepository.existsByName(name)) {
            throw new RuntimeException("分类已存在");
        }
        
        Category category = new Category(name);
        Category savedCategory = categoryRepository.save(category);
        
        return new CategoryDTO(
                savedCategory.getId(),
                savedCategory.getName(),
                savedCategory.getCreatedAt());
    }
    
    // 批量创建分类
    public int batchCreateCategories(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new RuntimeException("请提供要添加的分类名称列表");
        }
        
        int createdCount = 0;
        for (String name : names) {
            try {
                if (name != null && !name.trim().isEmpty()) {
                    createCategory(name.trim());
                    createdCount++;
                }
            } catch (RuntimeException e) {
                // 记录错误但继续创建其他分类
                System.err.println("创建分类 '" + name + "' 失败: " + e.getMessage());
            }
        }
        
        return createdCount;
    }
    
    public CategoryDTO updateCategory(Long id, String name) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类未找到"));
        
        // 检查是否与其他分类重名
        Category existingCategory = categoryRepository.findByName(name).orElse(null);
        if (existingCategory != null && !existingCategory.getId().equals(id)) {
            throw new RuntimeException("分类已存在");
        }
        
        category.setName(name);
        Category updatedCategory = categoryRepository.save(category);
        
        return new CategoryDTO(
                updatedCategory.getId(),
                updatedCategory.getName(),
                updatedCategory.getCreatedAt());
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类未找到"));
        
        categoryRepository.delete(category);
    }
    
    public boolean isCategoryInUse(Long id) {
        // 在实际应用中，这里需要检查是否有文章使用了这个分类
        // 简化实现，始终返回false
        return false;
    }
    
    // 新增：检查多个分类是否被使用
    public List<Long> getCategoriesInUse(List<Long> ids) {
        // 在实际应用中，这里需要检查是否有文章使用了这些分类
        // 简化实现，返回空列表
        return new ArrayList<>();
    }
    
    // 新增：批量删除分类
    public int batchDeleteCategories(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("分类ID列表不能为空");
        }
        
        // 验证所有分类都存在
        List<Category> categories = categoryRepository.findAllById(ids);
        if (categories.size() != ids.size()) {
            throw new RuntimeException("部分分类不存在");
        }
        
        // 执行批量删除
        categoryRepository.deleteAllById(ids);
        return categories.size();
    }

    public List<CategoryDTO> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name).stream()
                .sorted((c1, c2) -> c1.getId().compareTo(c2.getId()))
                .map(category -> new CategoryDTO(
                        category.getId(),
                        category.getName(),
                        category.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("分类未找到"));
        return new CategoryDTO(
                category.getId(),
                category.getName(),
                category.getCreatedAt()
        );
    }
}