# SQL语法错误修复指南

## 问题描述
保存文章时出现错误：
```
{
    "message": "could not execute statement; SQL [n/a]; nested exception is org.hibernate.exception.SQLGrammarException: could not execute statement"
}
```

## 问题原因分析

1. **实体类与数据库表结构不匹配**
   - Post.java实体类中content字段之前定义为`@Column(length = 10000)`
   - 这会导致Hibernate尝试创建VARCHAR(10000)
   - 但数据库schema.sql中定义的是TEXT类型
   - 类型不匹配导致SQL执行失败

2. **关键字冲突**
   - `desc`是MySQL的关键字，需要使用反引号`` `desc` ``保护

## 修复步骤

### 步骤1：执行SQL修复脚本

运行 `fix-posts-table.sql` 修复数据库表结构：

```bash
mysql -u root -p990328 < fix-posts-table.sql
```

或者在MySQL客户端中执行：

```sql
USE blogdb;
ALTER TABLE posts MODIFY COLUMN content LONGTEXT NOT NULL;
ALTER TABLE posts MODIFY COLUMN `desc` VARCHAR(500);
```

### 步骤2：重新编译并重启后端

运行批处理文件：
```bash
rebuild-and-restart.bat
```

或者手动执行：
```bash
# 停止现有Java进程
taskkill /F /IM java.exe

# 重新编译
mvn clean package -DskipTests

# 启动服务
java -jar target\backend-0.0.1-SNAPSHOT.jar
```

### 步骤3：验证修复

1. 打开浏览器访问：http://localhost:4567/api/posts
2. 尝试创建新文章
3. 尝试编辑现有文章
4. 确认保存成功

## 已修复的代码

### Post.java 修改前：
```java
@NotBlank(message = "文章内容不能为空")
@Column(length = 10000)
private String content;
```

### Post.java 修改后：
```java
@NotBlank(message = "文章内容不能为空")
@Column(columnDefinition = "LONGTEXT")
private String content;
```

## 预防措施

1. **使用columnDefinition明确指定字段类型**
   - 对于大文本字段，使用`@Column(columnDefinition = "LONGTEXT")`
   - 避免使用`length`属性导致类型不匹配

2. **保护MySQL关键字**
   - 对于`desc`等关键字，使用`` `desc` ``
   - 在@Column注解中明确指定：`@Column(name = "desc")`

3. **保持实体类与数据库一致**
   - 修改实体类后，检查数据库表结构
   - 使用`spring.jpa.hibernate.ddl-auto=update`自动同步
   - 或者手动执行ALTER TABLE语句

## 测试清单

- [ ] 创建新文章（带标题、内容、摘要）
- [ ] 编辑现有文章
- [ ] 文章内容超过1000字
- [ ] 文章内容包含特殊字符
- [ ] 设置分类和标签
- [ ] 不设置分类和标签（可选字段）

## 相关文件

- `Post.java` - 文章实体类（已修复）
- `fix-posts-table.sql` - 数据库修复脚本
- `rebuild-and-restart.bat` - 重启服务脚本
- `blogdb-schema.sql` - 原始数据库结构
