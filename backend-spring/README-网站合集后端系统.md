# 网站合集后端系统开发文档

## 🎯 项目概述

本项目为网站合集功能开发了完整的后端接口系统，实现了对网站核心数据的增删改查（CRUD）操作，并支持分页显示、搜索筛选、分类管理、标签系统等高级功能。

## 🏗️ 系统架构

### 技术栈
- **框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0+
- **ORM**: Spring Data JPA
- **验证**: Jakarta Validation
- **序列化**: Jackson JSON
- **构建工具**: Maven

### 架构层次
```
Controller Layer (控制器层)
    ↓
Service Layer (业务逻辑层)
    ↓
Repository Layer (数据访问层)
    ↓
Entity Layer (实体模型层)
    ↓
Database (数据库层)
```

## 📊 数据库设计

### 核心表结构

#### 1. 网站分类表 (`website_categories`)
- 支持分类名称、描述、颜色、图标、排序等属性
- 支持软删除和状态管理
- 包含创建时间、更新时间等审计字段

#### 2. 网站信息表 (`websites`)
- 存储网站的基本信息（名称、URL、描述、图标等）
- 支持分类关联、标签关联
- 包含访问统计、收藏状态、网站状态等字段
- 支持软删除和状态管理

#### 3. 网站标签表 (`website_tags`)
- 支持标签名称、颜色、描述等属性
- 包含使用次数统计
- 支持软删除和状态管理

#### 4. 关联表
- `website_tag_relations`: 网站与标签的多对多关联
- `website_visits`: 网站访问记录
- `website_favorites`: 网站收藏记录
- `website_imports`: 导入记录
- `website_statistics`: 统计信息缓存

### 数据库特性
- **外键约束**: 确保数据完整性
- **索引优化**: 提升查询性能
- **视图支持**: 简化复杂查询
- **存储过程**: 支持批量操作
- **触发器**: 自动维护统计数据

## 🔧 核心功能模块

### 1. 网站管理模块
- **CRUD操作**: 完整的增删改查功能
- **批量操作**: 支持批量删除、批量状态更新
- **软删除**: 数据安全删除，支持恢复
- **状态管理**: 支持活跃、非活跃、损坏等状态

### 2. 分类管理模块
- **分类CRUD**: 支持分类的创建、编辑、删除
- **排序管理**: 支持分类的排序调整
- **状态控制**: 支持分类的启用/禁用

### 3. 标签系统模块
- **标签管理**: 支持标签的创建、编辑、删除
- **自动创建**: 网站创建时自动创建新标签
- **使用统计**: 自动统计标签使用次数
- **热门标签**: 支持热门标签查询

### 4. 搜索筛选模块
- **关键词搜索**: 支持名称、描述、URL的模糊搜索
- **分类筛选**: 按分类筛选网站
- **状态筛选**: 按收藏状态、活跃状态筛选
- **时间筛选**: 支持时间范围筛选
- **排序支持**: 支持多种排序方式

### 5. 分页查询模块
- **灵活分页**: 支持自定义页面大小
- **性能优化**: 基于数据库的分页查询
- **统计信息**: 返回总数量、总页数等信息

### 6. 统计功能模块
- **访问统计**: 记录和统计网站访问次数
- **数据统计**: 提供网站、分类、标签的统计信息
- **实时更新**: 统计数据实时更新

### 7. 导入导出模块
- **批量导入**: 支持JSON格式的批量导入
- **数据导出**: 支持按条件导出网站数据
- **错误处理**: 导入失败时的详细错误信息

## 📁 项目结构

```
backend-spring/
├── src/main/java/com/blog/
│   ├── entity/                    # 实体类
│   │   ├── Website.java          # 网站实体
│   │   ├── WebsiteCategory.java  # 分类实体
│   │   └── WebsiteTag.java       # 标签实体
│   ├── dto/                      # 数据传输对象
│   │   ├── WebsiteDTO.java       # 网站DTO
│   │   ├── WebsiteCreateRequest.java  # 创建请求DTO
│   │   ├── WebsiteQueryRequest.java   # 查询请求DTO
│   │   └── WebsitesResponseDTO.java   # 响应DTO
│   ├── repository/               # 数据访问层
│   │   ├── WebsiteRepository.java      # 网站Repository
│   │   ├── WebsiteCategoryRepository.java  # 分类Repository
│   │   └── WebsiteTagRepository.java       # 标签Repository
│   ├── service/                  # 业务逻辑层
│   │   ├── WebsiteService.java          # 网站服务接口
│   │   └── impl/
│   │       └── WebsiteServiceImpl.java  # 网站服务实现
│   └── controller/               # 控制器层
│       └── WebsiteController.java       # 网站控制器
├── website-collection-schema.sql # 数据库建表脚本
├── API接口测试文档-网站合集.md    # API测试文档
└── README-网站合集后端系统.md    # 本文档
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 安装步骤

#### 1. 数据库准备
```sql
-- 执行数据库建表脚本
source website-collection-schema.sql;
```

#### 2. 应用配置
在 `application.properties` 中配置数据库连接：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate
```

#### 3. 启动应用
```bash
mvn spring-boot:run
```

