USE blogdb;

-- 简单粗暴的方式：为所有desc为空的文章生成摘要
UPDATE posts 
SET `desc` = CONCAT(
    LEFT(
        TRIM(
            REPLACE(
                REPLACE(
                    REPLACE(
                        REPLACE(content, '#', ''),
                        '*', ''
                    ),
                    CHAR(10), ' '
                ),
                CHAR(13), ''
            )
        ),
        180
    ),
    '...'
)
WHERE `desc` IS NULL OR `desc` = '' OR TRIM(`desc`) = '';
