# 网站合集功能 API 接口测试文档

## 概述
本文档描述了网站合集功能的完整后端API接口系统，包括网站管理、分类管理、标签管理等核心功能的接口定义和测试方法。

## 基础信息
- **基础URL**: `http://localhost:8080/api/websites`
- **内容类型**: `application/json`
- **字符编码**: `UTF-8`

## 1. 网站管理接口

### 1.1 创建网站
**接口**: `POST /api/websites`

**请求体**:
```json
{
  "name": "GitHub",
  "url": "https://github.com",
  "description": "全球最大的代码托管平台，支持Git版本控制",
  "categoryId": 1,
  "icon": "https://github.githubassets.com/favicons/favicon.svg",
  "favicon": "https://github.githubassets.com/favicons/favicon.svg",
  "screenshot": "https://example.com/screenshot.png",
  "tags": ["开发", "编程", "开源"],
  "isFavorite": true
}
```

**响应示例**:
```json
{
  "id": 1,
  "name": "GitHub",
  "url": "https://github.com",
  "description": "全球最大的代码托管平台，支持Git版本控制",
  "categoryId": 1,
  "categoryName": "开发工具",
  "categoryColor": "#409EFF",
  "categoryIcon": "🔧",
  "icon": "https://github.githubassets.com/favicons/favicon.svg",
  "favicon": "https://github.githubassets.com/favicons/favicon.svg",
  "screenshot": "https://example.com/screenshot.png",
  "visitCount": 0,
  "isFavorite": true,
  "isActive": true,
  "status": "active",
  "tags": ["开发", "编程", "开源"],
  "tagColors": ["#409EFF", "#409EFF", "#FF9800"],
  "createdAt": "2025-02-27 10:00:00",
  "updatedAt": "2025-02-27 10:00:00"
}
```

### 1.2 获取网站详情
**接口**: `GET /api/websites/{id}`

**路径参数**:
- `id`: 网站ID

**响应示例**: 同创建网站响应

### 1.3 根据URL获取网站
**接口**: `GET /api/websites/url?url={url}`

**查询参数**:
- `url`: 网站URL

**响应示例**: 同创建网站响应

### 1.4 更新网站
**接口**: `PUT /api/websites/{id}`

**路径参数**:
- `id`: 网站ID

**请求体**: 同创建网站请求体

**响应示例**: 同创建网站响应

### 1.5 删除网站
**接口**: `DELETE /api/websites/{id}`

**路径参数**:
- `id`: 网站ID

**响应**: `204 No Content`

### 1.6 批量删除网站
**接口**: `DELETE /api/websites/batch`

**请求体**:
```json
[1, 2, 3]
```

**响应**: `204 No Content`

## 2. 网站查询接口

### 2.1 查询网站列表（分页）
**接口**: `GET /api/websites`

**查询参数**:
- `page`: 页码（默认1）
- `size`: 每页大小（默认12）
- `keyword`: 搜索关键词
- `categoryId`: 分类ID
- `isFavorite`: 是否收藏
- `isActive`: 是否活跃
- `status`: 网站状态
- `sortBy`: 排序字段（createdAt, name, url, visitCount, updatedAt）
- `sortOrder`: 排序顺序（asc, desc）
- `startTime`: 开始时间
- `endTime`: 结束时间

**响应示例**:
```json
{
  "websites": [
    {
      "id": 1,
      "name": "GitHub",
      "url": "https://github.com",
      "description": "全球最大的代码托管平台，支持Git版本控制",
      "categoryId": 1,
      "categoryName": "开发工具",
      "categoryColor": "#409EFF",
      "categoryIcon": "🔧",
      "icon": "https://github.githubassets.com/favicons/favicon.svg",
      "visitCount": 156,
      "isFavorite": true,
      "tags": ["开发", "编程", "开源"],
      "createdAt": "2025-02-27 10:00:00"
    }
  ],
  "pagination": {
    "currentPage": 1,
    "pageSize": 12,
    "total": 1
  },
  "totalCount": 1,
  "totalPages": 1
}
```

### 2.2 根据分类查询网站
**接口**: `GET /api/websites/category/{categoryId}`

**路径参数**:
- `categoryId`: 分类ID

**响应示例**: 网站数组

### 2.3 根据收藏状态查询网站
**接口**: `GET /api/websites/favorite?isFavorite={isFavorite}`

**查询参数**:
- `isFavorite`: 是否收藏（true/false）

**响应示例**: 网站数组

### 2.4 搜索网站
**接口**: `GET /api/websites/search?keyword={keyword}`

**查询参数**:
- `keyword`: 搜索关键词

**响应示例**: 网站数组

### 2.5 根据标签查询网站
**接口**: `GET /api/websites/tag/{tagName}`

**路径参数**:
- `tagName`: 标签名称

**响应示例**: 网站数组

