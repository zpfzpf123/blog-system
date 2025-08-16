package com.blog.service;

import com.blog.dto.TagDTO;
import com.blog.dto.TagsResponseDTO;
import com.blog.dto.PaginationDTO;
import com.blog.entity.Tag;
import com.blog.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {
    
    @Autowired
    private TagRepository tagRepository;
    
    public List<TagDTO> getAllTags() {
        return tagRepository.findAll().stream()
                .sorted((t1, t2) -> t1.getId().compareTo(t2.getId()))
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }
    
    // 新增：分页查询标签
    public TagsResponseDTO getTagsWithPagination(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<Tag> tagPage = tagRepository.findAll(pageable);
        
        List<TagDTO> tagDTOs = tagPage.getContent().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
        
        PaginationDTO pagination = new PaginationDTO(
                page,
                tagPage.getTotalPages(),
                tagPage.getTotalElements()
        );
        
        return new TagsResponseDTO(tagDTOs, pagination);
    }
    
    // 新增：分页搜索标签
    public TagsResponseDTO searchTagsWithPagination(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));
        
        // 如果搜索名为空，返回所有标签
        if (name == null || name.trim().isEmpty()) {
            return getTagsWithPagination(page, size);
        }
        
        // 使用数据库分页
        Page<Tag> tagPage = tagRepository.findByNameContainingIgnoreCase(name, pageable);
        
        List<TagDTO> tagDTOs = tagPage.getContent().stream()
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
        
        PaginationDTO pagination = new PaginationDTO(page, tagPage.getTotalPages(), tagPage.getTotalElements());
        
        return new TagsResponseDTO(tagDTOs, pagination);
    }
    
    public TagDTO createTag(String name) {
        // 检查标签是否已存在
        if (tagRepository.existsByName(name)) {
            throw new RuntimeException("标签已存在");
        }
        
        Tag tag = new Tag(name);
        Tag savedTag = tagRepository.save(tag);
        
        return new TagDTO(savedTag.getId(), savedTag.getName());
    }
    
    // 批量创建标签
    public int batchCreateTags(List<String> names) {
        if (names == null || names.isEmpty()) {
            throw new RuntimeException("请提供要添加的标签名称列表");
        }
        
        int createdCount = 0;
        for (String name : names) {
            try {
                if (name != null && !name.trim().isEmpty()) {
                    createTag(name.trim());
                    createdCount++;
                }
            } catch (RuntimeException e) {
                // 记录错误但继续创建其他标签
                System.err.println("创建标签 '" + name + "' 失败: " + e.getMessage());
            }
        }
        
        return createdCount;
    }
    
    public TagDTO updateTag(Long id, String name) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签未找到"));
        
        // 检查是否与其他标签重名
        Tag existingTag = tagRepository.findByName(name).orElse(null);
        if (existingTag != null && !existingTag.getId().equals(id)) {
            throw new RuntimeException("标签已存在");
        }
        
        tag.setName(name);
        Tag updatedTag = tagRepository.save(tag);
        
        return new TagDTO(updatedTag.getId(), updatedTag.getName());
    }
    
    public void deleteTag(Long id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签未找到"));
        
        tagRepository.delete(tag);
    }
    
    // 批量删除标签
    public int batchDeleteTags(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("请提供要删除的标签ID列表");
        }
        
        int deletedCount = 0;
        for (Long id : ids) {
            try {
                deleteTag(id);
                deletedCount++;
            } catch (RuntimeException e) {
                // 记录错误但继续删除其他标签
                System.err.println("删除标签ID " + id + " 失败: " + e.getMessage());
            }
        }
        
        return deletedCount;
    }

    public List<TagDTO> searchTagsByName(String name) {
        return tagRepository.findByNameContainingIgnoreCase(name).stream()
                .sorted((t1, t2) -> t1.getId().compareTo(t2.getId()))
                .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                .collect(Collectors.toList());
    }
}