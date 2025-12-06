-- 自动为所有文章生成desc摘要
-- 策略：从content中智能提取摘要，去除markdown标记，取前150-200字符

USE blogdb;

-- 更新所有desc为null或空的文章
UPDATE posts 
SET `desc` = CASE 
    -- 如果内容以markdown标题开头，提取第一段非标题内容
    WHEN content LIKE '#%' THEN
        CONCAT(
            SUBSTRING(
                -- 去除markdown标记和多余空格
                TRIM(
                    REGEXP_REPLACE(
                        REGEXP_REPLACE(
                            REGEXP_REPLACE(content, '#{1,6}\\s*', ''), -- 去除标题符号
                            '\\*\\*|\\*|__|_|`', ''  -- 去除加粗、斜体、代码标记
                        ),
                        '\\n+', ' '  -- 换行符替换为空格
                    )
                ),
                1, 180
            ),
            '...'
        )
    -- 普通内容直接提取
    ELSE 
        CONCAT(
            SUBSTRING(
                TRIM(
                    REGEXP_REPLACE(
                        REGEXP_REPLACE(content, '\\*\\*|\\*|__|_|`', ''),
                        '\\n+', ' '
                    )
                ),
                1, 180
            ),
            '...'
        )
END
WHERE `desc` IS NULL OR `desc` = '' OR TRIM(`desc`) = '';

-- 显示更新后的结果
SELECT 
    id,
    title,
    `desc` as description,
    CHAR_LENGTH(`desc`) as desc_length,
    created_at
FROM posts
ORDER BY id DESC;
