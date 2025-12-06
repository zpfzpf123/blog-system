# API 文档 & 开发环境管理功能说明

## 功能概述

为项目管理系统新增了两个实用功能模块：

### 1. API 文档管理 📝

在项目详情页新增「API文档」标签页，提供完整的接口管理能力：

**核心功能：**
- **接口列表管理** - 按分类展示所有 API 接口
- **自动扫描** - 从项目代码自动发现 API（支持 Spring Boot Controller 和前端 axios 调用）
- **接口调试** - 类似 Postman 的接口测试工具
- **Mock 数据** - 为每个接口配置 Mock 响应，支持延迟和状态码设置
- **状态追踪** - 追踪接口开发状态（待开发/开发中/测试中/已完成）
- **文档导出** - 支持导出 Markdown 和 OpenAPI JSON 格式

**使用方式：**
1. 进入项目详情页
2. 点击「API文档」标签
3. 点击「扫描代码」自动发现接口，或手动添加
4. 点击接口的「测试」按钮进行调试
5. 开启 Mock 模式可返回模拟数据

---

### 2. 开发环境管理 🔧

在项目详情页新增「开发环境」标签页，提供可视化的服务管理：

**核心功能：**
- **服务配置** - 配置前端/后端/数据库等服务的启动命令
- **一键启停** - 单个服务或批量启动/停止/重启
- **状态监控** - 实时显示服务运行状态
- **端口检测** - 检测端口占用情况
- **日志查看** - 实时查看服务输出日志
- **启动顺序** - 支持配置服务启动顺序

**使用方式：**
1. 进入项目详情页
2. 点击「开发环境」标签
3. 点击「添加服务」配置服务信息
4. 点击「启动」按钮启动服务
5. 点击服务卡片查看实时日志

---

## 数据库变更

需要执行 SQL 脚本创建新表：

```sql
-- 执行 sql/V4__add_project_services_and_apis.sql
```

## 新增文件

### 后端
- `entity/DevServiceConfig.java` - 服务配置实体
- `entity/ProjectApi.java` - API 接口实体
- `repository/DevServiceConfigRepository.java` - 服务配置仓库
- `repository/ProjectApiRepository.java` - API 接口仓库
- `controller/DevEnvironmentController.java` - 开发环境控制器
- `controller/ProjectApiController.java` - API 文档控制器

### 前端
- `components/DevEnvironmentPanel.vue` - 开发环境管理面板
- `components/ApiDocPanel.vue` - API 文档管理面板

## API 接口

### 开发环境管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/projects/{id}/dev/services` | GET | 获取服务列表 |
| `/api/projects/{id}/dev/services` | POST | 添加服务 |
| `/api/projects/{id}/dev/services/{serviceId}` | PUT | 更新服务 |
| `/api/projects/{id}/dev/services/{serviceId}` | DELETE | 删除服务 |
| `/api/projects/{id}/dev/services/{serviceId}/start` | POST | 启动服务 |
| `/api/projects/{id}/dev/services/{serviceId}/stop` | POST | 停止服务 |
| `/api/projects/{id}/dev/services/{serviceId}/restart` | POST | 重启服务 |
| `/api/projects/{id}/dev/services/{serviceId}/logs` | GET | 获取日志 |
| `/api/projects/{id}/dev/start-all` | POST | 启动所有服务 |
| `/api/projects/{id}/dev/stop-all` | POST | 停止所有服务 |
| `/api/projects/{id}/dev/check-port/{port}` | GET | 检查端口 |

### API 文档管理
| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/projects/{id}/apis` | GET | 获取 API 列表 |
| `/api/projects/{id}/apis` | POST | 添加 API |
| `/api/projects/{id}/apis/{apiId}` | PUT | 更新 API |
| `/api/projects/{id}/apis/{apiId}` | DELETE | 删除 API |
| `/api/projects/{id}/apis/{apiId}/status` | PATCH | 更新状态 |
| `/api/projects/{id}/apis/{apiId}/mock` | PATCH | 切换 Mock |
| `/api/projects/{id}/apis/{apiId}/test` | POST | 测试接口 |
| `/api/projects/{id}/apis/scan` | POST | 扫描代码 |
| `/api/projects/{id}/apis/import` | POST | 导入 API |
| `/api/projects/{id}/apis/export` | GET | 导出文档 |