#### 4. 验证启动
访问 `http://localhost:8080/api/websites` 验证接口是否正常

## 📡 API接口说明

### 基础URL
- **开发环境**: `http://localhost:8080/api/websites`
- **生产环境**: `https://your-domain.com/api/websites`

### 主要接口

#### 网站管理
- `POST /api/websites` - 创建网站
- `GET /api/websites/{id}` - 获取网站详情
- `PUT /api/websites/{id}` - 更新网站
- `DELETE /api/websites/{id}` - 删除网站
- `DELETE /api/websites/batch` - 批量删除

#### 网站查询
- `GET /api/websites` - 分页查询网站列表
- `GET /api/websites/category/{categoryId}` - 按分类查询
- `GET /api/websites/favorite?isFavorite={isFavorite}` - 按收藏状态查询
- `GET /api/websites/search?keyword={keyword}` - 关键词搜索
- `GET /api/websites/tag/{tagName}` - 按标签查询

#### 网站操作
- `POST /api/websites/{id}/visit` - 增加访问次数
- `POST /api/websites/{id}/favorite` - 切换收藏状态
- `POST /api/websites/{id}/check` - 检查网站状态

#### 统计和导入导出
- `GET /api/websites/statistics` - 获取统计信息
- `POST /api/websites/import` - 批量导入
- `GET /api/websites/export` - 数据导出

### 请求参数示例

#### 创建网站
```json
{
  "name": "GitHub",
  "url": "https://github.com",
  "description": "全球最大的代码托管平台",
  "categoryId": 1,
  "tags": ["开发", "编程", "开源"],
  "isFavorite": true
}
```

#### 查询参数
```
GET /api/websites?page=1&size=12&keyword=GitHub&categoryId=1&sortBy=createdAt&sortOrder=desc
```

## 🧪 测试指南

### 测试工具
- **Postman**: 推荐使用Postman进行API测试
- **curl**: 命令行测试工具
- **浏览器**: 用于前端集成测试

### 测试用例
详细的测试用例请参考 `API接口测试文档-网站合集.md`

### 测试环境
- 开发环境: `http://localhost:8080`
- 测试环境: `http://test-server:8080`
- 生产环境: `https://production-server.com`

## 🔒 安全特性

### 数据验证
- **输入验证**: 使用Jakarta Validation进行参数验证
- **URL验证**: 确保URL格式正确
- **长度限制**: 防止超长输入攻击

### 错误处理
- **统一异常处理**: 提供友好的错误信息
- **日志记录**: 记录操作日志便于审计
- **状态码规范**: 使用标准HTTP状态码

### 数据安全
- **软删除**: 防止数据意外丢失
- **状态管理**: 支持数据状态控制
- **访问控制**: 支持用户权限管理（可扩展）

## 📈 性能优化

### 数据库优化
- **索引设计**: 针对常用查询字段建立索引
- **查询优化**: 使用JPA的查询优化功能
- **分页查询**: 避免大量数据一次性加载

### 应用优化
- **缓存策略**: 支持统计数据缓存
- **批量操作**: 支持批量导入导出
- **异步处理**: 支持异步状态检查

### 监控指标
- **响应时间**: 监控API响应时间
- **并发处理**: 支持高并发访问
- **资源使用**: 监控系统资源使用情况

## 🔧 扩展功能

### 可扩展模块
- **用户管理**: 支持多用户系统
- **权限控制**: 基于角色的访问控制
- **数据同步**: 支持多数据源同步
- **API限流**: 防止API滥用

### 集成支持
- **第三方登录**: 支持OAuth2.0
- **消息队列**: 支持异步消息处理
- **缓存系统**: 支持Redis缓存
- **搜索引擎**: 支持Elasticsearch集成

## 🐛 常见问题

### Q1: 如何处理大量数据的分页查询？
A: 系统使用数据库级别的分页查询，通过LIMIT和OFFSET实现高效分页。

### Q2: 标签系统如何避免重复？
A: 系统在创建网站时自动检查标签是否存在，不存在则自动创建，确保标签唯一性。

### Q3: 如何实现网站状态检查？
A: 系统提供手动和批量状态检查接口，可以集成第三方服务进行网站可用性检测。

### Q4: 支持哪些数据导入格式？
A: 目前支持JSON格式的批量导入，可以扩展支持CSV、Excel等格式。

### Q5: 如何处理并发访问？
A: 系统使用数据库事务和乐观锁机制处理并发访问，确保数据一致性。

## 📚 相关文档

- [API接口测试文档](./API接口测试文档-网站合集.md)
- [数据库设计文档](./website-collection-schema.sql)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [Spring Data JPA文档](https://spring.io/projects/spring-data-jpa)

## 🤝 贡献指南

### 开发规范
- 遵循Java编码规范
- 使用有意义的变量和方法名
- 添加必要的注释和文档
- 编写单元测试

### 提交流程
1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 📄 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：
- 项目Issues: [GitHub Issues](https://github.com/your-repo/issues)
- 邮箱: your-email@example.com
- 微信: your-wechat-id

---

**最后更新**: 2025-02-27  
**版本**: 1.0.0  
**维护者**: 开发团队
