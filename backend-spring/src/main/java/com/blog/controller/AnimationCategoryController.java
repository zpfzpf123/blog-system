package com.blog.controller;

import com.blog.entity.AnimationCategory;
import com.blog.service.AnimationCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animation-categories")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"})
public class AnimationCategoryController {

    @Autowired
    private AnimationCategoryService service;

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<AnimationCategory> list = service.getAllCategories();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
