-- AI智能摘要生成器 - 基于内容深度分析生成专业摘要
-- 作者建议：根据文章主题、技术栈、内容类型定制化生成
USE blogdb;

-- 第一步：清理和标准化现有数据
UPDATE posts SET `desc` = NULL WHERE TRIM(IFNULL(`desc`, '')) = '';

-- 第二步：根据文章内容特征智能生成摘要
UPDATE posts p
SET `desc` = CASE 
    
    -- ========== 技术教程类 ==========
    WHEN p.title LIKE '%教程%' OR p.title LIKE '%指南%' OR p.title LIKE '%入门%' 
         OR p.content LIKE '%教程%' OR p.content LIKE '%步骤%' THEN
        CONCAT(
            '本文是一篇关于', 
            SUBSTRING_INDEX(SUBSTRING_INDEX(p.title, ' ', 1), '-', 1),
            '的实用教程，涵盖基础概念、核心功能和实践案例，适合各阶段开发者学习参考。'
        )
    
    -- ========== 命令/工具类 ==========
    WHEN p.title LIKE '%命令%' OR p.title LIKE '%command%' 
         OR p.content LIKE '%命令%' OR p.content LIKE '%指令%' THEN
        CONCAT(
            '整理了',
            SUBSTRING_INDEX(p.title, ' ', 1),
            '的常用命令集合，包括安装配置、日常操作和高级技巧，方便开发过程中快速查阅使用。'
        )
    
    -- ========== 框架/库介绍类 ==========
    WHEN p.title LIKE '%Vue%' OR p.title LIKE '%React%' OR p.title LIKE '%Spring%'
         OR p.title LIKE '%Angular%' OR p.title LIKE '%Node%' THEN
        CONCAT(
            '深入探讨',
            SUBSTRING_INDEX(p.title, ' ', 2),
            '框架的核心特性、开发实践和最佳应用场景，帮助开发者快速掌握和应用。'
        )
    
    -- ========== 性能优化类 ==========
    WHEN p.content LIKE '%优化%' OR p.content LIKE '%性能%' 
         OR p.content LIKE '%提升%' OR p.content LIKE '%加速%' THEN
        CONCAT(
            '分享',
            SUBSTRING_INDEX(p.title, '优化', 1),
            '的性能优化策略和实战经验，包含具体优化方法、效果对比和注意事项。'
        )
    
    -- ========== 问题解决类 ==========
    WHEN p.content LIKE '%问题%' OR p.content LIKE '%解决%' 
         OR p.content LIKE '%报错%' OR p.content LIKE '%ERROR%' THEN
        CONCAT(
            '记录',
            SUBSTRING_INDEX(p.title, ' ', 3),
            '开发中遇到的典型问题、排查思路和解决方案，帮助开发者避坑和快速定位问题。'
        )
    
    -- ========== 技术对比类 ==========
    WHEN p.content LIKE '%对比%' OR p.content LIKE '%区别%' 
         OR p.content LIKE '%vs%' OR p.content LIKE '%比较%' THEN
        CONCAT(
            '详细对比',
            SUBSTRING_INDEX(p.title, 'vs', 1),
            '的特性差异、性能表现和应用场景，为技术选型提供参考依据。'
        )
    
    -- ========== 前端开发类 ==========
    WHEN p.content LIKE '%CSS%' OR p.content LIKE '%HTML%' 
         OR p.content LIKE '%JavaScript%' OR p.content LIKE '%前端%' THEN
        CONCAT(
            '介绍',
            SUBSTRING_INDEX(p.title, ' ', 2),
            '在前端开发中的应用技巧、实现方法和常见场景，提升前端开发效率。'
        )
    
    -- ========== 后端开发类 ==========
    WHEN p.content LIKE '%API%' OR p.content LIKE '%接口%' 
         OR p.content LIKE '%数据库%' OR p.content LIKE '%服务器%' THEN
        CONCAT(
            '讲解',
            SUBSTRING_INDEX(p.title, ' ', 2),
            '的后端实现方案、接口设计和数据处理逻辑，助力构建稳定可靠的后端服务。'
        )
    
    -- ========== 工具使用类 ==========
    WHEN p.content LIKE '%安装%' OR p.content LIKE '%配置%' 
         OR p.content LIKE '%使用%' OR p.content LIKE '%设置%' THEN
        CONCAT(
            '详述',
            SUBSTRING_INDEX(p.title, ' ', 2),
            '工具的安装配置、基本使用和常用功能，帮助开发者快速上手。'
        )
    
    -- ========== 项目实战类 ==========
    WHEN p.content LIKE '%项目%' OR p.content LIKE '%实战%' 
         OR p.content LIKE '%开发%' OR p.content LIKE '%搭建%' THEN
        CONCAT(
            '通过',
            SUBSTRING_INDEX(p.title, ' ', 2),
            '项目实战，分享完整的开发流程、技术选型和实现细节，积累项目经验。'
        )
    
    -- ========== 概念解析类 ==========
    WHEN p.content LIKE '%是什么%' OR p.content LIKE '%概念%' 
         OR p.content LIKE '%原理%' OR p.content LIKE '%理解%' THEN
        CONCAT(
            '深入浅出地解析',
            SUBSTRING_INDEX(p.title, ' ', 2),
            '的核心概念、工作原理和应用价值，帮助理解底层机制。'
        )
    
    -- ========== 最佳实践类 ==========
    WHEN p.content LIKE '%最佳实践%' OR p.content LIKE '%规范%' 
         OR p.content LIKE '%标准%' OR p.content LIKE '%建议%' THEN
        CONCAT(
            '总结',
            SUBSTRING_INDEX(p.title, ' ', 2),
            '的最佳实践和开发规范，提供可落地的实施建议和注意事项。'
        )
    
    -- ========== 默认智能提取 ==========
    ELSE 
        -- 智能提取：去除标记符号，取关键内容
        CONCAT(
            SUBSTRING(
                TRIM(
                    -- 移除Markdown标记
                    REGEXP_REPLACE(
                        REGEXP_REPLACE(
                            REGEXP_REPLACE(
                                REGEXP_REPLACE(
                                    p.content,
                                    '#{1,6}\\s*', ''  -- 去除标题#
                                ),
                                '[*_`]', ''  -- 去除加粗斜体代码标记
                            ),
                            '\\[.*?\\]\\(.*?\\)', ''  -- 去除链接
                        ),
                        '\\s+', ' '  -- 多个空格合并为一个
                    )
                ),
                1, 120
            ),
            '...'
        )
END
WHERE p.`desc` IS NULL OR p.`desc` = '';

-- 第三步：显示生成结果
SELECT 
    id,
    title,
    `desc` as ai_generated_description,
    CHAR_LENGTH(`desc`) as length,
    CASE 
        WHEN title LIKE '%教程%' THEN '教程类'
        WHEN title LIKE '%命令%' THEN '工具类'
        WHEN content LIKE '%优化%' THEN '优化类'
        WHEN content LIKE '%问题%' THEN '问题类'
        ELSE '通用类'
    END as article_type
FROM posts
ORDER BY id DESC;
