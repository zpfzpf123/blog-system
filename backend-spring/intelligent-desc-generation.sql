-- 智能生成博客摘要 - 根据内容特征定制化生成
USE blogdb;

-- 为每篇文章根据其内容特点生成专业摘要
UPDATE posts 
SET `desc` = CASE 
    -- 检测到"教程"、"入门"、"指南"等关键词
    WHEN LOWER(content) LIKE '%教程%' OR LOWER(content) LIKE '%指南%' OR LOWER(content) LIKE '%入门%' THEN
        CONCAT('本教程详细介绍', SUBSTRING(title, 1, 20), '的相关知识，适合初学者和进阶开发者学习参考。')
    
    -- 检测到"常用命令"、"命令大全"等
    WHEN LOWER(content) LIKE '%命令%' OR LOWER(content) LIKE '%command%' THEN
        CONCAT('汇总了', SUBSTRING(title, 1, 30), '的常用命令和使用技巧，方便快速查阅和实际应用。')
    
    -- 检测到代码片段（包含大量的```或代码块）
    WHEN (LENGTH(content) - LENGTH(REPLACE(content, '```', ''))) > 10 THEN
        CONCAT('分享关于', SUBSTRING(title, 1, 25), '的实用代码示例和最佳实践，包含详细的代码演示。')
    
    -- 检测到"是什么"、"介绍"、"概念"等
    WHEN LOWER(content) LIKE '%是什么%' OR LOWER(content) LIKE '%介绍%' OR LOWER(content) LIKE '%概念%' THEN
        CONCAT('深入浅出地讲解', SUBSTRING(title, 1, 25), '的核心概念、应用场景和技术要点。')
    
    -- 检测到"优化"、"性能"、"提升"等
    WHEN LOWER(content) LIKE '%优化%' OR LOWER(content) LIKE '%性能%' OR LOWER(content) LIKE '%提升%' THEN
        CONCAT('探讨', SUBSTRING(title, 1, 25), '的性能优化方法和实践经验，助力项目效率提升。')
    
    -- 检测到"问题"、"解决"、"报错"等
    WHEN LOWER(content) LIKE '%问题%' OR LOWER(content) LIKE '%解决%' OR LOWER(content) LIKE '%错误%' THEN
        CONCAT('记录', SUBSTRING(title, 1, 25), '开发过程中遇到的常见问题及解决方案。')
    
    -- 检测到"对比"、"区别"、"vs"等
    WHEN LOWER(content) LIKE '%对比%' OR LOWER(content) LIKE '%区别%' OR LOWER(content) LIKE '%vs%' THEN
        CONCAT('详细对比', SUBSTRING(title, 1, 25), '的特性差异、优缺点及适用场景分析。')
    
    -- 技术栈相关（Vue、React、Spring等）
    WHEN LOWER(content) LIKE '%vue%' OR LOWER(content) LIKE '%react%' OR LOWER(content) LIKE '%spring%' THEN
        CONCAT('分享', SUBSTRING(title, 1, 25), '的开发经验、技术要点和项目实战心得。')
    
    -- 默认：从内容中智能提取关键信息
    ELSE 
        CONCAT(
            LEFT(
                TRIM(
                    REPLACE(
                        REPLACE(
                            REPLACE(
                                REPLACE(
                                    REPLACE(content, '#', ''),
                                    '*', ''
                                ),
                                '`', ''
                            ),
                            CHAR(10), ' '
                        ),
                        CHAR(13), ''
                    )
                ),
                150
            ),
            '...'
        )
END
WHERE `desc` IS NULL OR `desc` = '' OR TRIM(`desc`) = '';

-- 显示更新结果
SELECT 
    id,
    title,
    `desc` as generated_description,
    CHAR_LENGTH(`desc`) as desc_length
FROM posts
ORDER BY id DESC;