### 2.6 获取热门网站
**接口**: `GET /api/websites/popular?limit={limit}`

**查询参数**:
- `limit`: 返回数量（默认10）

**响应示例**: 网站数组

### 2.7 获取最新网站
**接口**: `GET /api/websites/recent?limit={limit}`

**查询参数**:
- `limit`: 返回数量（默认10）

**响应示例**: 网站数组

## 3. 网站操作接口

### 3.1 增加访问次数
**接口**: `POST /api/websites/{id}/visit`

**路径参数**:
- `id`: 网站ID

**响应**: `200 OK`

### 3.2 切换收藏状态
**接口**: `POST /api/websites/{id}/favorite`

**路径参数**:
- `id`: 网站ID

**响应**: `200 OK`

### 3.3 检查网站状态
**接口**: `POST /api/websites/{id}/check`

**路径参数**:
- `id`: 网站ID

**响应**: `200 OK`

### 3.4 批量检查网站状态
**接口**: `POST /api/websites/check/batch`

**响应**: `200 OK`

## 4. 统计和导入导出接口

### 4.1 获取网站统计信息
**接口**: `GET /api/websites/statistics`

**响应示例**:
```json
{
  "totalWebsites": 8,
  "totalCategories": 6,
  "totalTags": 8,
  "totalVisits": 1000,
  "favoriteWebsites": 5,
  "activeWebsites": 8
}
```

### 4.2 导入网站数据
**接口**: `POST /api/websites/import`

**请求体**:
```json
[
  {
    "name": "Stack Overflow",
    "url": "https://stackoverflow.com",
    "description": "程序员问答社区",
    "categoryId": 1,
    "tags": ["开发", "问答"],
    "isFavorite": true
  }
]
```

**响应示例**:
```json
{
  "totalCount": 1,
  "successCount": 1,
  "failedCount": 0,
  "errors": []
}
```

### 4.3 导出网站数据
**接口**: `GET /api/websites/export`

**查询参数**: 同查询网站列表

**响应示例**: 网站数组

## 5. 测试用例

### 5.1 基础CRUD测试

#### 测试1: 创建网站
```bash
curl -X POST http://localhost:8080/api/websites \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试网站",
    "url": "https://test.com",
    "description": "这是一个测试网站",
    "categoryId": 1,
    "tags": ["测试"],
    "isFavorite": false
  }'
```

#### 测试2: 查询网站列表
```bash
curl "http://localhost:8080/api/websites?page=1&size=10"
```

#### 测试3: 更新网站
```bash
curl -X PUT http://localhost:8080/api/websites/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "更新后的网站名称",
    "url": "https://test.com",
    "description": "这是更新后的描述",
    "categoryId": 1,
    "tags": ["测试", "更新"],
    "isFavorite": true
  }'
```

#### 测试4: 删除网站
```bash
curl -X DELETE http://localhost:8080/api/websites/1
```

### 5.2 高级查询测试

#### 测试5: 关键词搜索
```bash
curl "http://localhost:8080/api/websites/search?keyword=GitHub"
```

#### 测试6: 分类筛选
```bash
curl "http://localhost:8080/api/websites?categoryId=1&page=1&size=5"
```

#### 测试7: 收藏筛选
```bash
curl "http://localhost:8080/api/websites?isFavorite=true&page=1&size=5"
```

#### 测试8: 排序查询
```bash
curl "http://localhost:8080/api/websites?sortBy=visitCount&sortOrder=desc&page=1&size=5"
```

### 5.3 操作测试

#### 测试9: 增加访问次数
```bash
curl -X POST http://localhost:8080/api/websites/1/visit
```

#### 测试10: 切换收藏状态
```bash
curl -X POST http://localhost:8080/api/websites/1/favorite
```

#### 测试11: 检查网站状态
```bash
curl -X POST http://localhost:8080/api/websites/1/check
```

### 5.4 统计和导入导出测试

#### 测试12: 获取统计信息
```bash
curl http://localhost:8080/api/websites/statistics
```

#### 测试13: 导入网站数据
```bash
curl -X POST http://localhost:8080/api/websites/import \
  -H "Content-Type: application/json" \
  -d '[
    {
      "name": "导入网站1",
      "url": "https://import1.com",
      "description": "导入的网站1",
      "categoryId": 1,
      "tags": ["导入"],
      "isFavorite": false
    },
    {
      "name": "导入网站2",
      "url": "https://import2.com",
      "description": "导入的网站2",
      "categoryId": 2,
      "tags": ["导入"],
      "isFavorite": true
    }
  ]'
```

#### 测试14: 导出网站数据
```bash
curl "http://localhost:8080/api/websites/export?categoryId=1"
```

## 6. 错误处理测试

### 6.1 验证错误测试

