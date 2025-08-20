/*
 * @Author: Your Name your.email@example.com
 * @Date: 2025-08-06 09:42:14
 * @LastEditors: Your Name your.email@example.com
 * @LastEditTime: 2025-08-06 09:42:14
 * @FilePath: \blog\backend-spring\src\main\java\com\blog\controller\RecommendationController.java
 * @Description: 推荐功能控制器
 */
package com.blog.controller;

import com.blog.dto.ErrorResponse;
import com.blog.dto.PostDTO;
import com.blog.dto.SmartRecommendationRequest;
import com.blog.service.RecommendationService;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"})
public class RecommendationController {
    
    @Autowired
    private RecommendationService recommendationService;
    
    @Autowired
    private PostService postService;
    
    /**
     * 统一异常处理方法
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("发生未知错误: " + ex.getMessage());
        return ResponseEntity.status(500).body(errorResponse);
    }
    
    /**
     * 获取外部推荐内容
     * @param query 搜索关键词
     * @return ResponseEntity<List<Map<String, Object>>> 推荐内容列表
     */
    @GetMapping("/external")
    public ResponseEntity<List<Map<String, Object>>> getExternalRecommendations(
            @RequestParam String query) {
        
        List<Map<String, Object>> recommendations = recommendationService.searchWebContent(query);
        // 确保即使搜索结果为空也返回一个空列表而不是null
        if (recommendations == null) {
            recommendations = new ArrayList<>();
        }
        return ResponseEntity.ok(recommendations);
    }
    
    /**
     * 智能获取外部推荐内容
     * @param request 智能推荐请求参数
     * @return ResponseEntity<List<Map<String, Object>>> 推荐内容列表
     */
    @PostMapping("/external/smart")
    public ResponseEntity<List<Map<String, Object>>> getSmartExternalRecommendations(
            @RequestBody SmartRecommendationRequest request) {
        
        String title = request.getTitle();
        String content = request.getContent();
        String tags = request.getTags();
        
        // 解析标签
        List<String> tagList = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            String[] tagArray = tags.split(",");
            for (String tag : tagArray) {
                tagList.add(tag.trim());
            }
        }
        
        // 生成多个搜索查询
        List<String> queries = recommendationService.generateSearchQueries(title, content, tagList);
        
        // 收集所有查询结果
        List<Map<String, Object>> allResults = new ArrayList<>();
        for (String query : queries) {
            List<Map<String, Object>> results = recommendationService.searchWebContent(query);
            // 防止null结果
            if (results != null) {
                allResults.addAll(results);
            }
        }
        
        // 去重并按评分排序
        List<Map<String, Object>> uniqueResults = allResults.stream()
                .collect(Collectors.toMap(
                        result -> (String) result.get("url"), // 用URL作为键
                        result -> result,                     // 保留结果
                        (existing, replacement) -> {          // 合并重复项，保留评分高的
                            Double existingScore = (Double) existing.getOrDefault("score", 0.0);
                            Double replacementScore = (Double) replacement.getOrDefault("score", 0.0);
                            return existingScore >= replacementScore ? existing : replacement;
                        }))
                .values()
                .stream()
                .sorted((a, b) -> {
                    Double scoreA = (Double) a.getOrDefault("score", 0.0);
                    Double scoreB = (Double) b.getOrDefault("score", 0.0);
                    return scoreB.compareTo(scoreA); // 降序排列
                })
                .limit(5) // 限制返回结果数量
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(uniqueResults);
    }
    
    /**
     * 获取基于标签的内部推荐
     * @param tagIds 标签ID列表
     * @param limit 返回推荐数量
     * @return ResponseEntity<List<PostDTO>> 推荐内容列表
     */
    @GetMapping("/internal")
    public ResponseEntity<List<PostDTO>> getInternalRecommendations(
            @RequestParam List<Long> tagIds,
            @RequestParam(defaultValue = "3") Integer limit) {
        
        List<PostDTO> posts = postService.getPostsByTagIds(tagIds, limit);
        return ResponseEntity.ok(posts);
    }
    
    /**
     * 获取与指定文章相关的推荐内容
     * @param postId 文章ID
     * @param limit 返回推荐数量
     * @return ResponseEntity<List<PostDTO>> 推荐内容列表
     */
    @GetMapping("/related")
    public ResponseEntity<List<PostDTO>> getRelatedPosts(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "3") Integer limit) {
        
        List<PostDTO> posts = postService.getRelatedPosts(postId, limit);
        return ResponseEntity.ok(posts);
    }
    
    /**
     * 获取外部网页内容预览
     * @param url 网页URL
     * @return ResponseEntity<String> 网页内容预览
     */
    @GetMapping("/external/preview")
    public ResponseEntity<String> getExternalContentPreview(@RequestParam String url) {
        String preview = recommendationService.fetchExternalContentPreview(url);
        return ResponseEntity.ok(preview);
    }
}