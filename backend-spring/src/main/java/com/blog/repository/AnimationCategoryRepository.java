package com.blog.repository;

import com.blog.entity.AnimationCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimationCategoryRepository extends JpaRepository<AnimationCategory, Long> {

    List<AnimationCategory> findAllByOrderBySortOrderAscNameAsc();
}