#### 测试15: 缺少必填字段
```bash
curl -X POST http://localhost:8080/api/websites \
  -H "Content-Type: application/json" \
  -d '{
    "name": "",
    "url": "invalid-url",
    "description": ""
  }'
```

**预期响应**: `400 Bad Request`

#### 测试16: URL格式错误
```bash
curl -X POST http://localhost:8080/api/websites \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试网站",
    "url": "not-a-valid-url",
    "description": "测试描述",
    "categoryId": 1
  }'
```

**预期响应**: `400 Bad Request`

### 6.2 业务逻辑错误测试

#### 测试17: 重复URL
```bash
# 先创建一个网站
curl -X POST http://localhost:8080/api/websites \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试网站1",
    "url": "https://duplicate.com",
    "description": "测试描述1",
    "categoryId": 1
  }'

# 再创建相同URL的网站
curl -X POST http://localhost:8080/api/websites \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试网站2",
    "url": "https://duplicate.com",
    "description": "测试描述2",
    "categoryId": 1
  }'
```

**预期响应**: `400 Bad Request`

#### 测试18: 分类不存在
```bash
curl -X POST http://localhost:8080/api/websites \
  -H "Content-Type: application/json" \
  -d '{
    "name": "测试网站",
    "url": "https://test.com",
    "description": "测试描述",
    "categoryId": 999
  }'
```

**预期响应**: `400 Bad Request`

## 7. 性能测试

### 7.1 分页性能测试
```bash
# 测试大页面大小
curl "http://localhost:8080/api/websites?page=1&size=1000"

# 测试多页查询
curl "http://localhost:8080/api/websites?page=100&size=10"
```

### 7.2 搜索性能测试
```bash
# 测试长关键词搜索
curl "http://localhost:8080/api/websites/search?keyword=这是一个非常长的搜索关键词用来测试搜索性能"

# 测试特殊字符搜索
curl "http://localhost:8080/api/websites/search?keyword=!@#$%^&*()"
```

## 8. 安全测试

### 8.1 SQL注入测试
```bash
curl "http://localhost:8080/api/websites/search?keyword=' OR '1'='1"
```

### 8.2 XSS测试
```bash
curl -X POST http://localhost:8080/api/websites \
  -H "Content-Type: application/json" \
  -d '{
    "name": "<script>alert(\"xss\")</script>",
    "url": "https://test.com",
    "description": "测试描述<script>alert(\"xss\")</script>",
    "categoryId": 1
  }'
```

## 9. 测试工具推荐

### 9.1 Postman
- 创建集合保存所有API请求
- 使用环境变量管理不同环境的URL
- 设置自动化测试脚本

### 9.2 curl
- 命令行测试工具
- 适合自动化脚本
- 支持各种HTTP方法和参数

### 9.3 浏览器开发者工具
- 测试前端调用
- 查看网络请求和响应
- 调试JavaScript代码

## 10. 测试环境准备

### 10.1 数据库准备
1. 执行 `website-collection-schema.sql` 创建数据库表
2. 插入测试数据
3. 确保数据库连接正常

### 10.2 应用启动
1. 启动Spring Boot应用
2. 确认应用端口为8080
3. 检查日志无错误

### 10.3 测试数据准备
1. 创建测试分类
2. 创建测试标签
3. 准备测试网站数据

## 11. 测试报告模板

### 11.1 测试执行记录
| 测试用例 | 执行时间 | 执行结果 | 备注 |
|---------|---------|---------|------|
| 创建网站 | 2025-02-27 10:00:00 | 通过 | 正常 |
| 查询网站 | 2025-02-27 10:01:00 | 通过 | 正常 |
| 更新网站 | 2025-02-27 10:02:00 | 通过 | 正常 |
| 删除网站 | 2025-02-27 10:03:00 | 通过 | 正常 |

### 11.2 性能测试结果
| 测试场景 | 响应时间 | 并发用户数 | 成功率 |
|---------|---------|-----------|--------|
| 单用户查询 | 50ms | 1 | 100% |
| 10用户并发 | 120ms | 10 | 100% |
| 100用户并发 | 500ms | 100 | 95% |

### 11.3 问题记录
| 问题描述 | 严重程度 | 状态 | 解决方案 |
|---------|---------|------|---------|
| 大页面查询慢 | 中 | 已解决 | 优化SQL查询 |
| 特殊字符搜索异常 | 低 | 已解决 | 添加字符转义 |

## 12. 总结

本文档提供了网站合集功能的完整API接口测试指南，包括：

1. **完整的接口定义**: 涵盖所有CRUD操作和高级查询功能
2. **详细的测试用例**: 从基础功能到高级特性的全面测试
3. **错误处理测试**: 验证系统的健壮性和安全性
4. **性能测试**: 确保系统在高负载下的稳定性
5. **安全测试**: 防止常见的安全漏洞

通过系统性的测试，可以确保网站合集功能的质量和稳定性，为用户提供可靠的服务。
