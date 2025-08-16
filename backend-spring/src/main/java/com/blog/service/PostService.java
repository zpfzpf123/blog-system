package com.blog.service;

import com.blog.entity.Post;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.repository.PostRepository;
import com.blog.repository.CategoryRepository;
import com.blog.repository.TagRepository;
import com.blog.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class PostService {
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    public PostsResponseDTO getPosts(Integer page, Integer limit, List<Long> categoryIds, 
                                     List<Long> tagIds, String search) {
        page = (page == null || page < 1) ? 1 : page;
        limit = (limit == null || limit < 1) ? 10 : limit;
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        // 修复参数传递，确保正确处理null值
        Page<Post> postPage = postRepository.findPostsByFilters(
            categoryIds != null && !categoryIds.isEmpty() ? categoryIds : null,
            tagIds != null && !tagIds.isEmpty() ? tagIds : null,
            search,
            pageable
        );
        
        List<PostDTO> postDTOs = postPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        PaginationDTO pagination = new PaginationDTO(
                page, 
                postPage.getTotalPages(), 
                postPage.getTotalElements());
        
        return new PostsResponseDTO(postDTOs, pagination);
    }


    
    public PostsResponseDTO getPostsByCategory(Long categoryId, Integer page, Integer limit) {
        page = (page == null || page < 1) ? 1 : page;
        limit = (limit == null || limit < 1) ? 10 : limit;
        
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("分类未找到"));
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Post> postPage = postRepository.findByCategory(category, pageable);
        
        List<PostDTO> postDTOs = postPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        PaginationDTO pagination = new PaginationDTO(
                page, 
                postPage.getTotalPages(), 
                postPage.getTotalElements());
        
        return new PostsResponseDTO(postDTOs, pagination);
    }
    
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id).orElse(null);
        if (post == null) {
            return null;
        }
        return convertToDTO(post);
    }
    
    @Transactional
    public PostDTO createPost(PostCreateDTO postCreateDTO) {
        Post post = new Post();
        post.setTitle(postCreateDTO.getTitle());
        post.setDesc(postCreateDTO.getDesc());
        post.setContent(postCreateDTO.getContent());
        // 设置默认作者为"周鹏飞"
        post.setAuthor(postCreateDTO.getAuthor() != null ? postCreateDTO.getAuthor() : "周鹏飞");
        
        // 设置分类
        if (postCreateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(postCreateDTO.getCategoryId())
                    .orElse(null);
            post.setCategory(category);
        }
        
        // 设置标签
        if (postCreateDTO.getTagIds() != null && !postCreateDTO.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(postCreateDTO.getTagIds());
            post.setTags(new HashSet<>(tags));
        }
        
        Post savedPost = postRepository.save(post);
        return convertToDTO(savedPost);
    }


    
    @Transactional
    public PostDTO updatePost(Long id, PostCreateDTO postUpdateDTO) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章未找到"));

        if (postUpdateDTO.getTitle() != null) {
            post.setTitle(postUpdateDTO.getTitle());
        }

        if (postUpdateDTO.getDesc() != null) {
            post.setDesc(postUpdateDTO.getDesc());
        }

        if (postUpdateDTO.getContent() != null) {
            post.setContent(postUpdateDTO.getContent());
        }

        // 更新作者时，如果未提供作者信息，则保持原作者不变
        if (postUpdateDTO.getAuthor() != null) {
            post.setAuthor(postUpdateDTO.getAuthor());
        }

        // 更新分类
        if (postUpdateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(postUpdateDTO.getCategoryId())
                    .orElse(null);
            post.setCategory(category);
        }

        // 更新标签
        if (postUpdateDTO.getTagIds() != null) {
            if (postUpdateDTO.getTagIds().isEmpty()) {
                post.setTags(new HashSet<>());
            } else {
                List<Tag> tags = tagRepository.findAllById(postUpdateDTO.getTagIds());
                post.setTags(new HashSet<>(tags));
            }
        }

        Post updatedPost = postRepository.save(post);
        return convertToDTO(updatedPost);
    }
    
    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章未找到"));
        
        postRepository.deleteById(id);
    }
    
    /**
     * 根据标签ID列表获取相关文章
     * @param tagIds 标签ID列表
     * @param limit 返回文章数量限制
     * @return 相关文章列表
     */
    public List<PostDTO> getPostsByTagIds(List<Long> tagIds, int limit) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        Page<Post> postPage = postRepository.findPostsByFilters(null, tagIds, null, pageable);
        
        return postPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据文章ID获取相关文章（基于相同标签和分类）
     * @param postId 当前文章ID
     * @param limit 返回文章数量限制
     * @return 相关文章列表
     */
    public List<PostDTO> getRelatedPosts(Long postId, int limit) {
        // 获取当前文章
        Post currentPost = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("文章未找到"));
        
        // 准备查询条件
        List<Long> tagIds = new ArrayList<>();
        if (currentPost.getTags() != null && !currentPost.getTags().isEmpty()) {
            tagIds = currentPost.getTags().stream()
                    .map(Tag::getId)
                    .collect(Collectors.toList());
        }
        
        List<Long> categoryIds = new ArrayList<>();
        if (currentPost.getCategory() != null) {
            categoryIds.add(currentPost.getCategory().getId());
        }
        
        // 查询相关文章
        Pageable pageable = PageRequest.of(0, limit + 1); // 多查一个，后面会过滤掉当前文章
        Page<Post> postPage = postRepository.findPostsByFilters(
                categoryIds.isEmpty() ? null : categoryIds,
                tagIds.isEmpty() ? null : tagIds,
                null,
                pageable
        );
        
        // 过滤掉当前文章，并限制数量
        return postPage.getContent().stream()
                .filter(post -> !post.getId().equals(postId))
                .map(this::convertToDTO)
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    private PostDTO convertToDTO(Post post) {
        CategoryDTO categoryDTO = null;
        if (post.getCategory() != null) {
            categoryDTO = new CategoryDTO(
                    post.getCategory().getId(),
                    post.getCategory().getName(),
                    post.getCategory().getCreatedAt());
        }
        
        List<TagDTO> tagDTOs = new ArrayList<>();
        if (post.getTags() != null) {
            tagDTOs = post.getTags().stream()
                    .map(tag -> new TagDTO(tag.getId(), tag.getName()))
                    .collect(Collectors.toList());
        }
        
        return new PostDTO(
                post.getId(),
                post.getTitle(),
                post.getDesc(),
                post.getContent(),
                post.getAuthor(),
                post.getCreatedAt(),
                categoryDTO,
                tagDTOs);
    }
}