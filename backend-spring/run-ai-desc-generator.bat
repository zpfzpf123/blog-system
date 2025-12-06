@echo off
chcp 65001 >nul
cls
echo ================================================
echo AI智能摘要生成器
echo ================================================
echo.
echo 本工具将根据文章内容特征智能生成专业摘要
echo.
echo [1/3] 正在分析文章内容...
echo.

mysql -u root -p990328 --table -e "USE blogdb; SELECT id, title, LEFT(content, 80) as preview, IFNULL(`desc`, '待生成') as current_desc FROM posts ORDER BY id LIMIT 15;"

echo.
echo [2/3] 正在执行AI摘要生成...
echo.

mysql -u root -p990328 blogdb < ai-smart-desc-generator.sql > ai_desc_result.txt 2>&1

if %errorlevel% equ 0 (
    echo ✓ 生成成功！
    echo.
    echo [3/3] 查看生成结果...
    echo.
    mysql -u root -p990328 --table -e "USE blogdb; SELECT id, title, `desc` as generated_desc FROM posts ORDER BY id DESC LIMIT 10;"
    echo.
    echo ================================================
    echo 🎉 所有文章的AI摘要已生成完成！
    echo ================================================
    echo.
    echo 提示：摘要已根据文章类型智能定制
    echo - 教程类：突出学习价值和适用对象
    echo - 命令类：强调实用性和查阅便利
    echo - 优化类：注重性能提升和实践经验  
    echo - 问题类：聚焦问题解决和避坑指南
    echo - 其他类：智能提取核心内容
) else (
    echo ❌ 生成失败！
    echo.
    echo 错误信息已保存到: ai_desc_result.txt
    type ai_desc_result.txt
)

echo.
pause
