package com.blog.controller;

import com.blog.dto.TagDTO;
import com.blog.dto.TagsResponseDTO;
import com.blog.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tags")
@CrossOrigin(originPatterns = "http://localhost:*")
public class TagController {
    
    @Autowired
    private TagService tagService;
    
    // 新增：分页查询标签 - 放在通用路径之前
    @GetMapping("/page")
    public ResponseEntity<TagsResponseDTO> getTagsWithPagination(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        TagsResponseDTO response = tagService.getTagsWithPagination(page, size);
        return ResponseEntity.ok(response);
    }
    
    // 新增：分页搜索标签 - 放在通用路径之前
    @GetMapping("/search/page")
    public ResponseEntity<TagsResponseDTO> searchTagsWithPagination(
            @RequestParam("name") String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        TagsResponseDTO response = tagService.searchTagsWithPagination(name, page, size);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<TagDTO>> searchTags(@RequestParam("name") String name) {
        List<TagDTO> tags = tagService.searchTagsByName(name);
        return ResponseEntity.ok(tags);
    }
    
    @PostMapping
    public ResponseEntity<?> createTag(@RequestBody TagRequest request) {
        try {
            TagDTO tag = tagService.createTag(request.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(tag);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // 批量添加标签
    @PostMapping("/batch")
    public ResponseEntity<?> batchCreateTags(@RequestBody BatchCreateRequest request) {
        try {
            if (request.getNames() == null || request.getNames().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("请提供要添加的标签名称列表"));
            }
            
            int createdCount = tagService.batchCreateTags(request.getNames());
            return ResponseEntity.ok(new BatchCreateResponse(createdCount, "成功添加 " + createdCount + " 个标签"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTag(@PathVariable Long id, @RequestBody TagRequest request) {
        try {
            TagDTO tag = tagService.updateTag(id, request.getName());
            return ResponseEntity.ok(tag);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTag(@PathVariable Long id) {
        try {
            tagService.deleteTag(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // 批量删除标签
    @DeleteMapping("/batch")
    public ResponseEntity<?> batchDeleteTags(@RequestBody BatchDeleteRequest request) {
        try {
            if (request.getIds() == null || request.getIds().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ErrorResponse("请提供要删除的标签ID列表"));
            }
            
            int deletedCount = tagService.batchDeleteTags(request.getIds());
            return ResponseEntity.ok(new BatchDeleteResponse(deletedCount, "成功删除 " + deletedCount + " 个标签"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }
    
    // 内部类用于请求体
    public static class TagRequest {
        private String name;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
    }
    
    // 批量删除请求类
    public static class BatchDeleteRequest {
        private List<Long> ids;
        
        public List<Long> getIds() {
            return ids;
        }
        
        public void setIds(List<Long> ids) {
            this.ids = ids;
        }
    }
    
    // 批量删除响应类
    public static class BatchDeleteResponse {
        private int deletedCount;
        private String message;
        
        public BatchDeleteResponse(String message) {
            this.message = message;
        }
        
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