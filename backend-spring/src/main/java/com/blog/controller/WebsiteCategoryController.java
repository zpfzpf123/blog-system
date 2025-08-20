package com.blog.controller;

import com.blog.dto.CategoryDTO;
import com.blog.entity.WebsiteCategory;
import com.blog.service.WebsiteCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * 网站分类管理控制器
 * 
 * @author 开发团队
 * @since 2025-02-27
 */
@RestController
@RequestMapping("/api/websites/categories")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"})
public class WebsiteCategoryController {
    
    @Autowired
    private WebsiteCategoryService categoryService;
    
    /**
     * 获取所有网站分类
     * GET /api/websites/categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 根据ID获取分类详情
     * GET /api/websites/categories/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        try {
            Optional<CategoryDTO> category = categoryService.getCategoryById(id);
            return category.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 创建新分类
     * POST /api/websites/categories
     */
    @PostMapping
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        try {
            CategoryDTO category = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 更新分类
     * PUT /api/websites/categories/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, 
                                                    @Valid @RequestBody CategoryCreateRequest request) {
        try {
            CategoryDTO category = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 删除分类
     * DELETE /api/websites/categories/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 搜索分类
     * GET /api/websites/categories/search?keyword={keyword}
     */
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategories(@RequestParam String keyword) {
        try {
            List<CategoryDTO> categories = categoryService.searchCategories(keyword);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 获取热门分类
     * GET /api/websites/categories/popular?limit={limit}
     */
    @GetMapping("/popular")
    public ResponseEntity<List<CategoryDTO>> getPopularCategories(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<CategoryDTO> categories = categoryService.getPopularCategories(limit);
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 批量更新分类顺序
     * PUT /api/websites/categories/order
     */
    @PutMapping("/order")
    public ResponseEntity<Void> updateCategoryOrder(@Valid @RequestBody List<CategoryOrderRequest> orderRequests) {
        try {
            categoryService.updateCategoryOrder(orderRequests);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * 分类创建请求DTO
     */
    public static class CategoryCreateRequest {
        private String name;
        private String description;
        private String color;

        private Integer sortOrder;
        
        // 构造函数
        public CategoryCreateRequest() {}
        
        // Getter和Setter方法
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getColor() {
            return color;
        }
        
        public void setColor(String color) {
            this.color = color;
        }
        

        
        public Integer getSortOrder() {
            return sortOrder;
        }
        
        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
    
    /**
     * 分类顺序更新请求DTO
     */
    public static class CategoryOrderRequest {
        private Long id;
        private Integer sortOrder;
        
        // 构造函数
        public CategoryOrderRequest() {}
        
        public CategoryOrderRequest(Long id, Integer sortOrder) {
            this.id = id;
            this.sortOrder = sortOrder;
        }
        
        // Getter和Setter方法
        public Long getId() {
            return id;
        }
        
        public void setId(Long id) {
            this.id = id;
        }
        
        public Integer getSortOrder() {
            return sortOrder;
        }
        
        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }
}
