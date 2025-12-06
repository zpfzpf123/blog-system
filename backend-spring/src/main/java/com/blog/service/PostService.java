package com.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Post;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.mapper.PostMapper;
import com.blog.repository.PostRepository;
import com.blog.repository.CategoryRepository;
import com.blog.repository.TagRepository;
import com.blog.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class PostService {
    
    @Autowired
    private PostMapper postMapper;
    
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;
    
    public PostsResponseDTO getPosts(Integer page, Integer limit, List<Long> categoryIds, 
                                     List<Long> tagIds, String search, String sortBy, String sortOrder) {
        page = (page == null || page < 1) ? 1 : page;
        limit = (limit == null || limit < 1) ? 10 : limit;
        sortBy = (sortBy == null || sortBy.isEmpty()) ? "id" : sortBy;
        sortOrder = (sortOrder == null || sortOrder.isEmpty()) ? "desc" : sortOrder;
        
        // 创建 MyBatis-Plus 分页对象
        Page<Post> mybatisPage = new Page<>(page, limit);
        
        // 转换 Java 字段名为数据库列名（驼峰转下划线）
        String dbColumnName = camelToUnderscore(sortBy);
        
        System.out.println("【MyBatis-Plus分页】排序字段转换 - Java字段: " + sortBy + " -> 数据库列: " + dbColumnName);
        
        // 设置排序字段（使用数据库列名）
        com.baomidou.mybatisplus.core.metadata.OrderItem orderItem;
        if ("asc".equalsIgnoreCase(sortOrder)) {
            orderItem = new com.baomidou.mybatisplus.core.metadata.OrderItem();
            orderItem.setColumn(dbColumnName);
            orderItem.setAsc(true);
            mybatisPage.addOrder(orderItem);
        } else {
            orderItem = new com.baomidou.mybatisplus.core.metadata.OrderItem();
            orderItem.setColumn(dbColumnName);
            orderItem.setAsc(false);
            mybatisPage.addOrder(orderItem);
        }
        // 添加 ID 作为辅助排序，确保顺序稳定
        if (!"id".equals(sortBy)) {
            com.baomidou.mybatisplus.core.metadata.OrderItem idOrder = new com.baomidou.mybatisplus.core.metadata.OrderItem();
            idOrder.setColumn("id");
            idOrder.setAsc(false);
            mybatisPage.addOrder(idOrder);
        }
        
        System.out.println("【MyBatis-Plus分页】查询文章列表 - Page: " + page + ", Size: " + limit + 
                           ", SortBy: " + dbColumnName + ", Order: " + sortOrder);
        
        // 执行分页查询
        IPage<Post> resultPage = postMapper.selectPostsByFilters(
            mybatisPage,
            categoryIds,
            tagIds,
            search
        );
        
        // 手动加载关联数据（category 和 tags）
        List<Post> posts = resultPage.getRecords();
        for (Post post : posts) {
            // 通过 JPA Repository 重新加载完整数据
            Post fullPost = postRepository.findById(post.getId()).orElse(post);
            // 强制加载懒加载的关联数据
            if (fullPost.getCategory() != null) {
                fullPost.getCategory().getName(); // 触发加载
            }
            if (fullPost.getTags() != null && !fullPost.getTags().isEmpty()) {
                fullPost.getTags().size(); // 触发加载
            }
            // 用完整数据替换
            post.setCategory(fullPost.getCategory());
            post.setTags(fullPost.getTags());
        }
        
        // 转换为 DTO
        List<PostDTO> postDTOs = posts.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建分页信息
        PaginationDTO pagination = new PaginationDTO(
                (int) resultPage.getCurrent(),
                (int) resultPage.getPages(),
                resultPage.getTotal()
        );
        
        System.out.println("【MyBatis-Plus分页】查询完成 - 总记录数: " + resultPage.getTotal() + 
                           ", 总页数: " + resultPage.getPages() + ", 当前页记录数: " + resultPage.getRecords().size());
        
        return new PostsResponseDTO(postDTOs, pagination);
    }


    
    public PostsResponseDTO getPostsByCategory(Long categoryId, Integer page, Integer limit) {
        // 使用 MyBatis-Plus 实现，传入分类ID列表
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        return getPosts(page, limit, categoryIds, null, null, "id", "desc");
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
        
        // 使用 MyBatis-Plus 实现
        PostsResponseDTO response = getPosts(1, limit, null, tagIds, null, "id", "desc");
        return response.getPosts();
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
        
        // 使用 MyBatis-Plus 查询相关文章
        PostsResponseDTO response = getPosts(1, limit + 1, categoryIds, tagIds, null, "id", "desc");
        
        // 过滤掉当前文章，并限制数量
        return response.getPosts().stream()
                .filter(post -> !post.getId().equals(postId))
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    /**
     * 驼峰转下划线工具方法
     * 例如：createdAt -> created_at
     */
    private String camelToUnderscore(String camelCase) {
        if (camelCase == null || camelCase.isEmpty()) {
            return camelCase;
        }
        StringBuilder result = new StringBuilder();
        result.append(Character.toLowerCase(camelCase.charAt(0)));
        for (int i = 1; i < camelCase.length(); i++) {
            char ch = camelCase.charAt(i);
            if (Character.isUpperCase(ch)) {
                result.append('_');
                result.append(Character.toLowerCase(ch));
            } else {
                result.append(ch);
            }
        }
        return result.toString();
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