package com.blog.service;

import com.blog.entity.CssAnimation;
import com.blog.repository.CssAnimationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CssAnimationService {

    @Autowired
    private CssAnimationRepository repository;

    public List<CssAnimation> getAllAnimations() {
        return repository.findAll();
    }

    public List<CssAnimation> getAnimationsByCategory(String category) {
        return repository.findByCategory(category);
    }

    public CssAnimation createAnimation(CssAnimation animation) {
        return repository.save(animation);
    }

    public CssAnimation updateAnimation(Long id, CssAnimation animation) {
        CssAnimation existing = repository.findById(id).orElse(null);
        if (existing != null) {
            existing.setTitle(animation.getTitle());
            existing.setCategory(animation.getCategory());
            existing.setCssCode(animation.getCssCode());
            existing.setDescription(animation.getDescription());
            return repository.save(existing);
        }
        return null;
    }

    public void deleteAnimation(Long id) {
        repository.deleteById(id);
    }
}
