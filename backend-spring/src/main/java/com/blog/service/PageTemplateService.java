package com.blog.service;

import com.blog.entity.PageTemplate;
import com.blog.repository.PageTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PageTemplateService {

    @Autowired
    private PageTemplateRepository repository;

    public List<PageTemplate> getAllTemplates() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Optional<PageTemplate> getTemplateById(Long id) {
        return repository.findById(id);
    }

    public List<PageTemplate> getTemplatesByCategory(String category) {
        return repository.findByCategory(category);
    }

    public List<PageTemplate> searchTemplates(String keyword) {
        return repository.findByNameContainingIgnoreCase(keyword);
    }

    public List<String> getAllCategories() {
        return repository.findAllCategories();
    }

    public List<String> getAllTechStacks() {
        return repository.findAllTechStacks();
    }

    public List<PageTemplate> getTemplatesByTechStack(String techStack) {
        return repository.findByTechStack(techStack);
    }

    public PageTemplate createTemplate(PageTemplate template) {
        return repository.save(template);
    }

    public PageTemplate updateTemplate(Long id, PageTemplate template) {
        template.setId(id);
        return repository.save(template);
    }

    public void deleteTemplate(Long id) {
        repository.deleteById(id);
    }

    public void incrementViewCount(Long id) {
        repository.findById(id).ifPresent(template -> {
            template.setViewCount(template.getViewCount() + 1);
            repository.save(template);
        });
    }

    public void incrementCopyCount(Long id) {
        repository.findById(id).ifPresent(template -> {
            template.setCopyCount(template.getCopyCount() + 1);
            repository.save(template);
        });
    }

    public List<PageTemplate> getPopularTemplates() {
        return repository.findAllByOrderByCopyCountDesc();
    }
}
