/*
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-11-27 14:04:22
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-11-27 14:57:08
 * @FilePath: \ai博客\blog\backend-spring\src\main\java\com\blog\controller\CssAnimationController.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.blog.controller;

import com.blog.entity.CssAnimation;
import com.blog.service.CssAnimationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animations")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"})
public class CssAnimationController {

    @Autowired
    private CssAnimationService service;

    @GetMapping
    public ResponseEntity<?> getAllAnimations() {
        try {
            List<CssAnimation> list = service.getAllAnimations();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            error.put("type", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/category/{category}")
    public List<CssAnimation> getAnimationsByCategory(@PathVariable String category) {
        return service.getAnimationsByCategory(category);
    }

    @PostMapping
    public CssAnimation createAnimation(@RequestBody CssAnimation animation) {
        return service.createAnimation(animation);
    }

    @PutMapping("/{id}")
    public CssAnimation updateAnimation(@PathVariable Long id, @RequestBody CssAnimation animation) {
        return service.updateAnimation(id, animation);
    }

    @DeleteMapping("/{id}")
    public void deleteAnimation(@PathVariable Long id) {
        service.deleteAnimation(id);
    }
}
