package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Post Mapper 接口
 * 使用 MyBatis-Plus 进行数据访问
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {
    
    /**
     * 分页查询文章列表，支持多条件筛选
     * @param page 分页对象
     * @param categoryIds 分类ID列表
     * @param tagIds 标签ID列表
     * @param search 搜索关键字
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT DISTINCT p.* FROM posts p " +
            "LEFT JOIN post_tags pt ON p.id = pt.post_id " +
            "WHERE 1=1 " +
            "<if test='categoryIds != null and categoryIds.size() > 0'>" +
            "  AND p.category_id IN " +
            "  <foreach item='id' collection='categoryIds' open='(' separator=',' close=')'>" +
            "    #{id}" +
            "  </foreach>" +
            "</if>" +
            "<if test='tagIds != null and tagIds.size() > 0'>" +
            "  AND pt.tag_id IN " +
            "  <foreach item='id' collection='tagIds' open='(' separator=',' close=')'>" +
            "    #{id}" +
            "  </foreach>" +
            "</if>" +
            "<if test='search != null and search != \"\"'>" +
            "  AND (LOWER(p.title) LIKE CONCAT('%', LOWER(#{search}), '%') " +
            "    OR LOWER(p.content) LIKE CONCAT('%', LOWER(#{search}), '%') " +
            "    OR LOWER(p.`desc`) LIKE CONCAT('%', LOWER(#{search}), '%'))" +
            "</if>" +
            "</script>")
    IPage<Post> selectPostsByFilters(
            Page<Post> page,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("tagIds") List<Long> tagIds,
            @Param("search") String search
    );
}
