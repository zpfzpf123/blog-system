package com.blog.controller;

import com.blog.entity.PageTemplate;
import com.blog.service.PageTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/page-templates")
@CrossOrigin(originPatterns = {"http://localhost:*", "https://localhost:*", "http://127.0.0.1:*", "https://127.0.0.1:*"})
public class PageTemplateController {

    @Autowired
    private PageTemplateService service;

    @GetMapping
    public ResponseEntity<?> getAllTemplates() {
        try {
            List<PageTemplate> list = service.getAllTemplates();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTemplateById(@PathVariable Long id) {
        return service.getTemplateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public List<PageTemplate> getTemplatesByCategory(@PathVariable String category) {
        return service.getTemplatesByCategory(category);
    }

    @GetMapping("/search")
    public List<PageTemplate> searchTemplates(@RequestParam String keyword) {
        return service.searchTemplates(keyword);
    }

    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return service.getAllCategories();
    }

    @GetMapping("/tech-stacks")
    public List<String> getAllTechStacks() {
        return service.getAllTechStacks();
    }

    @GetMapping("/tech-stack/{techStack}")
    public List<PageTemplate> getTemplatesByTechStack(@PathVariable String techStack) {
        return service.getTemplatesByTechStack(techStack);
    }

    @GetMapping("/popular")
    public List<PageTemplate> getPopularTemplates() {
        return service.getPopularTemplates();
    }

    @PostMapping
    public PageTemplate createTemplate(@RequestBody PageTemplate template) {
        return service.createTemplate(template);
    }

    @PutMapping("/{id}")
    public PageTemplate updateTemplate(@PathVariable Long id, @RequestBody PageTemplate template) {
        return service.updateTemplate(id, template);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        service.deleteTemplate(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<?> incrementViewCount(@PathVariable Long id) {
        service.incrementViewCount(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/copy")
    public ResponseEntity<?> incrementCopyCount(@PathVariable Long id) {
        service.incrementCopyCount(id);
        return ResponseEntity.ok().build();
    }
}
