# 后端API接口说明 - 图片清理功能

## 概述

为了实现编辑博客后自动清理已删除图片的功能，需要在后端添加一个新的API接口，用于删除不再使用的图片文件。

## 需要实现的API接口

### 删除未使用的图片

**接口地址**: `POST /api/uploads/delete-unused`

**请求方法**: POST

**请求头**: 
```
Content-Type: application/json
```

**请求体**:
```json
{
  "imageUrls": [
    "/api/uploads/image1.jpg",
    "/api/uploads/image2.png",
    "/api/uploads/image3.gif"
  ]
}
```

**请求参数说明**:
- `imageUrls`: 字符串数组，包含需要删除的图片URL路径

**响应格式**:
```json
{
  "success": true,
  "message": "成功删除3张图片",
  "deletedCount": 3,
  "failedUrls": []
}
```

**响应参数说明**:
- `success`: 布尔值，表示操作是否成功
- `message`: 字符串，操作结果描述
- `deletedCount`: 数字，成功删除的图片数量
- `failedUrls`: 字符串数组，删除失败的图片URL列表

## 实现逻辑

### 1. 图片路径处理
- 接收的图片URL可能是相对路径（如 `/api/uploads/filename.jpg`）
- 需要提取文件名，找到对应的物理文件路径
- 验证文件是否存在，避免删除不存在的文件

### 2. 文件删除
- 安全删除文件，确保文件完全移除
- 记录删除操作日志
- 处理删除失败的情况

### 3. 错误处理
- 文件不存在的情况
- 文件权限不足的情况
- 文件被其他进程占用的情况

## 示例实现（Java Spring Boot）

```java
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    @PostMapping("/delete-unused")
    public ResponseEntity<Map<String, Object>> deleteUnusedImages(
            @RequestBody DeleteUnusedImagesRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        List<String> failedUrls = new ArrayList<>();
        int deletedCount = 0;
        
        try {
            for (String imageUrl : request.getImageUrls()) {
                try {
                    // 提取文件名
                    String fileName = extractFileName(imageUrl);
                    
                    // 构建文件路径
                    Path filePath = Paths.get(uploadDir, fileName);
                    
                    // 检查文件是否存在
                    if (Files.exists(filePath)) {
                        // 删除文件
                        Files.delete(filePath);
                        deletedCount++;
                        log.info("成功删除图片文件: {}", fileName);
                    } else {
                        log.warn("图片文件不存在: {}", fileName);
                    }
                    
                } catch (Exception e) {
                    log.error("删除图片失败: {}", imageUrl, e);
                    failedUrls.add(imageUrl);
                }
            }
            
            response.put("success", true);
            response.put("message", String.format("成功删除%d张图片", deletedCount));
            response.put("deletedCount", deletedCount);
            response.put("failedUrls", failedUrls);
            
        } catch (Exception e) {
            log.error("批量删除图片失败", e);
            response.put("success", false);
            response.put("message", "删除图片失败: " + e.getMessage());
            response.put("deletedCount", 0);
            response.put("failedUrls", request.getImageUrls());
        }
        
        return ResponseEntity.ok(response);
    }
    
    private String extractFileName(String imageUrl) {
        // 从URL中提取文件名
        if (imageUrl.startsWith("/api/uploads/")) {
            return imageUrl.substring("/api/uploads/".length());
        }
        return imageUrl;
    }
}

// 请求体类
public class DeleteUnusedImagesRequest {
    private List<String> imageUrls;
    
    // getter和setter
    public List<String> getImageUrls() {
        return imageUrls;
    }
    
    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }
}
```

## 安全考虑

### 1. 权限验证
- 确保只有有权限的用户才能删除图片
- 验证用户是否有权限删除指定博客的图片

### 2. 路径验证
- 验证图片URL是否在允许的目录范围内
- 防止路径遍历攻击

### 3. 文件类型验证
- 只允许删除图片文件
- 验证文件扩展名

## 测试用例

### 1. 正常删除
- 请求包含有效的图片URL
- 验证文件被成功删除
- 验证响应数据正确

### 2. 文件不存在
- 请求包含不存在的图片URL
- 验证响应中failedUrls包含该URL
- 验证deletedCount正确

### 3. 权限不足
- 使用无权限的用户请求
- 验证返回401或403状态码

### 4. 批量删除
- 请求包含多个图片URL
- 验证部分成功部分失败的情况
- 验证响应数据完整性

## 部署注意事项

### 1. 文件系统权限
- 确保应用有权限删除上传目录中的文件
- 考虑使用专门的用户运行应用

### 2. 日志记录
- 记录所有删除操作
- 便于问题排查和审计

### 3. 监控告警
- 监控删除操作的失败率
- 设置异常情况的告警机制

## 总结

实现这个API接口后，前端就可以在编辑博客成功后自动清理已删除的图片，避免存储空间浪费，提高系统效率。接口设计考虑了安全性、错误处理和用户体验，确保系统的稳定性和可靠性。
