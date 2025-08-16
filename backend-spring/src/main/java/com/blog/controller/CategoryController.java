package com.blog.controller;

import com.blog.dto.CategoryDTO;
import com.blog.dto.CategoriesResponseDTO;
import com.blog.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(originPatterns = "http://localhost:*")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    // 新增：分页查询分类 - 放在通用路径之前
    @GetMapping("/page")
    public ResponseEntity<CategoriesResponseDTO> getCategoriesWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        CategoriesResponseDTO response = categoryService.getCategoriesWithPagination(page, size);
        return ResponseEntity.ok(response);
    }
    
    // 新增：分页搜索分类 - 放在通用路径之前
    @GetMapping("/search/page")
    public ResponseEntity<CategoriesResponseDTO> searchCategoriesWithPagination(
            @RequestParam("name") String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        CategoriesResponseDTO response = categoryService.searchCategoriesWithPagination(name, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<CategoryDTO>> searchCategories(@RequestParam("name") String name) {
        List<CategoryDTO> categories = categoryService.searchCategoriesByName(name);
        return ResponseEntity.ok(categories);
    }
    
    // 通过ID获取分类，避免与/search冲突
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }
    
    // 获取所有分类 - 放在最后，作为默认路径
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
    
    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody CategoryRequest request) {
        try {
            CategoryDTO category = categoryService.createCategory(request.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(category);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // 批量添加分类
    @PostMapping("/batch")
    public ResponseEntity<?> batchCreateCategories(@RequestBody BatchCreateRequest request) {
        try {
            if (request.getNames() == null || request.getNames().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("请提供要添加的分类名称列表"));
            }
            
            int createdCount = categoryService.batchCreateCategories(request.getNames());
            return ResponseEntity.ok(new BatchCreateResponse(createdCount, "成功添加 " + createdCount + " 个分类"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        try {
            CategoryDTO category = categoryService.updateCategory(id, request.getName());
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        try {
            // 检查是否有文章使用了这个分类
            if (categoryService.isCategoryInUse(id)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("该分类正在被文章使用，无法删除"));
            }
            
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // 新增：批量删除分类
    @DeleteMapping("/batch")
    public ResponseEntity<?> batchDeleteCategories(@RequestBody BatchDeleteRequest request) {
        try {
            if (request.getIds() == null || request.getIds().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("请提供要删除的分类ID列表"));
            }
            
            // 检查是否有分类正在被文章使用
            List<Long> inUseCategories = categoryService.getCategoriesInUse(request.getIds());
            if (!inUseCategories.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("以下分类正在被文章使用，无法删除: " + inUseCategories));
            }
            
            int deletedCount = categoryService.batchDeleteCategories(request.getIds());
            return ResponseEntity.ok(new BatchDeleteResponse(deletedCount, "成功删除 " + deletedCount + " 个分类"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    // 内部类用于请求体
    public static class CategoryRequest {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    // 新增：批量删除请求类
    public static class BatchDeleteRequest {
        private List<Long> ids;
        
        public List<Long> getIds() {
            return ids;
        }
        
        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
    
    // 新增：批量删除响应类
    public static class BatchDeleteResponse {
        private int deletedCount;
        private String message;
        
        public BatchDeleteResponse(int deletedCount, String message) {
            this.deletedCount = deletedCount;
            this.message = message;
        }
        
        public int getDeletedCount() {
            return deletedCount;
        }
        
        public void setDeletedCount(int deletedCount) {
            this.deletedCount = deletedCount;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    // 批量添加请求类
    public static class BatchCreateRequest {
        private List<String> names;
        
        public List<String> getNames() {
            return names;
        }
        
        public void setNames(List<String> names) {
            this.names = names;
        }
    }
    
    // 批量添加响应类
    public static class BatchCreateResponse {
        private int createdCount;
        private String message;
        
        public BatchCreateResponse(int createdCount, String message) {
            this.createdCount = createdCount;
            this.message = message;
        }
        
        public int getCreatedCount() {
            return createdCount;
        }
        
        public void setCreatedCount(int createdCount) {
            this.createdCount = createdCount;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    // 内部类用于错误响应
    public static class ErrorResponse {
        private String message;
        
        public ErrorResponse(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}