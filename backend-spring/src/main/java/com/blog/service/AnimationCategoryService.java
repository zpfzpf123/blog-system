package com.blog.service;

import com.blog.entity.AnimationCategory;
import com.blog.repository.AnimationCategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimationCategoryService {

    @Autowired
    private AnimationCategoryRepository repository;

    public List<AnimationCategory> getAllCategories() {
        return repository.findAllByOrderBySortOrderAscNameAsc();
    }
}
