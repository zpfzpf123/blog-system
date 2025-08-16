/*
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-05 09:49:42
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-08-05 14:45:48
 * @FilePath: \blog\backend-spring\src\main\java\com\blog\controller\PostController.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.blog.controller;

import com.blog.dto.*;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(originPatterns = "http://localhost:*")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    /**
     * 统一异常处理方法
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("发生未知错误: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    @GetMapping
    /**
     * 获取文章列表
     * @param page 页码
     * @param limit 每页数量
     * @param categoryIds 分类ID列表
     * @param tagIds 标签ID列表
     * @param search 搜索关键字
     * @return ResponseEntity<PostsResponseDTO> 文章列表响应
     */
    public ResponseEntity<PostsResponseDTO> getPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(required = false) List<Long> categoryIds,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam(required = false) String search) {
        
        PostsResponseDTO response = postService.getPosts(page, limit, categoryIds, tagIds, search);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/category/{categoryId}")
    /**
     * 根据分类获取文章列表
     * @param categoryId 分类ID
     * @param page 页码
     * @param limit 每页数量
     * @return ResponseEntity<PostsResponseDTO> 文章列表响应
     */
    public ResponseEntity<PostsResponseDTO> getPostsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer limit) {
        
        PostsResponseDTO response = postService.getPostsByCategory(categoryId, page, limit);
        return ResponseEntity.ok(response);
    }
    
    /**
     * 根据ID获取文章详情
     * @param id 文章ID
     * @return ResponseEntity<PostDTO> 文章详情响应
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id) {
        try {
            PostDTO post = postService.getPostById(id);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    /**
     * 创建新文章
     * @param postCreateDTO 包含创建文章所需数据的DTO
     * @return ResponseEntity<PostDTO> 创建的文章的响应
     */
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody PostCreateDTO postCreateDTO) {
        try {
            PostDTO createdPost = postService.createPost(postCreateDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
    
    /**
     * 更新现有文章
     * @param id 要更新的文章的ID
     * @param postCreateDTO 包含更新文章所需数据的DTO
     * @return ResponseEntity<PostDTO> 更新后的文章的响应
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updatePost(@PathVariable Long id, @RequestBody PostCreateDTO postCreateDTO) {
        try {
            PostDTO updatedPost = postService.updatePost(id, postCreateDTO);
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            ErrorResponse errorResponse = new ErrorResponse(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
    
    /**
     * 删除文章
     * @param id 要删除的文章的ID
     * @return ResponseEntity<Void> 无内容的响应
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


}